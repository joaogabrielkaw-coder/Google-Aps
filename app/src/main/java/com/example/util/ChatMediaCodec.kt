package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val TAG = "ChatMediaCodec"

data class PreparedChatMedia(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val localFilePath: String,
    val inlineDataUrl: String = "",
    val thumbnailDataUrl: String = "",
    val base64Chunks: List<String> = emptyList(),
    val exceedsCloudTransferLimit: Boolean = false
)

object ChatMediaCodec {

    const val MAX_IMAGE_MAX_PX = 1080
    const val MAX_INLINE_DATA_URL_BYTES = 460_000
    const val CHUNK_SIZE_CHARS = 350_000
    const val MAX_CHUNKS = 10
    const val MAX_CLOUD_BINARY_BYTES = 2_600_000L // ~2.6 MB binary -> ~3.47 MB Base64 <= 10 chunks

    fun mediaDirForChat(context: Context, chatId: String): File {
        val dir = File(File(context.filesDir, "chat_media"), chatId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_"))
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun resolveDisplayFileName(context: Context, uri: Uri, fallback: String): String {
        return try {
            val resolved: String? = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx)?.takeIf { it.isNotBlank() } else null
                } else {
                    null
                }
            }
            resolved ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }

    fun formatByteSize(bytes: Long): String {
        return when {
            bytes < 1024L -> "$bytes B"
            bytes < 1024L * 1024L -> "${bytes / 1024L} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun splitBase64IntoChunks(base64Payload: String, chunkSize: Int = CHUNK_SIZE_CHARS): List<String> {
        if (base64Payload.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        var idx = 0
        while (idx < base64Payload.length) {
            val end = (idx + chunkSize).coerceAtMost(base64Payload.length)
            result.add(base64Payload.substring(idx, end))
            idx = end
        }
        return result
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun encodeImageForChat(
        context: Context,
        uri: Uri,
        chatId: String,
        messageId: String
    ): PreparedChatMedia? {
        return try {
            val fileName = resolveDisplayFileName(context, uri, "image_${messageId.take(6)}.jpg")
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOpts)
            }
            if (boundsOpts.outWidth <= 0 || boundsOpts.outHeight <= 0) return null

            var sample = 1
            while ((boundsOpts.outWidth / sample) > MAX_IMAGE_MAX_PX * 2 ||
                (boundsOpts.outHeight / sample) > MAX_IMAGE_MAX_PX * 2
            ) {
                sample *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
            val rawBitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: return null

            val maxDim = maxOf(rawBitmap.width, rawBitmap.height).coerceAtLeast(1)
            val scaled = if (maxDim > MAX_IMAGE_MAX_PX) {
                val ratio = MAX_IMAGE_MAX_PX.toFloat() / maxDim.toFloat()
                Bitmap.createScaledBitmap(
                    rawBitmap,
                    (rawBitmap.width * ratio).toInt().coerceAtLeast(1),
                    (rawBitmap.height * ratio).toInt().coerceAtLeast(1),
                    true
                ).also { if (it !== rawBitmap) rawBitmap.recycle() }
            } else {
                rawBitmap
            }

            val baos = ByteArrayOutputStream()
            for (q in listOf(85, 72, 58, 45, 34)) {
                baos.reset()
                scaled.compress(Bitmap.CompressFormat.JPEG, q, baos)
                if (baos.size() <= 320_000) break
            }
            scaled.recycle()

            val bytes = baos.toByteArray()
            val outFile = File(mediaDirForChat(context, chatId), "${messageId}_img.jpg")
            outFile.writeBytes(bytes)
            val dataUrl = "data:image/jpeg;base64,${Base64.encode(bytes)}"

            PreparedChatMedia(
                fileName = fileName,
                mimeType = "image/jpeg",
                sizeBytes = bytes.size.toLong(),
                localFilePath = outFile.absolutePath,
                inlineDataUrl = if (dataUrl.length <= MAX_INLINE_DATA_URL_BYTES) dataUrl else "",
                base64Chunks = if (dataUrl.length > MAX_INLINE_DATA_URL_BYTES) {
                    splitBase64IntoChunks(Base64.encode(bytes))
                } else emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding chat image: ${e.message}", e)
            null
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun prepareFileOrVideoForChat(
        context: Context,
        uri: Uri,
        chatId: String,
        messageId: String,
        isVideo: Boolean
    ): PreparedChatMedia? {
        return try {
            val defaultName = if (isVideo) "video_${messageId.take(6)}.mp4" else "file_${messageId.take(6)}"
            val fileName = resolveDisplayFileName(context, uri, defaultName)
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._\\-]"), "_").takeLast(64)
            val mimeType = context.contentResolver.getType(uri)
                ?: if (isVideo) "video/mp4" else "application/octet-stream"

            val outFile = File(mediaDirForChat(context, chatId), "${messageId}_$safeName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            val sizeBytes = outFile.length()
            val thumbDataUrl = if (isVideo) extractVideoThumbnailDataUrl(outFile) else ""

            if (sizeBytes > MAX_CLOUD_BINARY_BYTES) {
                return PreparedChatMedia(
                    fileName = fileName,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    localFilePath = outFile.absolutePath,
                    thumbnailDataUrl = thumbDataUrl,
                    exceedsCloudTransferLimit = true
                )
            }

            val bytes = outFile.readBytes()
            val b64 = Base64.encode(bytes)
            val fullDataUrl = "data:$mimeType;base64,$b64"

            if (fullDataUrl.length <= MAX_INLINE_DATA_URL_BYTES) {
                PreparedChatMedia(
                    fileName = fileName,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    localFilePath = outFile.absolutePath,
                    inlineDataUrl = fullDataUrl,
                    thumbnailDataUrl = thumbDataUrl
                )
            } else {
                val chunks = splitBase64IntoChunks(b64)
                PreparedChatMedia(
                    fileName = fileName,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    localFilePath = outFile.absolutePath,
                    thumbnailDataUrl = thumbDataUrl,
                    base64Chunks = chunks.take(MAX_CHUNKS),
                    exceedsCloudTransferLimit = chunks.size > MAX_CHUNKS
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing file/video for chat: ${e.message}", e)
            null
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun extractVideoThumbnailDataUrl(videoFile: File): String {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoFile.absolutePath)
            val frame = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: return ""
            val maxDim = maxOf(frame.width, frame.height).coerceAtLeast(1)
            val scaled = if (maxDim > 360) {
                val ratio = 360f / maxDim.toFloat()
                Bitmap.createScaledBitmap(
                    frame,
                    (frame.width * ratio).toInt().coerceAtLeast(1),
                    (frame.height * ratio).toInt().coerceAtLeast(1),
                    true
                ).also { if (it !== frame) frame.recycle() }
            } else {
                frame
            }
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            scaled.recycle()
            "data:image/jpeg;base64,${Base64.encode(baos.toByteArray())}"
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract video thumbnail: ${e.message}")
            ""
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun saveBase64PayloadToLocalFile(
        context: Context,
        chatId: String,
        messageId: String,
        fileName: String,
        dataUrlOrBase64: String
    ): String? {
        return try {
            val cleanPayload = if (dataUrlOrBase64.startsWith("data:", ignoreCase = true)) {
                val idx = dataUrlOrBase64.indexOf("base64,", ignoreCase = true)
                if (idx >= 0) dataUrlOrBase64.substring(idx + 7) else return null
            } else {
                dataUrlOrBase64
            }
            val bytes = Base64.decode(cleanPayload.trim())
            val safeName = fileName.ifBlank { "attachment_$messageId" }
                .replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
                .takeLast(64)
            val outFile = File(mediaDirForChat(context, chatId), "${messageId}_$safeName")
            outFile.writeBytes(bytes)
            outFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode media payload to local file: ${e.message}", e)
            null
        }
    }

    fun openLocalMediaFile(context: Context, localPath: String, mimeType: String): Boolean {
        val file = File(localPath)
        if (!file.exists()) return false
        return try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, file.name).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open local file with external viewer: ${e.message}")
            false
        }
    }
}
