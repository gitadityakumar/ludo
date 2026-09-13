package com.example.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.Player
import com.example.ludo.model.PlayerColor

@Composable
fun LudoKingPlayerPod(
    player: Player,
    isActive: Boolean,
    isRolling: Boolean,
    canRoll: Boolean,
    diceValue: Int,
    onDiceClick: () -> Unit,
    modifier: Modifier = Modifier,
    diceOnRight: Boolean = true,
    onRollComplete: (Int) -> Unit = {}
) {
    // Horizontal arrow bounce animation pointing toward the dice
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_bounce")
    val arrowBounce by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_x"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // If dice is on Left, arrow is placed on the Left pointing Right (→)
        if (!diceOnRight && isActive && canRoll) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = (-arrowBounce).dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawHorizontalArrow(size.width, size.height, pointingLeft = false)
                }
            }
            Spacer(modifier = Modifier.width(3.dp))
        }

        // Main Golden-Bordered Double-Compartment Pod (Matching Ludo King exactly)
        Box(
            modifier = Modifier
                .shadow(
                    elevation = if (isActive) 10.dp else 2.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isActive) {
                            listOf(Color(0xFFFFEA00), Color(0xFFFFB300), Color(0xFFFFEA00))
                        } else {
                            listOf(Color(0xFFE5C158), Color(0xFFC49A28))
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F3E80))
                .clickable(
                    enabled = isActive && canRoll,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDiceClick
                )
                .padding(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (!diceOnRight) {
                    // Dice compartment on Left
                    DiceCompartment(
                        isActive = isActive,
                        isRolling = isRolling,
                        canRoll = canRoll,
                        diceValue = diceValue,
                        playerColor = player.color,
                        onDiceClick = onDiceClick,
                        onRollComplete = onRollComplete
                    )
                    // Pin badge on Right
                    PinBadgeCompartment(player = player, isActive = isActive)
                } else {
                    // Pin badge on Left
                    PinBadgeCompartment(player = player, isActive = isActive)
                    // Dice compartment on Right
                    DiceCompartment(
                        isActive = isActive,
                        isRolling = isRolling,
                        canRoll = canRoll,
                        diceValue = diceValue,
                        playerColor = player.color,
                        onDiceClick = onDiceClick,
                        onRollComplete = onRollComplete
                    )
                }
            }
        }

        // If dice is on Right, arrow is placed on the Right pointing Left (←)
        if (diceOnRight && isActive && canRoll) {
            Spacer(modifier = Modifier.width(3.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = arrowBounce.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawHorizontalArrow(size.width, size.height, pointingLeft = true)
                }
            }
        }
    }
}

/**
 * Dice Compartment:
 * When ACTIVE: Shows the white 3D dice.
 * When INACTIVE: Shows the exact empty recessed placeholder slot with pale pink/beige fill.
 */
@Composable
private fun DiceCompartment(
    isActive: Boolean,
    isRolling: Boolean,
    canRoll: Boolean,
    diceValue: Int,
    playerColor: PlayerColor,
    onDiceClick: () -> Unit,
    onRollComplete: (Int) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF6E2E2), Color(0xFFE4CACA))
                )
            )
            .border(1.5.dp, Color(0xFFA88A8A), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            // Active player's dice
            AnimatedDice(
                targetValue = diceValue,
                diceColor = Color.White,
                isRolling = isRolling,
                onRollComplete = onRollComplete,
                enabled = canRoll,
                playerAccentColor = playerColor.primaryColor,
                size = 54.dp,
                onRollClick = onDiceClick
            )
        } else {
            // Recessed slot inner shadow bevel (empty placeholder)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawLine(
                    color = Color(0x33000000),
                    start = Offset(2f, 2f),
                    end = Offset(w - 2f, 2f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color(0x33000000),
                    start = Offset(2f, 2f),
                    end = Offset(2f, h - 2f),
                    strokeWidth = 3f
                )
            }
        }
    }
}

/**
 * Player Pin Badge Compartment with player colored gradient and 3D pin pawn.
 */
@Composable
private fun PinBadgeCompartment(
    player: Player,
    isActive: Boolean
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0277BD), Color(0xFF0B306B))
                )
            )
            .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(38.dp)) {
            drawMiniPin(size.width, size.height, player.color)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniPin(
    w: Float,
    h: Float,
    color: PlayerColor
) {
    val cx = w * 0.5f
    val headRadius = w * 0.34f
    val headY = h * 0.38f
    val tipY = h * 0.92f

    val path = Path().apply {
        moveTo(cx, tipY)
        lineTo(cx - headRadius, headY)
        arcTo(
            rect = Rect(
                cx - headRadius, headY - headRadius,
                cx + headRadius, headY + headRadius
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        lineTo(cx, tipY)
        close()
    }

    // Chrome metallic body
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFCFD8DC)),
            start = Offset(cx - headRadius, headY - headRadius),
            end = Offset(cx + headRadius, tipY)
        )
    )
    drawPath(path, color = Color(0xFF263238), style = Stroke(width = 1.2f))

    // Inner colored circle
    drawCircle(
        color = color.primaryColor,
        radius = headRadius * 0.65f,
        center = Offset(cx, headY)
    )
    drawCircle(
        color = Color(0xFF1B2228),
        radius = headRadius * 0.65f,
        center = Offset(cx, headY),
        style = Stroke(width = 1f)
    )
}

/**
 * Draws the vibrant horizontal pointer arrow pointing directly toward the dice.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHorizontalArrow(
    w: Float,
    h: Float,
    pointingLeft: Boolean
) {
    val path = Path().apply {
        if (pointingLeft) {
            // Pointing Left (←)
            moveTo(w * 0.05f, h * 0.5f)
            lineTo(w * 0.60f, h * 0.10f)
            lineTo(w * 0.60f, h * 0.32f)
            lineTo(w * 0.95f, h * 0.32f)
            lineTo(w * 0.95f, h * 0.68f)
            lineTo(w * 0.60f, h * 0.68f)
            lineTo(w * 0.60f, h * 0.90f)
            close()
        } else {
            // Pointing Right (→)
            moveTo(w * 0.95f, h * 0.5f)
            lineTo(w * 0.40f, h * 0.10f)
            lineTo(w * 0.40f, h * 0.32f)
            lineTo(w * 0.05f, h * 0.32f)
            lineTo(w * 0.05f, h * 0.68f)
            lineTo(w * 0.40f, h * 0.68f)
            lineTo(w * 0.40f, h * 0.90f)
            close()
        }
    }

    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFEA00), Color(0xFFFF9100))
        ),
        style = Fill
    )
    drawPath(
        path = path,
        color = Color(0xFF212121),
        style = Stroke(width = 1.8f)
    )
}
