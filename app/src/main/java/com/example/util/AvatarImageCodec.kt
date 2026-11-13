package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Utilitário para comprimir fotos da galeria em Data URIs Base64 compactos (~10-28 KB),
 * permitindo que a foto escolhida pelo usuário seja sincronizada no Cloud Firestore
 * e exibida globalmente para todos os usuários no placar e perfis.
 */
object AvatarImageCodec {

    private const val TAG = "AvatarImageCodec"
    const val JPEG_DATA_URI_PREFIX = "data:image/jpeg;base64,"
    const val DEFAULT_TARGET_MAX_PX = 192
    const val DEFAULT_MAX_JPEG_BYTES = 28_000

    /**
     * Retorna true se a URL/valor da foto puder ser carregado por QUALQUER usuário em outro aparelho
     * (HTTPS/HTTP do Google, avatares temáticos `preset:...`, ou imagens embutidas `data:image/...`).
     */
    fun isShareableAvatarUrl(url: String?): Boolean {
        val clean = url?.trim().orEmpty()
        if (clean.isBlank()) return false
        return clean.startsWith("https://", ignoreCase = true) ||
            clean.startsWith("http://", ignoreCase = true) ||
            clean.startsWith("preset:", ignoreCase = true) ||
            clean.startsWith("data:image/", ignoreCase = true)
    }

    /**
     * Retorna true se a URI apontar apenas para o armazenamento local deste dispositivo (`file://` ou `content://`).
     */
    fun isLocalFileOrContentUri(url: String?): Boolean {
        val clean = url?.trim().orEmpty()
        if (clean.isBlank()) return false
        return clean.startsWith("file://", ignoreCase = true) ||
            clean.startsWith("content://", ignoreCase = true)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun encodeJpegBytesToDataUrl(jpegBytes: ByteArray): String {
        val b64 = Base64.encode(jpegBytes)
        return "$JPEG_DATA_URI_PREFIX$b64"
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decodeDataUrlToBytes(dataUrl: String?): ByteArray? {
        val clean = dataUrl?.trim().orEmpty()
        if (!clean.startsWith("data:image/", ignoreCase = true)) return null
        val marker = "base64,"
        val idx = clean.indexOf(marker, ignoreCase = true)
        if (idx < 0) return null
        val payload = clean.substring(idx + marker.length).trim()
        if (payload.isBlank()) return null
        return try {
            Base64.decode(payload)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode Base64 avatar: ${e.message}")
            null
        }
    }

    /**
     * Lê uma imagem da galeria (`content://` ou `file://`), corrige orientação EXIF,
     * recorta em quadrado central, redimensiona para [targetMaxPx] e comprime em JPEG
     * para caber abaixo de [maxBytes] (~28 KB) e sincronizar globalmente no Firestore.
     */
    fun encodeUriToDataUrl(
        context: Context,
        uri: Uri,
        targetMaxPx: Int = DEFAULT_TARGET_MAX_PX,
        maxBytes: Int = DEFAULT_MAX_JPEG_BYTES
    ): String? {
        return try {
            // 1. Descobrir dimensões sem carregar a imagem inteira em memória
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) {
                return null
            }

            // 2. Calcular subamostragem (inSampleSize) para evitar OOM
            var sampleSize = 1
            val desiredMax = targetMaxPx * 2
            while (
                (boundsOpts.outWidth / sampleSize) > desiredMax &&
                (boundsOpts.outHeight / sampleSize) > desiredMax
            ) {
                sampleSize *= 2
            }

            // 3. Ler orientação EXIF
            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    ExifInterface(exifStream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            // 4. Decodificar bitmap reduzido
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            val orientedBitmap = applyExifOrientation(rawBitmap, orientation)
            val squareBitmap = centerCropAndScaleSquare(orientedBitmap, targetMaxPx)

            // 5. Comprimir iterativamente até ficar abaixo de maxBytes
            val jpegBytes = compressWithinLimit(squareBitmap, maxBytes)
            squareBitmap.recycle()

            if (jpegBytes.isEmpty()) null else encodeJpegBytesToDataUrl(jpegBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding avatar URI to Data URL: ${e.message}", e)
            null
        }
    }

    private fun applyExifOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return source
        }
        return try {
            val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            if (rotated !== source) source.recycle()
            rotated
        } catch (_: Exception) {
            source
        }
    }

    private fun centerCropAndScaleSquare(source: Bitmap, targetMaxPx: Int): Bitmap {
        val side = minOf(source.width, source.height).coerceAtLeast(1)
        val xOffset = ((source.width - side) / 2).coerceAtLeast(0)
        val yOffset = ((source.height - side) / 2).coerceAtLeast(0)

        val cropped = if (source.width != side || source.height != side) {
            Bitmap.createBitmap(source, xOffset, yOffset, side, side).also {
                if (it !== source) source.recycle()
            }
        } else {
            source
        }

        val finalSide = minOf(side, targetMaxPx).coerceAtLeast(1)
        return if (cropped.width != finalSide || cropped.height != finalSide) {
            Bitmap.createScaledBitmap(cropped, finalSide, finalSide, true).also {
                if (it !== cropped) cropped.recycle()
            }
        } else {
            cropped
        }
    }

    private fun compressWithinLimit(bitmap: Bitmap, maxBytes: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        for (quality in listOf(84, 74, 64, 52, 40)) {
            baos.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            if (baos.size() <= maxBytes) {
                return baos.toByteArray()
            }
        }
        // Fallback reduzindo mais a resolução se a imagem ainda estiver acima do limite
        val compactSide = minOf(bitmap.width, 128).coerceAtLeast(64)
        val smaller = Bitmap.createScaledBitmap(bitmap, compactSide, compactSide, true)
        baos.reset()
        smaller.compress(Bitmap.CompressFormat.JPEG, 45, baos)
        if (smaller !== bitmap) smaller.recycle()
        return baos.toByteArray()
    }
}
