package com.example.ludo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GameButtonStyle {
    GOLD,
    GREEN,
    RED,
    BLUE,
    DARK
}

data class GameButtonColors(
    val topGradient: List<Color>,
    val bottomBevel: Color,
    val border: Color,
    val textColor: Color
)

fun getGameButtonColors(style: GameButtonStyle): GameButtonColors {
    return when (style) {
        GameButtonStyle.GOLD -> GameButtonColors(
            topGradient = listOf(Color(0xFFFFEA00), Color(0xFFFFB300), Color(0xFFFF8F00)),
            bottomBevel = Color(0xFFC46500),
            border = Color(0xFFFFF9C4),
            textColor = Color(0xFF3E2723)
        )
        GameButtonStyle.GREEN -> GameButtonColors(
            topGradient = listOf(Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF16A34A)),
            bottomBevel = Color(0xFF14532D),
            border = Color(0xFFBBF7D0),
            textColor = Color.White
        )
        GameButtonStyle.RED -> GameButtonColors(
            topGradient = listOf(Color(0xFFF87171), Color(0xFFEF4444), Color(0xFFDC2626)),
            bottomBevel = Color(0xFF7F1D1D),
            border = Color(0xFFFECACA),
            textColor = Color.White
        )
        GameButtonStyle.BLUE -> GameButtonColors(
            topGradient = listOf(Color(0xFF60A5FA), Color(0xFF3B82F6), Color(0xFF2563EB)),
            bottomBevel = Color(0xFF1E3A8A),
            border = Color(0xFFBFDBFE),
            textColor = Color.White
        )
        GameButtonStyle.DARK -> GameButtonColors(
            topGradient = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
            bottomBevel = Color(0xFF020617),
            border = Color(0xFF64748B),
            textColor = Color(0xFFE2E8F0)
        )
    }
}

/**
 * 3D Tactile Arcade Push Button.
 * Simulates real depth extrusion that compresses downward when tapped.
 */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GameButtonStyle = GameButtonStyle.GOLD,
    fontSize: Int = 16,
    height: Dp = 48.dp,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val colors = getGameButtonColors(style)
    val pressOffset = if (isPressed) 3.dp else 0.dp
    val bevelDepth = 4.dp
    val cornerRadius = 14.dp

    Box(
        modifier = modifier
            .height(height + bevelDepth)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Bottom 3D shadow extrusion bevel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = bevelDepth)
                .clip(RoundedCornerShape(cornerRadius))
                .background(colors.bottomBevel)
        )

        // Top interactive button surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = pressOffset)
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = colors.topGradient
                    )
                )
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = if (isSelected) Color.White else colors.border,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textColor,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Circular 3D Game Icon Button for Back, Pause, Settings, Sound.
 */
@Composable
fun GameIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    style: GameButtonStyle = GameButtonStyle.GOLD,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val colors = getGameButtonColors(style)
    val pressOffset = if (isPressed) 2.5.dp else 0.dp
    val bevelDepth = 3.dp

    Box(
        modifier = modifier
            .size(size, size + bevelDepth)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow base
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = bevelDepth)
                .clip(CircleShape)
                .background(colors.bottomBevel)
        )

        // Top surface
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = pressOffset)
                .clip(CircleShape)
                .background(Brush.verticalGradient(colors.topGradient))
                .border(1.8.dp, colors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
