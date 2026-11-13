package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.Technology
import com.example.ui.game3d.Camera3D
import com.example.ui.game3d.Face3D
import com.example.ui.game3d.LightingPreset
import com.example.ui.game3d.ShowcaseModelType
import com.example.ui.game3d.Vec3
import com.example.ui.game3d.addBoxPart
import com.example.ui.game3d.addCrystalGem
import com.example.ui.game3d.buildDevBotExplorerFaces
import com.example.ui.game3d.buildDevRunnerCraftFaces
import com.example.ui.game3d.buildSqlClusterTowerFaces
import com.example.ui.game3d.renderScene3D
import com.example.ui.theme.LocalDevRoutineColors
import com.example.util.SoundEffectsEngine
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

private enum class GameViewportMode {
    ARENA_GAME,
    SCENE_STUDIO_3D
}

private enum class GamePlayPhase {
    TITLE,
    PLAYING,
    PAUSED,
    GAME_OVER
}

private enum class ObstacleKind {
    TALL_FIREWALL,
    LOW_BARRIER
}

private data class ArenaEntity3D(
    var laneIndex: Int, // 0, 1, 2 -> x = -2.4, 0, +2.4
    var z: Float,
    val isCrystal: Boolean,
    val obstacleKind: ObstacleKind = ObstacleKind.TALL_FIREWALL,
    var collectedOrHit: Boolean = false
)

private data class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var life: Float,
    val maxLife: Float,
    val color: Color
)

private val LANE_POSITIONS = floatArrayOf(-2.4f, 0f, 2.4f)

/**
 * Full-Viewport 3D Game Arena ("DevCore 3D Runner") + Interactive 3D Scene Studio.
 * Combines a 60fps damped 3D simulation loop, composite multi-part procedural 3D models,
 * PBR-inspired lighting presets, hitstop/camera shake/particle bursts, procedural AudioTrack SFX,
 * and automatic Room SQLite score persistence.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameAndScene3DScreen(
    language: Language,
    bestScore: Int,
    selectedFocusTech: Technology,
    onGameFinished: (score: Int, maxCombo: Int, crystals: Int, durationSec: Int, focusTech: Technology) -> Unit,
    onExit: () -> Unit
) {
    val isPt = language == Language.PT
    var viewportMode by remember { mutableStateOf(GameViewportMode.ARENA_GAME) }
    var playPhase by remember { mutableStateOf(GamePlayPhase.TITLE) }
    var isMuted by remember { mutableStateOf(SoundEffectsEngine.isMuted) }
    var lightingPreset by remember { mutableStateOf(LightingPreset.STUDIO) }

    // 3D Scene Studio state (orbit camera + model switcher)
    var showcaseModel by remember { mutableStateOf(ShowcaseModelType.DEVRUNNER_CRAFT) }
    var orbitYawTarget by remember { mutableFloatStateOf(0.55f) }
    var orbitPitchTarget by remember { mutableFloatStateOf(0.32f) }
    var orbitYawCurrent by remember { mutableFloatStateOf(0.55f) }
    var orbitPitchCurrent by remember { mutableFloatStateOf(0.32f) }
    var orbitDistance by remember { mutableFloatStateOf(6.2f) }
    var autoRotateShowcase by remember { mutableStateOf(true) }
    var wireframeMode by remember { mutableStateOf(false) }
    var studioDockExpanded by remember { mutableStateOf(false) }

    // Arena Game simulation state
    var playerLane by remember { mutableIntStateOf(1) }
    var playerX by remember { mutableFloatStateOf(0f) }
    var playerY by remember { mutableFloatStateOf(0f) }
    var playerVy by remember { mutableFloatStateOf(0f) }
    var playerRoll by remember { mutableFloatStateOf(0f) }
    var jumpBufferTimer by remember { mutableFloatStateOf(0f) }
    var hitstopTimer by remember { mutableFloatStateOf(0f) }
    var cameraShakeTimer by remember { mutableFloatStateOf(0f) }
    var hitFlashTimer by remember { mutableFloatStateOf(0f) }

    var score by remember { mutableIntStateOf(0) }
    var crystals by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(1) }
    var maxCombo by remember { mutableIntStateOf(1) }
    var livesRemaining by remember { mutableIntStateOf(3) }
    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    var worldScrollZ by remember { mutableFloatStateOf(0f) }
    var spawnAccumulator by remember { mutableFloatStateOf(0f) }
    var globalTime by remember { mutableFloatStateOf(0f) }
    var scoreSubmittedThisRun by remember { mutableStateOf(false) }

    val entities = remember { mutableStateListOf<ArenaEntity3D>() }
    val particles = remember { mutableStateListOf<Particle3D>() }

    fun resetAndStartGame() {
        playerLane = 1
        playerX = 0f
        playerY = 0f
        playerVy = 0f
        playerRoll = 0f
        jumpBufferTimer = 0f
        hitstopTimer = 0f
        cameraShakeTimer = 0f
        hitFlashTimer = 0f
        score = 0
        crystals = 0
        combo = 1
        maxCombo = 1
        livesRemaining = 3
        elapsedSeconds = 0f
        worldScrollZ = 0f
        spawnAccumulator = 0f
        scoreSubmittedThisRun = false
        entities.clear()
        particles.clear()
        entities.add(ArenaEntity3D(laneIndex = 1, z = 18f, isCrystal = true))
        entities.add(ArenaEntity3D(laneIndex = 2, z = 24f, isCrystal = true))
        playPhase = GamePlayPhase.PLAYING
        SoundEffectsEngine.playStartFanfare()
    }

    fun triggerJump() {
        jumpBufferTimer = 0.14f
    }

    fun moveLane(delta: Int) {
        playerLane = (playerLane + delta).coerceIn(0, 2)
    }

    fun spawnBurst(x: Float, y: Float, z: Float, color: Color, count: Int) {
        if (particles.size > 60) {
            repeat((particles.size - 45).coerceAtLeast(0)) {
                if (particles.isNotEmpty()) particles.removeAt(0)
            }
        }
        repeat(count) {
            particles.add(
                Particle3D(
                    x = x,
                    y = y + 0.4f,
                    z = z,
                    vx = (Random.nextFloat() - 0.5f) * 5.2f,
                    vy = 1.6f + Random.nextFloat() * 4.5f,
                    vz = (Random.nextFloat() - 0.5f) * 5.2f,
                    life = 0.48f,
                    maxLife = 0.48f,
                    color = color
                )
            )
        }
    }

    // 60fps simulation loop using Compose frame clock with clamped dt <= 0.05f
    LaunchedEffect(viewportMode, playPhase, autoRotateShowcase) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nowNanos ->
                if (lastNanos == 0L) {
                    lastNanos = nowNanos
                    return@withFrameNanos
                }
                val rawDt = (nowNanos - lastNanos) / 1_000_000_000f
                lastNanos = nowNanos
                val dt = min(rawDt, 0.05f).coerceAtLeast(0.0001f)
                globalTime += dt

                // Damped orbit smoothing for 3D Studio showcase
                if (viewportMode == GameViewportMode.SCENE_STUDIO_3D) {
                    if (autoRotateShowcase) {
                        orbitYawTarget += dt * 0.45f
                    }
                    val damp = 1f - exp(-9f * dt)
                    orbitYawCurrent += (orbitYawTarget - orbitYawCurrent) * damp
                    orbitPitchCurrent += (orbitPitchTarget - orbitPitchCurrent) * damp
                } else if (playPhase == GamePlayPhase.TITLE) {
                    worldScrollZ += dt * 6.5f
                } else if (playPhase == GamePlayPhase.PLAYING) {
                    if (hitstopTimer > 0f) {
                        hitstopTimer = (hitstopTimer - dt).coerceAtLeast(0f)
                        return@withFrameNanos
                    }

                    elapsedSeconds += dt
                    if (cameraShakeTimer > 0f) cameraShakeTimer = (cameraShakeTimer - dt).coerceAtLeast(0f)
                    if (hitFlashTimer > 0f) hitFlashTimer = (hitFlashTimer - dt).coerceAtLeast(0f)
                    if (jumpBufferTimer > 0f) jumpBufferTimer = (jumpBufferTimer - dt).coerceAtLeast(0f)

                    val speed = (12.5f + elapsedSeconds * 0.28f).coerceAtMost(26f)
                    worldScrollZ += speed * dt
                    score += (speed * dt * 1.4f).toInt()

                    // Damped horizontal steering + banking roll
                    val targetX = LANE_POSITIONS[playerLane]
                    val prevX = playerX
                    val steerDamp = 1f - exp(-15f * dt)
                    playerX += (targetX - playerX) * steerDamp
                    val latVel = (playerX - prevX) / dt
                    playerRoll = (-latVel * 0.045f).coerceIn(-0.42f, 0.42f)

                    // Buffered Jump + Gravity
                    if (jumpBufferTimer > 0f && playerY <= 0.02f) {
                        playerVy = 8.6f
                        playerY = 0.03f
                        jumpBufferTimer = 0f
                        SoundEffectsEngine.playJumpWoosh()
                    }
                    if (playerY > 0f || playerVy > 0f) {
                        playerVy -= 23.5f * dt
                        playerY += playerVy * dt
                        if (playerY <= 0f) {
                            playerY = 0f
                            playerVy = 0f
                        }
                    }

                    // Spawn waves
                    spawnAccumulator += speed * dt
                    val spawnStep = (9.5f - elapsedSeconds * 0.06f).coerceIn(5.4f, 9.5f)
                    if (spawnAccumulator >= spawnStep) {
                        spawnAccumulator = 0f
                        val lane = Random.nextInt(0, 3)
                        val isCrystal = Random.nextFloat() < 0.48f
                        if (isCrystal) {
                            entities.add(ArenaEntity3D(laneIndex = lane, z = 32f, isCrystal = true))
                        } else {
                            val kind = if (Random.nextFloat() < 0.4f) ObstacleKind.LOW_BARRIER else ObstacleKind.TALL_FIREWALL
                            entities.add(ArenaEntity3D(laneIndex = lane, z = 32f, isCrystal = false, obstacleKind = kind))
                            // Fair companion crystal in an open lane
                            if (Random.nextFloat() < 0.65f) {
                                val safeLane = (lane + Random.nextInt(1, 3)) % 3
                                entities.add(ArenaEntity3D(laneIndex = safeLane, z = 32f, isCrystal = true))
                            }
                        }
                    }

                    // Advance entities & resolve collisions
                    val iter = entities.listIterator()
                    while (iter.hasNext()) {
                        val ent = iter.next()
                        ent.z -= speed * dt
                        val ex = LANE_POSITIONS[ent.laneIndex]
                        if (!ent.collectedOrHit && ent.z in 0.8f..2.6f && abs(ex - playerX) < 1.05f) {
                            if (ent.isCrystal) {
                                if (playerY < 1.8f) {
                                    ent.collectedOrHit = true
                                    crystals++
                                    score += 45 * combo
                                    combo = (combo + 1).coerceAtMost(8)
                                    if (combo > maxCombo) maxCombo = combo
                                    spawnBurst(ex, 0.7f, ent.z, Color(0xFF00E5FF), 10)
                                    SoundEffectsEngine.playPickupChime(combo)
                                }
                            } else {
                                val clearedLow = ent.obstacleKind == ObstacleKind.LOW_BARRIER && playerY > 1.05f
                                if (!clearedLow) {
                                    ent.collectedOrHit = true
                                    livesRemaining = (livesRemaining - 1).coerceAtLeast(0)
                                    combo = 1
                                    hitstopTimer = 0.055f
                                    cameraShakeTimer = 0.24f
                                    hitFlashTimer = 0.28f
                                    spawnBurst(playerX, playerY + 0.5f, 1.8f, Color(0xFFFF3D7F), 14)
                                    SoundEffectsEngine.playImpactThud()
                                    if (livesRemaining <= 0) {
                                        playPhase = GamePlayPhase.GAME_OVER
                                        if (!scoreSubmittedThisRun) {
                                            scoreSubmittedThisRun = true
                                            onGameFinished(score, maxCombo, crystals, elapsedSeconds.toInt().coerceAtLeast(1), selectedFocusTech)
                                        }
                                    }
                                }
                            }
                        }
                        if (ent.z < -2.5f || ent.collectedOrHit) {
                            iter.remove()
                        }
                    }

                    // Update particles
                    val pIter = particles.listIterator()
                    while (pIter.hasNext()) {
                        val p = pIter.next()
                        p.x += p.vx * dt
                        p.y += p.vy * dt
                        p.z += p.vz * dt
                        p.vy -= 9.8f * dt
                        p.life -= dt
                        if (p.life <= 0f) pIter.remove()
                    }
                }
            }
        }
    }

    // Main full-viewport 3D Surface
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightingPreset.skyTop)
            .testTag("screen_game_and_scene_3d")
    ) {
        // Uninterrupted 3D Canvas Viewport
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(viewportMode, playPhase) {
                    if (viewportMode == GameViewportMode.SCENE_STUDIO_3D) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            autoRotateShowcase = false
                            orbitYawTarget -= dragAmount.x * 0.0085f
                            orbitPitchTarget = (orbitPitchTarget + dragAmount.y * 0.007f).coerceIn(0.08f, 1.15f)
                        }
                    } else if (playPhase == GamePlayPhase.PLAYING) {
                        detectTapGestures { offset ->
                            when {
                                offset.y < size.height * 0.45f -> triggerJump()
                                offset.x < size.width * 0.42f -> moveLane(-1)
                                offset.x > size.width * 0.58f -> moveLane(1)
                                else -> triggerJump()
                            }
                        }
                    }
                }
        ) {
            if (viewportMode == GameViewportMode.SCENE_STUDIO_3D) {
                val eyeX = sin(orbitYawCurrent) * cos(orbitPitchCurrent) * orbitDistance
                val eyeY = (sin(orbitPitchCurrent) * orbitDistance).coerceAtLeast(0.75f) + 0.6f
                val eyeZ = -cos(orbitYawCurrent) * cos(orbitPitchCurrent) * orbitDistance
                val cam = Camera3D(
                    position = Vec3(eyeX, eyeY, eyeZ),
                    target = Vec3(0f, 1.0f, 0f),
                    fovScale = 1.6f
                )
                val faces = when (showcaseModel) {
                    ShowcaseModelType.DEVRUNNER_CRAFT -> buildDevRunnerCraftFaces(
                        worldPos = Vec3(0f, 0.45f + 0.12f * sin(globalTime * 2f), 0f),
                        rollRad = 0.08f * sin(globalTime * 1.5f),
                        yawRad = 0f,
                        hullAccent = selectedFocusTech.accentColor
                    )
                    ShowcaseModelType.DEVBOT_EXPLORER -> buildDevBotExplorerFaces(
                        worldPos = Vec3(0f, 0.1f + 0.1f * sin(globalTime * 2.2f), 0f),
                        yawRad = 0f
                    )
                    ShowcaseModelType.SQL_CLUSTER_TOWER -> buildSqlClusterTowerFaces(
                        worldPos = Vec3(0f, 0f, 0f),
                        yawRad = 0f,
                        pulseTime = globalTime
                    )
                }
                renderScene3D(
                    faces = faces,
                    camera = cam,
                    preset = lightingPreset,
                    wireframeOnly = wireframeMode,
                    gridScrollZ = 0f,
                    showLaneGuides = false
                )
            } else {
                // Arena Game Camera (title showcase vs trailing chase cam)
                val shakeX = if (cameraShakeTimer > 0f) (Random.nextFloat() - 0.5f) * 0.35f else 0f
                val shakeY = if (cameraShakeTimer > 0f) (Random.nextFloat() - 0.5f) * 0.25f else 0f
                val cam = if (playPhase == GamePlayPhase.TITLE) {
                    val panX = sin(globalTime * 0.5f) * 2.8f
                    Camera3D(
                        position = Vec3(panX, 3.2f, -4.6f),
                        target = Vec3(0f, 0.8f, 3.0f),
                        fovScale = 1.6f
                    )
                } else {
                    Camera3D(
                        position = Vec3(playerX * 0.52f + shakeX, 3.55f + shakeY, -4.2f),
                        target = Vec3(playerX * 0.72f, 0.8f, 8.5f),
                        fovScale = 1.65f
                    )
                }

                val sceneFaces = ArrayList<Face3D>(140)

                // Player composite 8-part DevRunner Craft
                val craftPos = if (playPhase == GamePlayPhase.TITLE) {
                    Vec3(0f, 0.45f + 0.15f * sin(globalTime * 2.4f), 2.0f)
                } else {
                    Vec3(playerX, playerY, 1.8f)
                }
                val craftYaw = if (playPhase == GamePlayPhase.TITLE) sin(globalTime * 0.6f) * 0.35f else 0f
                val flashAlpha = (hitFlashTimer / 0.28f).coerceIn(0f, 1f)
                sceneFaces.addAll(
                    buildDevRunnerCraftFaces(
                        worldPos = craftPos,
                        rollRad = playerRoll,
                        yawRad = craftYaw,
                        hullAccent = selectedFocusTech.accentColor,
                        hitFlash = flashAlpha
                    )
                )

                // Arena Entities
                for (ent in entities) {
                    val ex = LANE_POSITIONS[ent.laneIndex]
                    if (ent.isCrystal) {
                        sceneFaces.addCrystalGem(
                            center = Vec3(ex, 0.75f + 0.15f * sin(globalTime * 4f + ent.z), ent.z),
                            radius = 0.44f,
                            height = 0.92f,
                            spinRad = globalTime * 3.2f + ent.z,
                            color = Color(0xFF00E5FF)
                        )
                    } else if (ent.obstacleKind == ObstacleKind.LOW_BARRIER) {
                        sceneFaces.addBoxPart(
                            center = Vec3(ex, 0.36f, ent.z),
                            halfExtents = Vec3(0.88f, 0.34f, 0.28f),
                            color = Color(0xFFFF9100),
                            shininess = 0.55f
                        )
                        sceneFaces.addBoxPart(
                            center = Vec3(ex, 0.75f, ent.z),
                            halfExtents = Vec3(0.92f, 0.08f, 0.30f),
                            color = Color(0xFFFFEA00),
                            emissive = 0.5f + 0.3f * sin(globalTime * 8f)
                        )
                    } else {
                        sceneFaces.addBoxPart(
                            center = Vec3(ex, 1.05f, ent.z),
                            halfExtents = Vec3(0.58f, 1.05f, 0.45f),
                            color = Color(0xFFD81B60),
                            shininess = 0.6f
                        )
                        sceneFaces.addBoxPart(
                            center = Vec3(ex, 2.22f, ent.z),
                            halfExtents = Vec3(0.42f, 0.14f, 0.36f),
                            color = Color(0xFFFF5252),
                            emissive = 0.55f + 0.35f * sin(globalTime * 9f)
                        )
                    }
                }

                // 3D Particle cubes
                for (p in particles) {
                    val sz = 0.14f * (p.life / p.maxLife).coerceIn(0.2f, 1f)
                    sceneFaces.addBoxPart(
                        center = Vec3(p.x, p.y.coerceAtLeast(0.08f), p.z),
                        halfExtents = Vec3(sz, sz, sz),
                        color = p.color,
                        emissive = 0.8f
                    )
                }

                renderScene3D(
                    faces = sceneFaces,
                    camera = cam,
                    preset = lightingPreset,
                    wireframeOnly = false,
                    gridScrollZ = worldScrollZ,
                    showLaneGuides = true
                )
            }
        }

        // Top HUD Strip (compact perimeter UI, <= 12% viewport height, no backdrop-filter)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xCC121420),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_exit_game_3d")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isPt) "Sair do 3D" else "Exit 3D",
                            tint = Color.White
                        )
                    }
                }

                if (viewportMode == GameViewportMode.ARENA_GAME && playPhase == GamePlayPhase.PLAYING) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xD9121420),
                        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${score} pts",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "×$combo",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "💎$crystals",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(3) { idx ->
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = if (idx < livesRemaining) RedAccent else Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xD9121420),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                    ) {
                        Text(
                            text = if (viewportMode == GameViewportMode.ARENA_GAME) "DEVCORE 3D"
                            else if (isPt) "ESTÚDIO 3D" else "3D STUDIO",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Mute toggle
                Surface(
                    shape = CircleShape,
                    color = Color(0xCC121420),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            SoundEffectsEngine.isMuted = isMuted
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_toggle_3d_audio")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isPt) "Alternar Som" else "Toggle Sound",
                            tint = Color.White
                        )
                    }
                }

                // Switch between Arena Game and 3D Scene Studio (or Pause during play)
                if (viewportMode == GameViewportMode.ARENA_GAME && playPhase == GamePlayPhase.PLAYING) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xCC121420),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        IconButton(
                            onClick = { playPhase = GamePlayPhase.PAUSED },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("btn_pause_3d_game")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = if (isPt) "Pausar" else "Pause",
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xD9121420),
                        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.55f)),
                        onClick = {
                            viewportMode = if (viewportMode == GameViewportMode.ARENA_GAME) {
                                GameViewportMode.SCENE_STUDIO_3D
                            } else {
                                GameViewportMode.ARENA_GAME
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_switch_3d_mode")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (viewportMode == GameViewportMode.ARENA_GAME) Icons.Default.ViewInAr else Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewportMode == GameViewportMode.ARENA_GAME) {
                                    if (isPt) "Cena 3D" else "3D Scene"
                                } else {
                                    if (isPt) "Jogar" else "Game"
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bottom Controls during Active Play (compact edge buttons leaving >=85% center unobstructed)
        if (viewportMode == GameViewportMode.ARENA_GAME && playPhase == GamePlayPhase.PLAYING) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xBF151828),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        IconButton(
                            onClick = { moveLane(-1) },
                            modifier = Modifier.size(56.dp).testTag("btn_3d_left")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xBF151828),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        IconButton(
                            onClick = { moveLane(1) },
                            modifier = Modifier.size(56.dp).testTag("btn_3d_right")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = PurplePrimary.copy(alpha = 0.88f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    IconButton(
                        onClick = { triggerJump() },
                        modifier = Modifier.size(62.dp).testTag("btn_3d_jump")
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Jump", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // Title Screen Overlay (Clean calm composition + CTA + single control hint line)
        if (viewportMode == GameViewportMode.ARENA_GAME && playPhase == GamePlayPhase.TITLE) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xD9121420),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f))
                ) {
                    Text(
                        text = if (isPt) "RECORDE SQL: $bestScore PTS" else "SQL BEST: $bestScore PTS",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "DEVCORE 3D RUNNER",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = { resetAndStartGame() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .heightIn(min = 52.dp)
                        .testTag("btn_start_3d_game")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "INICIAR JOGO 3D" else "START 3D GAME",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = if (isPt) "Toque Esquerda/Direita Mover · Centro/Botão Pular · Colete Cristais 💎"
                    else "Tap Left/Right Steer · Center/Button Jump · Collect Data Crystals 💎",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Pause or Game Over Modal
        if (viewportMode == GameViewportMode.ARENA_GAME && (playPhase == GamePlayPhase.PAUSED || playPhase == GamePlayPhase.GAME_OVER)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99080910)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xF2151726),
                    border = BorderStroke(1.5.dp, PurplePrimary.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .padding(16.dp)
                        .testTag("modal_3d_game_state")
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = when (playPhase) {
                                GamePlayPhase.PAUSED -> if (isPt) "JOGO PAUSADO" else "GAME PAUSED"
                                else -> if (isPt) "FIM DE MISSÃO 3D" else "MISSION COMPLETE"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$score", color = Color(0xFF00E5FF), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                Text(if (isPt) "Pontos" else "Score", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("×$maxCombo", color = RedAccent, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                Text("Combo Max", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$crystals", color = Color(0xFFFFD54F), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                Text(if (isPt) "Cristais" else "Crystals", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            }
                        }

                        if (playPhase == GamePlayPhase.GAME_OVER) {
                            Text(
                                text = if (isPt) "Pontuação salva no Banco SQL e XP adicionado ao seu perfil!"
                                else "Score saved to SQL Database and XP awarded to your profile!",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (playPhase == GamePlayPhase.PAUSED) {
                            Button(
                                onClick = { playPhase = GamePlayPhase.PLAYING },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Text(if (isPt) "Continuar" else "Resume", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { resetAndStartGame() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("btn_restart_3d_game")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPt) "Jogar Novamente" else "Play Again", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewportMode = GameViewportMode.SCENE_STUDIO_3D
                                playPhase = GamePlayPhase.TITLE
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Text(if (isPt) "Explorar Cena 3D" else "Open 3D Scene Studio", color = Color.White)
                        }
                    }
                }
            }
        }

        // Collapsible Compact Control Dock for 3D Scene Showcase Mode
        if (viewportMode == GameViewportMode.SCENE_STUDIO_3D) {
            Surface(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = Color(0xE6141624),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .testTag("dock_3d_studio_controls")
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPt) "${showcaseModel.titlePt} (${showcaseModel.partCount} partes)"
                                else "${showcaseModel.titleEn} (${showcaseModel.partCount} parts)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isPt) "Arraste a tela para orbitar 360°" else "Drag canvas to orbit 360°",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                        }

                        IconButton(
                            onClick = { studioDockExpanded = !studioDockExpanded },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_toggle_3d_dock")
                        ) {
                            Icon(
                                imageVector = if (studioDockExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isPt) "Expandir Controles 3D" else "Toggle 3D Controls",
                                tint = Color.White
                            )
                        }
                    }

                    AnimatedVisibility(visible = studioDockExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ShowcaseModelType.entries.forEach { model ->
                                    FilterChip(
                                        selected = showcaseModel == model,
                                        onClick = { showcaseModel = model },
                                        label = {
                                            Text(
                                                text = if (isPt) model.titlePt else model.titleEn,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PurplePrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                LightingPreset.entries.forEach { preset ->
                                    FilterChip(
                                        selected = lightingPreset == preset,
                                        onClick = { lightingPreset = preset },
                                        label = { Text(if (isPt) preset.labelPt else preset.labelEn, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RedAccent,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                                FilterChip(
                                    selected = wireframeMode,
                                    onClick = { wireframeMode = !wireframeMode },
                                    label = { Text("Wireframe", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = autoRotateShowcase,
                                    onClick = { autoRotateShowcase = !autoRotateShowcase },
                                    label = { Text(if (isPt) "Auto-Giro" else "Auto-Orbit", fontSize = 11.sp) }
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Zoom", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Slider(
                                    value = orbitDistance,
                                    onValueChange = { orbitDistance = it },
                                    valueRange = 4.2f..9.5f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF)),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
