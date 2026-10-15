package com.example.ludo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.R
import com.example.ludo.model.PlayerColor
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle

@Composable
fun MainMenuScreen(
    onStartGame: (playerCount: Int, botCount: Int) -> Unit
) {
    var playerCount by remember { mutableIntStateOf(2) }
    var isVsAiMode by remember { mutableStateOf(true) }

    val botCount = if (isVsAiMode) playerCount - 1 else 0
    val humanCount = playerCount - botCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Authentic Game Felt Background
        Image(
            painter = painterResource(id = R.drawable.ludo_king_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x990A1325))
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val isLandscape = maxWidth > maxHeight
            val cardWidth = if (isLandscape) 520.dp else (maxWidth * 0.92f).coerceAtMost(460.dp)

            Card(
                modifier = Modifier
                    .width(cardWidth)
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .shadow(16.dp, RoundedCornerShape(26.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xF00D1B36)),
                border = androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFFFFD54F)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Game Logo / Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x66000000),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xAAFFD54F))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                VectorDiceIcon(size = if (isLandscape) 26.dp else 22.dp)
                                Text(
                                    text = "LUDO SUPREME",
                                    fontSize = if (isLandscape) 24.sp else 21.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.1.sp,
                                    color = Color(0xFFFFEA00),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Text(
                            text = "Authentic Indian Board Game",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Active Player Color Tokens Preview
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            PlayerColor.RED,
                            PlayerColor.GREEN,
                            PlayerColor.YELLOW,
                            PlayerColor.BLUE
                        ).take(playerCount).forEach { pc ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .shadow(6.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color.White, pc.primaryColor)
                                        )
                                    )
                                    .border(2.dp, Color(0xFFFFD54F), CircleShape)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color(0x44FFD54F),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // 1. Player Count Selection
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "NUMBER OF PLAYERS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color(0xFFFFD54F)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                val isSelected = playerCount == count
                                Box(modifier = Modifier.weight(1f)) {
                                    GameButton(
                                        text = "$count Players",
                                        onClick = { playerCount = count },
                                        style = if (isSelected) GameButtonStyle.GOLD else GameButtonStyle.DARK,
                                        isSelected = isSelected,
                                        fontSize = 12,
                                        height = 46.dp,
                                        cornerRadius = 10.dp,
                                        horizontalPadding = 4.dp
                                    )
                                }
                            }
                        }
                    }

                    // 2. Mode Selection (Pass & Play vs AI Bots)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val botSuffix = if (botCount == 1) "Bot" else "Bots"
                        Text(
                            text = "GAME MODE ($humanCount Human vs $botCount $botSuffix)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color(0xFFFFD54F)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                GameButton(
                                    text = "Pass & Play",
                                    onClick = { isVsAiMode = false },
                                    style = if (!isVsAiMode) GameButtonStyle.BLUE else GameButtonStyle.DARK,
                                    isSelected = !isVsAiMode,
                                    fontSize = 13,
                                    height = 46.dp,
                                    cornerRadius = 10.dp
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                GameButton(
                                    text = "vs AI Bots",
                                    onClick = { isVsAiMode = true },
                                    style = if (isVsAiMode) GameButtonStyle.BLUE else GameButtonStyle.DARK,
                                    isSelected = isVsAiMode,
                                    fontSize = 13,
                                    height = 46.dp,
                                    cornerRadius = 10.dp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. Huge Tactile "START GAME" Button
                    GameButton(
                        text = "START GAME",
                        onClick = { onStartGame(playerCount, botCount) },
                        style = GameButtonStyle.GREEN,
                        fontSize = 18,
                        height = 54.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Procedural Vector Dice Icon for titles and branding (no emojis).
 */
@Composable
fun VectorDiceIcon(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 26.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cr = w * 0.22f

        // 3D Bevel Rim
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.White, Color(0xFFFFD54F), Color(0xFFC49A28)),
                start = Offset.Zero,
                end = Offset(w, h)
            ),
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(cr, cr)
        )

        // Porcelain Face
        val pad = w * 0.08f
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
                center = Offset(w * 0.35f, h * 0.35f),
                radius = w * 0.8f
            ),
            topLeft = Offset(pad, pad),
            size = Size(w - pad * 2, h - pad * 2),
            cornerRadius = CornerRadius(cr * 0.8f, cr * 0.8f)
        )

        // 5 Pips (Center striking ruby red, 4 corner obsidian black)
        val pr = w * 0.09f
        val center = w * 0.5f
        val left = w * 0.30f
        val right = w * 0.70f
        val top = h * 0.30f
        val bottom = h * 0.70f

        // Center red pip
        drawCircle(Color(0xFFFF3D00), pr * 1.25f, Offset(center, center))
        drawCircle(Color(0xCCFFFFFF), pr * 0.4f, Offset(center - pr * 0.3f, center - pr * 0.3f))

        // 4 corner dark pips
        listOf(
            Offset(left, top),
            Offset(right, top),
            Offset(left, bottom),
            Offset(right, bottom)
        ).forEach { pos ->
            drawCircle(Color(0xFF0F172A), pr, pos)
            drawCircle(Color(0xCCFFFFFF), pr * 0.3f, Offset(pos.x - pr * 0.3f, pos.y - pr * 0.3f))
        }
    }
}
