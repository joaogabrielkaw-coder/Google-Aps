package com.example.ui.game3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure Kotlin 3D Vector math & projection engine for interactive 3D scenes & 60fps 3D gameplay.
 * Supports composite multi-part procedural geometry, PBR-style directional key + hemisphere lighting,
 * distance fog, damped camera projection, and painter's depth sorting.
 */
data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)

    fun dot(o: Vec3): Float = x * o.x + y * o.y + z * o.z

    fun cross(o: Vec3): Vec3 = Vec3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vec3 {
        val len = length()
        return if (len > 1e-5f) Vec3(x / len, y / len, z / len) else Vec3(0f, 1f, 0f)
    }

    fun rotateX(rad: Float): Vec3 {
        val c = cos(rad)
        val s = sin(rad)
        return Vec3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(rad: Float): Vec3 {
        val c = cos(rad)
        val s = sin(rad)
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateZ(rad: Float): Vec3 {
        val c = cos(rad)
        val s = sin(rad)
        return Vec3(x * c - y * s, x * s + y * c, z)
    }
}

data class Face3D(
    val v0: Vec3,
    val v1: Vec3,
    val v2: Vec3,
    val v3: Vec3? = null,
    val baseColor: Color,
    val shininess: Float = 0.35f,
    val emissive: Float = 0f
) {
    val centroid: Vec3
        get() = if (v3 != null) {
            Vec3(
                (v0.x + v1.x + v2.x + v3.x) * 0.25f,
                (v0.y + v1.y + v2.y + v3.y) * 0.25f,
                (v0.z + v1.z + v2.z + v3.z) * 0.25f
            )
        } else {
            Vec3(
                (v0.x + v1.x + v2.x) / 3f,
                (v0.y + v1.y + v2.y) / 3f,
                (v0.z + v1.z + v2.z) / 3f
            )
        }

    fun normal(): Vec3 = (v1 - v0).cross(v2 - v0).normalized()
}

enum class LightingPreset(
    val labelPt: String,
    val labelEn: String,
    val skyTop: Color,
    val skyHorizon: Color,
    val fogColor: Color,
    val keyLightDir: Vec3,
    val keyColor: Color,
    val fillColor: Color,
    val gridColor: Color
) {
    STUDIO(
        labelPt = "Estúdio PBR",
        labelEn = "PBR Studio",
        skyTop = Color(0xFF0A0D18),
        skyHorizon = Color(0xFF1A1530),
        fogColor = Color(0xFF17132B),
        keyLightDir = Vec3(0.55f, 0.85f, -0.45f).normalized(),
        keyColor = Color(0xFFFFF3E0),
        fillColor = Color(0xFF6B8BFF),
        gridColor = Color(0xFF7B52AB)
    ),
    CYBER_NEON(
        labelPt = "Cyber Neon",
        labelEn = "Cyber Neon",
        skyTop = Color(0xFF070212),
        skyHorizon = Color(0xFF230A36),
        fogColor = Color(0xFF1C082C),
        keyLightDir = Vec3(-0.4f, 0.9f, -0.5f).normalized(),
        keyColor = Color(0xFFFF88E8),
        fillColor = Color(0xFF00E5FF),
        gridColor = Color(0xFFE040FB)
    ),
    EMERALD_LAB(
        labelPt = "Lab Esmeralda",
        labelEn = "Emerald Lab",
        skyTop = Color(0xFF041415),
        skyHorizon = Color(0xFF0E2C2E),
        fogColor = Color(0xFF0B2527),
        keyLightDir = Vec3(0.7f, 0.8f, -0.35f).normalized(),
        keyColor = Color(0xFFE0FFF4),
        fillColor = Color(0xFF26A69A),
        gridColor = Color(0xFF26C6DA)
    )
}

enum class ShowcaseModelType(val titlePt: String, val titleEn: String, val partCount: Int) {
    DEVRUNNER_CRAFT("Nave DevRunner", "DevRunner Craft", 8),
    DEVBOT_EXPLORER("Robô DevBot", "DevBot Explorer", 9),
    SQL_CLUSTER_TOWER("Cluster SQL 3D", "SQL Server Cluster", 10)
}

data class Camera3D(
    val position: Vec3,
    val target: Vec3,
    val fovScale: Float = 1.65f
) {
    val forward: Vec3 = (target - position).normalized()
    val right: Vec3 = Vec3(0f, 1f, 0f).cross(forward).normalized()
    val up: Vec3 = forward.cross(right).normalized()

    fun project(worldPt: Vec3, viewportW: Float, viewportH: Float): ProjectedPoint? {
        val rel = worldPt - position
        val camZ = rel.dot(forward)
        if (camZ <= 0.25f) return null
        val camX = rel.dot(right)
        val camY = rel.dot(up)
        val scale = (min(viewportW, viewportH) * 0.5f * fovScale) / camZ
        val sx = viewportW * 0.5f + camX * scale
        val sy = viewportH * 0.5f - camY * scale
        return ProjectedPoint(Offset(sx, sy), camZ, scale)
    }
}

data class ProjectedPoint(val screen: Offset, val depth: Float, val perspectiveScale: Float)

/**
 * Adds an axis-aligned 3D box (8 verts -> 6 quad faces) transformed into world space.
 */
fun MutableList<Face3D>.addBoxPart(
    center: Vec3,
    halfExtents: Vec3,
    color: Color,
    rotY: Float = 0f,
    rotX: Float = 0f,
    rotZ: Float = 0f,
    worldOffset: Vec3 = Vec3(0f, 0f, 0f),
    worldRoll: Float = 0f,
    worldYaw: Float = 0f,
    shininess: Float = 0.4f,
    emissive: Float = 0f
) {
    val hx = halfExtents.x
    val hy = halfExtents.y
    val hz = halfExtents.z
    val localVerts = arrayOf(
        Vec3(-hx, -hy, -hz), // 0 LBF
        Vec3(hx, -hy, -hz),  // 1 RBF
        Vec3(hx, hy, -hz),   // 2 RTF
        Vec3(-hx, hy, -hz),  // 3 LTF
        Vec3(-hx, -hy, hz),  // 4 LBB
        Vec3(hx, -hy, hz),   // 5 RBB
        Vec3(hx, hy, hz),    // 6 RTB
        Vec3(-hx, hy, hz)    // 7 LTB
    ).map { v ->
        var p = v.rotateX(rotX).rotateY(rotY).rotateZ(rotZ) + center
        if (worldRoll != 0f) p = p.rotateZ(worldRoll)
        if (worldYaw != 0f) p = p.rotateY(worldYaw)
        p + worldOffset
    }

    // Quad winding order for outward normals:
    add(Face3D(localVerts[0], localVerts[1], localVerts[2], localVerts[3], color, shininess, emissive)) // Front (-Z)
    add(Face3D(localVerts[5], localVerts[4], localVerts[7], localVerts[6], color, shininess, emissive)) // Back (+Z)
    add(Face3D(localVerts[4], localVerts[0], localVerts[3], localVerts[7], color, shininess, emissive)) // Left (-X)
    add(Face3D(localVerts[1], localVerts[5], localVerts[6], localVerts[2], color, shininess, emissive)) // Right (+X)
    add(Face3D(localVerts[3], localVerts[2], localVerts[6], localVerts[7], color, shininess, emissive)) // Top (+Y)
    add(Face3D(localVerts[4], localVerts[5], localVerts[1], localVerts[0], color, shininess, emissive)) // Bottom (-Y)
}

/**
 * Adds a procedural hexagonal prism/cylinder part (e.g. engine nacelles, turrets, database platters).
 */
fun MutableList<Face3D>.addHexPrismPart(
    center: Vec3,
    radius: Float,
    height: Float,
    sides: Int = 6,
    color: Color,
    worldOffset: Vec3 = Vec3(0f, 0f, 0f),
    worldYaw: Float = 0f,
    worldRoll: Float = 0f,
    shininess: Float = 0.55f,
    emissive: Float = 0f
) {
    val hh = height * 0.5f
    val bottomRing = mutableListOf<Vec3>()
    val topRing = mutableListOf<Vec3>()
    for (i in 0 until sides) {
        val angle = (2.0 * PI * i / sides).toFloat()
        val x = cos(angle) * radius
        val z = sin(angle) * radius
        var b = Vec3(x, -hh, z) + center
        var t = Vec3(x, hh, z) + center
        if (worldRoll != 0f) {
            b = b.rotateZ(worldRoll)
            t = t.rotateZ(worldRoll)
        }
        if (worldYaw != 0f) {
            b = b.rotateY(worldYaw)
            t = t.rotateY(worldYaw)
        }
        bottomRing.add(b + worldOffset)
        topRing.add(t + worldOffset)
    }

    for (i in 0 until sides) {
        val next = (i + 1) % sides
        add(Face3D(bottomRing[i], bottomRing[next], topRing[next], topRing[i], color, shininess, emissive))
    }
    var topCenter = Vec3(0f, hh, 0f) + center
    var botCenter = Vec3(0f, -hh, 0f) + center
    if (worldRoll != 0f) {
        topCenter = topCenter.rotateZ(worldRoll)
        botCenter = botCenter.rotateZ(worldRoll)
    }
    if (worldYaw != 0f) {
        topCenter = topCenter.rotateY(worldYaw)
        botCenter = botCenter.rotateY(worldYaw)
    }
    topCenter += worldOffset
    botCenter += worldOffset

    for (i in 0 until sides) {
        val next = (i + 1) % sides
        add(Face3D(topCenter, topRing[i], topRing[next], null, color, shininess, emissive))
        add(Face3D(botCenter, bottomRing[next], bottomRing[i], null, color, shininess, emissive))
    }
}

/**
 * Adds a spinning 3D octahedral crystal collectible (8 triangular facets + floating ring).
 */
fun MutableList<Face3D>.addCrystalGem(
    center: Vec3,
    radius: Float,
    height: Float,
    spinRad: Float,
    color: Color
) {
    val top = Vec3(0f, height * 0.5f, 0f) + center
    val bot = Vec3(0f, -height * 0.5f, 0f) + center
    val eq = (0 until 4).map { i ->
        val a = spinRad + (PI.toFloat() * 0.5f * i)
        Vec3(cos(a) * radius, 0f, sin(a) * radius) + center
    }
    for (i in 0 until 4) {
        val n = (i + 1) % 4
        add(Face3D(top, eq[n], eq[i], null, color, shininess = 0.85f, emissive = 0.35f))
        add(Face3D(bot, eq[i], eq[n], null, color.copy(alpha = 0.95f), shininess = 0.7f, emissive = 0.25f))
    }
}

/**
 * Builds composite 8-part DevRunner Craft model.
 */
fun buildDevRunnerCraftFaces(
    worldPos: Vec3,
    rollRad: Float = 0f,
    yawRad: Float = 0f,
    hullAccent: Color = Color(0xFF7B52AB),
    hitFlash: Float = 0f
): List<Face3D> {
    val faces = mutableListOf<Face3D>()
    val primary = if (hitFlash > 0f) lerp(Color(0xFFE8EAFF), Color(0xFFFF5252), hitFlash) else Color(0xFFE8EAFF)
    val accent = if (hitFlash > 0f) Color(0xFFFF8A80) else hullAccent
    val glow = Color(0xFF00E5FF)

    // 1. Main central fuselage
    faces.addBoxPart(Vec3(0f, 0.38f, 0f), Vec3(0.42f, 0.24f, 0.95f), primary, worldOffset = worldPos, worldRoll = rollRad, worldYaw = yawRad, shininess = 0.6f)
    // 2. Beveled forward nose cone
    faces.addBoxPart(Vec3(0f, 0.34f, -1.05f), Vec3(0.24f, 0.16f, 0.38f), accent, rotX = 0.12f, worldOffset = worldPos, worldRoll = rollRad, worldYaw = yawRad, shininess = 0.7f)
    // 3. Glass cockpit canopy
    faces.addBoxPart(Vec3(0f, 0.62f, -0.18f), Vec3(0.26f, 0.16f, 0.44f), glow, rotX = -0.18f, worldOffset = worldPos, worldRoll = rollRad, worldYaw = yawRad, shininess = 0.95f, emissive = 0.3f)
    // 4. Port swept wing
    faces.addBoxPart(Vec3(-0.78f, 0.30f, 0.2f), Vec3(0.48f, 0.06f, 0.42f), accent, rotY = 0.35f, worldOffset = worldPos, worldRoll = rollRad, worldYaw = yawRad, shininess = 0.5f)
    // 5. Starboard swept wing
    faces.addBoxPart(Vec3(0.78f, 0.30f, 0.2f), Vec3(0.48f, 0.06f, 0.42f), accent, rotY = -0.35f, worldOffset = worldPos, worldRoll = rollRad, worldYaw = yawRad, shininess = 0.5f)
    // 6. Left thruster nacelle
    faces.addBoxPart(Vec3(-0.52f, 0.40f, 0.72f), Vec3(0.16f, 0.16f, 0.36f), Color(0xFF28243E), worldOffset = worldPos, worldRoll = rollRad, worldYaw = yawRad, shininess = 0.8f)
    // 7. Right thruster nacelle
    faces.addBoxPart(Vec3(0.52f, 0.40f, 0.72f), Vec3(0.16f, 0.16f, 0.36f), Color(0xFF28243E), worldOffset = worldPos, worldRoll = rollRad, worldYaw = yawRad, shininess = 0.8f)
    // 8. Rear twin ion exhaust cores
    faces.addBoxPart(Vec3(0f, 0.38f, 1.02f), Vec3(0.28f, 0.12f, 0.12f), Color(0xFFFF3D7F), worldOffset = worldPos, worldRoll = rollRad, worldYaw = yawRad, shininess = 0.9f, emissive = 0.85f)
    return faces
}

/**
 * Builds composite 9-part DevBot Explorer 3D character for the 3D Scene Showcase.
 */
fun buildDevBotExplorerFaces(worldPos: Vec3, yawRad: Float): List<Face3D> {
    val faces = mutableListOf<Face3D>()
    val chassis = Color(0xFF4F46E5)
    val armor = Color(0xFFE0E7FF)
    val visor = Color(0xFF00E5FF)

    // 1. Floating thruster pedestal
    faces.addHexPrismPart(Vec3(0f, 0.22f, 0f), radius = 0.72f, height = 0.28f, sides = 8, color = Color(0xFF262341), worldOffset = worldPos, worldYaw = yawRad, shininess = 0.8f)
    // 2. Core torso
    faces.addBoxPart(Vec3(0f, 0.95f, 0f), Vec3(0.54f, 0.52f, 0.42f), armor, worldOffset = worldPos, worldYaw = yawRad, shininess = 0.55f)
    // 3. Chest reactor medallion
    faces.addBoxPart(Vec3(0f, 0.98f, -0.44f), Vec3(0.22f, 0.22f, 0.06f), Color(0xFFFF3D7F), worldOffset = worldPos, worldYaw = yawRad, emissive = 0.75f)
    // 4. Head cube
    faces.addBoxPart(Vec3(0f, 1.82f, 0f), Vec3(0.44f, 0.34f, 0.38f), chassis, worldOffset = worldPos, worldYaw = yawRad, shininess = 0.6f)
    // 5. Glowing visor screen
    faces.addBoxPart(Vec3(0f, 1.84f, -0.40f), Vec3(0.32f, 0.14f, 0.05f), visor, worldOffset = worldPos, worldYaw = yawRad, emissive = 0.85f)
    // 6. Antenna stalk & beacon
    faces.addBoxPart(Vec3(0.24f, 2.34f, 0f), Vec3(0.06f, 0.22f, 0.06f), Color(0xFFFFB300), worldOffset = worldPos, worldYaw = yawRad, emissive = 0.6f)
    // 7. Left articulated shoulder + arm
    faces.addBoxPart(Vec3(-0.78f, 0.98f, -0.08f), Vec3(0.18f, 0.42f, 0.20f), chassis, rotX = 0.3f, worldOffset = worldPos, worldYaw = yawRad)
    // 8. Right articulated shoulder + arm
    faces.addBoxPart(Vec3(0.78f, 0.98f, -0.08f), Vec3(0.18f, 0.42f, 0.20f), chassis, rotX = -0.3f, worldOffset = worldPos, worldYaw = yawRad)
    // 9. Jetpack backpack
    faces.addBoxPart(Vec3(0f, 1.02f, 0.52f), Vec3(0.34f, 0.36f, 0.16f), Color(0xFF312E81), worldOffset = worldPos, worldYaw = yawRad)
    return faces
}

/**
 * Builds composite 10-part Relational SQL Cluster Tower 3D model.
 */
fun buildSqlClusterTowerFaces(worldPos: Vec3, yawRad: Float, pulseTime: Float): List<Face3D> {
    val faces = mutableListOf<Face3D>()
    val tierColors = listOf(Color(0xFF336791), Color(0xFF00758F), Color(0xFF7B52AB))
    // 1-3: Three stacked octagonal database disk platters
    for (i in 0 until 3) {
        val y = 0.36f + i * 0.68f
        faces.addHexPrismPart(
            center = Vec3(0f, y, 0f),
            radius = 1.05f - i * 0.12f,
            height = 0.32f,
            sides = 8,
            color = tierColors[i],
            worldOffset = worldPos,
            worldYaw = yawRad + i * 0.25f,
            shininess = 0.7f
        )
        // 4-6: Glowing telemetry ring accents between platters
        faces.addHexPrismPart(
            center = Vec3(0f, y + 0.22f, 0f),
            radius = (0.72f - i * 0.08f),
            height = 0.08f,
            sides = 8,
            color = Color(0xFF00E5FF),
            worldOffset = worldPos,
            worldYaw = yawRad,
            emissive = 0.45f + 0.25f * sin(pulseTime * 3f + i)
        )
    }
    // 7-9: Three vertical structural data bus pillars
    for (k in 0 until 3) {
        val angle = yawRad + (k * 2.094f)
        val px = cos(angle) * 0.92f
        val pz = sin(angle) * 0.92f
        faces.addBoxPart(
            center = Vec3(px, 1.05f, pz),
            halfExtents = Vec3(0.08f, 0.92f, 0.08f),
            color = Color(0xFFE2E8F0),
            worldOffset = worldPos,
            shininess = 0.85f
        )
    }
    // 10: Floating top query crystal
    faces.addCrystalGem(
        center = worldPos + Vec3(0f, 2.55f + 0.12f * sin(pulseTime * 2.5f), 0f),
        radius = 0.34f,
        height = 0.64f,
        spinRad = yawRad * 2f,
        color = Color(0xFFFFD54F)
    )
    return faces
}

/**
 * Renders sorted 3D faces onto Compose Canvas with PBR directional + hemisphere + specular lighting & horizon fog.
 */
fun DrawScope.renderScene3D(
    faces: List<Face3D>,
    camera: Camera3D,
    preset: LightingPreset,
    wireframeOnly: Boolean = false,
    gridScrollZ: Float = 0f,
    showLaneGuides: Boolean = false
) {
    // 1. Atmosphere sky-to-horizon gradient backdrop
    drawRect(
        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(preset.skyTop, preset.skyHorizon)
        )
    )

    // 2. Perspective 3D ground grid & horizon lane rails
    val maxGridZ = 34f
    val minGridZ = -2f
    for (gx in -8..8 step 2) {
        val pStart = camera.project(Vec3(gx.toFloat(), 0f, minGridZ), size.width, size.height)
        val pEnd = camera.project(Vec3(gx.toFloat(), 0f, maxGridZ), size.width, size.height)
        if (pStart != null && pEnd != null) {
            val isLaneDivider = showLaneGuides && (gx == -2 || gx == 2)
            drawLine(
                color = if (isLaneDivider) preset.fillColor.copy(alpha = 0.45f) else preset.gridColor.copy(alpha = 0.24f),
                start = pStart.screen,
                end = pEnd.screen,
                strokeWidth = if (isLaneDivider) 2.5f else 1.2f
            )
        }
    }
    val stepZ = 3f
    var gz = -stepZ + (gridScrollZ % stepZ)
    while (gz <= maxGridZ) {
        if (gz > 0.4f) {
            val pL = camera.project(Vec3(-10f, 0f, gz), size.width, size.height)
            val pR = camera.project(Vec3(10f, 0f, gz), size.width, size.height)
            if (pL != null && pR != null) {
                val fogAlpha = (1f - (gz / maxGridZ)).coerceIn(0.05f, 0.35f)
                drawLine(
                    color = preset.gridColor.copy(alpha = fogAlpha),
                    start = pL.screen,
                    end = pR.screen,
                    strokeWidth = 1.2f
                )
            }
        }
        gz += stepZ
    }

    // 3. Transform, cull, light & painter-sort 3D faces back-to-front
    data class DrawableFace(
        val points: List<Offset>,
        val depth: Float,
        val shadedColor: Color
    )

    val drawList = ArrayList<DrawableFace>(faces.size)
    val fogFar = 38f

    for (face in faces) {
        val c = face.centroid
        val viewVector = (camera.position - c).normalized()
        val n = face.normal()
        // Backface culling when solid
        if (!wireframeOnly && n.dot(viewVector) < -0.08f) continue

        val p0 = camera.project(face.v0, size.width, size.height) ?: continue
        val p1 = camera.project(face.v1, size.width, size.height) ?: continue
        val p2 = camera.project(face.v2, size.width, size.height) ?: continue
        val p3 = if (face.v3 != null) camera.project(face.v3, size.width, size.height) ?: continue else null

        val avgDepth = if (p3 != null) {
            (p0.depth + p1.depth + p2.depth + p3.depth) * 0.25f
        } else {
            (p0.depth + p1.depth + p2.depth) / 3f
        }

        // Hemisphere sky bounce + directional key light + Blinn-Phong specular reflection
        val keyDiff = max(0f, n.dot(preset.keyLightDir))
        val hemiFill = ((n.y + 1f) * 0.5f).coerceIn(0.15f, 1f)
        val halfVec = (preset.keyLightDir + viewVector).normalized()
        val spec = Math.pow(max(0f, n.dot(halfVec)).toDouble(), 16.0).toFloat() * face.shininess

        val litR = (face.baseColor.red * (0.28f + 0.65f * keyDiff + 0.22f * hemiFill + face.emissive) +
            preset.keyColor.red * spec * 0.45f).coerceIn(0f, 1f)
        val litG = (face.baseColor.green * (0.28f + 0.65f * keyDiff + 0.22f * hemiFill + face.emissive) +
            preset.keyColor.green * spec * 0.45f).coerceIn(0f, 1f)
        val litB = (face.baseColor.blue * (0.28f + 0.65f * keyDiff + 0.22f * hemiFill + face.emissive) +
            preset.keyColor.blue * spec * 0.45f).coerceIn(0f, 1f)

        val litColor = Color(litR, litG, litB, face.baseColor.alpha)
        val fogFactor = ((avgDepth - 10f) / (fogFar - 10f)).coerceIn(0f, 0.82f)
        val finalColor = lerp(litColor, preset.fogColor, fogFactor)

        val pts = if (p3 != null) listOf(p0.screen, p1.screen, p2.screen, p3.screen) else listOf(p0.screen, p1.screen, p2.screen)
        drawList.add(DrawableFace(pts, avgDepth, finalColor))
    }

    drawList.sortByDescending { it.depth }

    for (df in drawList) {
        val path = Path().apply {
            moveTo(df.points[0].x, df.points[0].y)
            for (i in 1 until df.points.size) {
                lineTo(df.points[i].x, df.points[i].y)
            }
            close()
        }
        if (wireframeOnly) {
            drawPath(path = path, color = df.shadedColor.copy(alpha = 0.14f))
            drawPath(path = path, color = Color(0xFF00E5FF).copy(alpha = 0.78f), style = Stroke(width = 1.6f))
        } else {
            drawPath(path = path, color = df.shadedColor)
            drawPath(path = path, color = Color.Black.copy(alpha = 0.16f), style = Stroke(width = 0.8f))
        }
    }
}
