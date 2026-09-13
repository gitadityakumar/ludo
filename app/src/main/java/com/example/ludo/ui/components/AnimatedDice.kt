package com.example.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Fast, snappy 2.5D pseudo-3D tumbling dice component with camera perspective,
 * depth face shading, rapid motion blur cycling, impact snap, and overshoot bounce easing.
 *
 * Total duration: ~400ms (snappy 350ms–450ms range).
 * - Phase 1: Tumbling & Blur (~270ms): 3D perspective transform with camera distance,
 *   rapid X/Y rotations with Z wobble and dynamic depth face shading, rapid face randomizing every 35ms.
 * - Phase 2: Impact & Settle (~130ms): abrupt flat snap onto targetValue, scale impact dip to 0.90x,
 *   overshoot to 1.06x, and settle to 1.0x.
 */
@Composable
fun AnimatedDice(
    targetValue: Int,
    diceColor: Color = Color.White,
    isRolling: Boolean,
    onRollComplete: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    enabled: Boolean = true,
    playerAccentColor: Color = Color(0xFFFFD54F),
    onRollClick: () -> Unit = {}
) {
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val rotZ = remember { Animatable(0f) }
    val scale = remember { Animatable(1.0f) }
    val depthShading = remember { Animatable(0f) }
    val tossHop = remember { Animatable(0f) }

    var displayedFace by remember { mutableIntStateOf(targetValue.coerceIn(1, 6)) }
    var isAnimationActive by remember { mutableStateOf(false) }

    // Gentle breathing pulse animation when waiting for human roll
    val infiniteTransition = rememberInfiniteTransition(label = "dice_idle")
    val waitingPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (enabled && !isRolling && !isAnimationActive) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waiting_pulse"
    )

    // Execute the snappy 2-Phase 2.5D tumbling animation when isRolling is triggered
    LaunchedEffect(isRolling, targetValue) {
        if (isRolling) {
            if (isAnimationActive) return@LaunchedEffect
            isAnimationActive = true

            // Reset initial values
            rotX.snapTo(0f)
            rotY.snapTo(0f)
            rotZ.snapTo(0f)
            scale.snapTo(1.0f)
            depthShading.snapTo(0f)
            tossHop.snapTo(0f)

            // --- Phase 1: Tumbling & Blur (~270ms) ---
            // 1. Rapidly cycle faces every 35ms to simulate high-speed rotation & motion blur
            val faceCycleJob = launch {
                while (true) {
                    displayedFace = Random.nextInt(1, 7)
                    delay(35L)
                }
            }

            // 2. 3D perspective tumbling (rotX 720°, rotY 540°, Z-axis wobble)
            val rotXJob = launch {
                rotX.animateTo(
                    targetValue = 720f,
                    animationSpec = tween(durationMillis = 270, easing = LinearEasing)
                )
            }
            val rotYJob = launch {
                rotY.animateTo(
                    targetValue = 540f,
                    animationSpec = tween(durationMillis = 270, easing = LinearEasing)
                )
            }
            val rotZJob = launch {
                rotZ.animateTo(14f, tween(68, easing = FastOutSlowInEasing))
                rotZ.animateTo(-14f, tween(68, easing = FastOutSlowInEasing))
                rotZ.animateTo(8f, tween(68, easing = FastOutSlowInEasing))
                rotZ.animateTo(0f, tween(66, easing = FastOutSlowInEasing))
            }
            val hopJob = launch {
                tossHop.animateTo(-12f, tween(135, easing = FastOutSlowInEasing))
                tossHop.animateTo(0f, tween(135, easing = FastOutLinearInEasing))
            }
            val depthJob = launch {
                depthShading.animateTo(1f, tween(135, easing = FastOutSlowInEasing))
                depthShading.animateTo(0.2f, tween(135, easing = FastOutSlowInEasing))
            }

            // Wait for Phase 1 completion
            delay(270L)
            faceCycleJob.cancel()
            rotXJob.cancel()
            rotYJob.cancel()
            rotZJob.cancel()
            hopJob.cancel()
            depthJob.cancel()

            // --- Phase 2: Impact & Settle (~130ms) ---
            // Abruptly snap flat onto target rolled value
            rotX.snapTo(0f)
            rotY.snapTo(0f)
            rotZ.snapTo(0f)
            tossHop.snapTo(0f)
            depthShading.snapTo(0f)
            displayedFace = targetValue.coerceIn(1, 6)

            // Overshoot/bounce easing curve
            // Scale dips to ~0.90x on impact
            scale.animateTo(0.90f, animationSpec = tween(durationMillis = 35, easing = FastOutLinearInEasing))
            // Overshoots to ~1.06x
            scale.animateTo(1.06f, animationSpec = tween(durationMillis = 55, easing = LinearOutSlowInEasing))
            // Settles back to 1.0x
            scale.animateTo(1.0f, animationSpec = tween(durationMillis = 40, easing = FastOutSlowInEasing))

            isAnimationActive = false
            onRollComplete(targetValue)
        } else {
            isAnimationActive = false
            displayedFace = targetValue.coerceIn(1, 6)
            rotX.snapTo(0f)
            rotY.snapTo(0f)
            rotZ.snapTo(0f)
            scale.snapTo(1.0f)
            depthShading.snapTo(0f)
            tossHop.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                enabled = enabled && !isRolling && !isAnimationActive,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onRollClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ground Elevation Shadow (shrinks/fades during hop, darkens on impact)
        Canvas(
            modifier = Modifier
                .size(size * 0.90f, size * 0.32f)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    val s = if (isAnimationActive) 0.72f - (tossHop.value / 60f) else 1.0f
                    scaleX = s.coerceIn(0.6f, 1.1f)
                    scaleY = s.coerceIn(0.6f, 1.1f)
                    alpha = if (isAnimationActive) (0.25f + (tossHop.value / 40f)).coerceIn(0.12f, 0.45f) else 0.55f
                    translationY = -2.dp.toPx()
                }
        ) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99000000), Color(0x00000000)),
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                    radius = this.size.width / 2f
                )
            )
        }

        // Pseudo-3D (2.5D) Tumbling Cube with Camera Distance
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .graphicsLayer {
                    cameraDistance = 16f * density
                    rotationX = rotX.value
                    rotationY = rotY.value
                    rotationZ = rotZ.value
                    translationY = tossHop.value.dp.toPx()
                    val s = scale.value * (if (!isAnimationActive && !isRolling) waitingPulse else 1f)
                    scaleX = s
                    scaleY = s
                },
            contentAlignment = Alignment.Center
        ) {
            // High-Definition Canvas-rendered 3D Dice Face
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val cornerRadius = w * 0.22f
                val dShade = depthShading.value

                // 2.5D Simulated Depth Edges (visible when tumbling)
                if (dShade > 0.05f) {
                    drawRoundRect(
                        color = Color(0xFF64748B).copy(alpha = 0.5f * dShade),
                        topLeft = Offset(w * 0.08f, h * 0.08f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }

                // Outer 3D Bevel Rim (Metallic tactile edge)
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFE2E8F0),
                            playerAccentColor.copy(alpha = 0.85f),
                            Color(0xFF94A3B8)
                        ),
                        start = Offset.Zero,
                        end = Offset(w, h)
                    ),
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )

                // Inner Glossy Porcelain Surface with diceColor
                val borderPad = w * 0.045f
                val baseFill = if (dShade > 0.1f) {
                    Color(0xFFF1F5F9)
                } else {
                    diceColor
                }

                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(baseFill, Color(0xFFF8FAFC), Color(0xFFE2E8F0)),
                        center = Offset(w * 0.35f, h * 0.35f),
                        radius = w * 0.85f
                    ),
                    topLeft = Offset(borderPad, borderPad),
                    size = Size(w - borderPad * 2, h - borderPad * 2),
                    cornerRadius = CornerRadius(cornerRadius * 0.85f, cornerRadius * 0.85f)
                )

                // Top-Left Gloss Specular Sheen
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xBBFFFFFF), Color(0x00FFFFFF)),
                        start = Offset(borderPad, borderPad),
                        end = Offset(w * 0.65f, h * 0.45f)
                    ),
                    topLeft = Offset(borderPad, borderPad),
                    size = Size((w - borderPad * 2) * 0.92f, (h - borderPad * 2) * 0.42f),
                    cornerRadius = CornerRadius(cornerRadius * 0.75f, cornerRadius * 0.75f)
                )

                // Deep, Large, High-Contrast Pips
                drawLargeDicePips(displayedFace, w, h)
            }
        }

        // Crystal-Clear Result Number Badge (floating gold pill)
        if (!isRolling && !isAnimationActive && targetValue in 1..6) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-4).dp)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F))
            ) {
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$targetValue",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = if (targetValue == 6) Color(0xFFFF5252) else Color(0xFFFFD54F)
                    )
                }
            }
        }
    }
}

/**
 * Draws prominent, high-contrast, deeply embossed pips.
 * 1 is a bold ruby red pip.
 * 2..6 are large, bold obsidian black pips with inner radial shadow and specular shine.
 */
private fun DrawScope.drawLargeDicePips(value: Int, w: Float, h: Float) {
    val pipRadius = w * 0.105f
    val center = w * 0.5f
    val mid = h * 0.5f

    val left = w * 0.27f
    val right = w * 0.73f
    val top = h * 0.27f
    val bottom = h * 0.73f

    fun drawBlackPip(cx: Float, cy: Float) {
        // Deep inset cavity shadow
        drawCircle(
            color = Color(0x33000000),
            radius = pipRadius * 1.18f,
            center = Offset(cx, cy + pipRadius * 0.12f)
        )
        // Main pip body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF334155), Color(0xFF090D16)),
                center = Offset(cx - pipRadius * 0.15f, cy - pipRadius * 0.15f),
                radius = pipRadius
            ),
            radius = pipRadius,
            center = Offset(cx, cy)
        )
        // Specular highlight gloss dot
        drawCircle(
            color = Color(0xCCFFFFFF),
            radius = pipRadius * 0.30f,
            center = Offset(cx - pipRadius * 0.32f, cy - pipRadius * 0.32f)
        )
    }

    when (value) {
        1 -> {
            // Massive striking ruby red pip
            val rubyRadius = pipRadius * 1.65f
            drawCircle(
                color = Color(0x44000000),
                radius = rubyRadius * 1.15f,
                center = Offset(center, mid + rubyRadius * 0.1f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF5252), Color(0xFFDC2626), Color(0xFF7F1D1D)),
                    center = Offset(center - rubyRadius * 0.2f, mid - rubyRadius * 0.2f),
                    radius = rubyRadius
                ),
                radius = rubyRadius,
                center = Offset(center, mid)
            )
            drawCircle(
                color = Color(0xDDFFFFFF),
                radius = rubyRadius * 0.32f,
                center = Offset(center - rubyRadius * 0.30f, mid - rubyRadius * 0.30f)
            )
        }
        2 -> {
            drawBlackPip(left, top)
            drawBlackPip(right, bottom)
        }
        3 -> {
            drawBlackPip(left, top)
            drawBlackPip(center, mid)
            drawBlackPip(right, bottom)
        }
        4 -> {
            drawBlackPip(left, top)
            drawBlackPip(right, top)
            drawBlackPip(left, bottom)
            drawBlackPip(right, bottom)
        }
        5 -> {
            drawBlackPip(left, top)
            drawBlackPip(right, top)
            drawBlackPip(center, mid)
            drawBlackPip(left, bottom)
            drawBlackPip(right, bottom)
        }
        6 -> {
            // 6 bold pips
            drawBlackPip(left, top)
            drawBlackPip(left, mid)
            drawBlackPip(left, bottom)
            drawBlackPip(right, top)
            drawBlackPip(right, mid)
            drawBlackPip(right, bottom)
        }
    }
}
