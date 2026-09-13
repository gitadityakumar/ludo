package com.example.ludo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ludo.R
import com.example.ludo.model.PlayerColor
import com.example.ludo.model.TurnPhase
import com.example.ludo.ui.components.*
import com.example.ludo.viewmodel.LudoViewModel

@Composable
fun GameScreen(
    viewModel: LudoViewModel,
    onBackToMenu: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val activePlayer = gameState.activePlayer
    var isMuted by remember { mutableStateOf(viewModel.soundManager.isMuted) }
    var showExitDialog by remember { mutableStateOf(false) }

    val redPlayer = gameState.players.find { it.color == PlayerColor.RED }
    val greenPlayer = gameState.players.find { it.color == PlayerColor.GREEN }
    val yellowPlayer = gameState.players.find { it.color == PlayerColor.YELLOW }
    val bluePlayer = gameState.players.find { it.color == PlayerColor.BLUE }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Authentic Ludo King Royal Blue Table Felt Background
        Image(
            painter = painterResource(id = R.drawable.ludo_king_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // ==========================================
                // LANDSCAPE TABLET LAYOUT: MAXIMIZE BOARD SIZE
                // ==========================================
                val sidePanelWidth = 175.dp
                val maxBoardH = maxHeight - 50.dp
                val maxBoardW = maxWidth - (sidePanelWidth * 2) - 36.dp
                val boardSize = min(maxBoardH, maxBoardW).coerceAtLeast(320.dp)

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT FLANK: Top-Left Red, Bottom-Left Blue
                    Column(
                        modifier = Modifier
                            .width(sidePanelWidth)
                            .height(boardSize),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Top Section: Back Button & Sound Toggle, then Red Player Pod directly beside Red House
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GameIconButton(
                                    onClick = { showExitDialog = true },
                                    size = 38.dp,
                                    style = GameButtonStyle.RED
                                ) {
                                    // Crisp Back Arrow (←)
                                    Canvas(modifier = Modifier.size(18.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        val path = Path().apply {
                                            moveTo(w * 0.7f, h * 0.18f)
                                            lineTo(w * 0.28f, h * 0.5f)
                                            lineTo(w * 0.7f, h * 0.82f)
                                        }
                                        drawPath(path, Color.White, style = Stroke(width = 4.5f))
                                        drawLine(Color.White, Offset(w * 0.32f, h * 0.5f), Offset(w * 0.85f, h * 0.5f), strokeWidth = 4.5f)
                                    }
                                }

                                GameIconButton(
                                    onClick = {
                                        viewModel.toggleMute()
                                        isMuted = viewModel.soundManager.isMuted
                                    },
                                    size = 38.dp,
                                    style = GameButtonStyle.GOLD
                                ) {
                                    VectorSpeakerIcon(isMuted = isMuted, size = 18.dp)
                                }
                            }

                            // Red Player Pod (Aligned directly with Red Home at Top-Left of Board!)
                            if (redPlayer != null) {
                                LudoKingPlayerPod(
                                    player = redPlayer,
                                    isActive = redPlayer.id == activePlayer.id,
                                    isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE,
                                    canRoll = gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL && !redPlayer.isBot,
                                    diceValue = gameState.diceValue,
                                    onDiceClick = { viewModel.onDiceClicked() },
                                    diceOnRight = true,
                                    onRollComplete = { dice -> viewModel.onDiceRollComplete(dice) }
                                )
                            }
                        }

                        // Blue Player Pod (Aligned with Blue Home at Bottom-Left of Board)
                        if (bluePlayer != null) {
                            LudoKingPlayerPod(
                                player = bluePlayer,
                                isActive = bluePlayer.id == activePlayer.id,
                                isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE,
                                canRoll = gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL && !bluePlayer.isBot,
                                diceValue = gameState.diceValue,
                                onDiceClick = { viewModel.onDiceClicked() },
                                diceOnRight = true,
                                onRollComplete = { dice -> viewModel.onDiceRollComplete(dice) }
                            )
                        } else {
                            Spacer(modifier = Modifier.height(1.dp))
                        }
                    }

                    // CENTER: Maximized Ludo Board + Subtle Status Pill
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(boardSize)
                                .clip(RoundedCornerShape(6.dp))
                                .shadow(8.dp, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            LudoBoardView(
                                players = gameState.players,
                                activePlayer = activePlayer,
                                legalMoves = gameState.legalMoves,
                                onTokenClicked = { token -> viewModel.onTokenClicked(token) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Status Banner below board
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xDD0A192F),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFD54F))
                        ) {
                            Text(
                                text = gameState.statusMessage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD54F),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // RIGHT FLANK: Top-Right Green, Bottom-Right Yellow
                    Column(
                        modifier = Modifier
                            .width(sidePanelWidth)
                            .height(boardSize),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Top Section: Symmetrical spacer matching controls height + Green Player Pod
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Spacer(modifier = Modifier.height(38.dp))

                            // Green Player Pod (Aligned directly with Green Home at Top-Right of Board)
                            if (greenPlayer != null) {
                                LudoKingPlayerPod(
                                    player = greenPlayer,
                                    isActive = greenPlayer.id == activePlayer.id,
                                    isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE,
                                    canRoll = gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL && !greenPlayer.isBot,
                                    diceValue = gameState.diceValue,
                                    onDiceClick = { viewModel.onDiceClicked() },
                                    diceOnRight = false,
                                    onRollComplete = { dice -> viewModel.onDiceRollComplete(dice) }
                                )
                            }
                        }

                        // Yellow Player Pod (Aligned with Yellow Home at Bottom-Right of Board)
                        if (yellowPlayer != null) {
                            LudoKingPlayerPod(
                                player = yellowPlayer,
                                isActive = yellowPlayer.id == activePlayer.id,
                                isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE,
                                canRoll = gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL && !yellowPlayer.isBot,
                                diceValue = gameState.diceValue,
                                onDiceClick = { viewModel.onDiceClicked() },
                                diceOnRight = false,
                                onRollComplete = { dice -> viewModel.onDiceRollComplete(dice) }
                            )
                        } else {
                            Spacer(modifier = Modifier.height(1.dp))
                        }
                    }
                }

            } else {
                // ==========================================
                // PORTRAIT LAYOUT: TOP/BOTTOM PODS + HEADER
                // ==========================================
                val reservedVertical = 64.dp * 2 + 38.dp + 28.dp + 16.dp
                val maxBoardDimension = min(maxWidth - 12.dp, maxHeight - reservedVertical)
                val boardSize = maxBoardDimension.coerceAtLeast(280.dp)

                Column(
                    modifier = Modifier
                        .width(boardSize)
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Top Navigation & Sound Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameIconButton(
                            onClick = { showExitDialog = true },
                            size = 38.dp,
                            style = GameButtonStyle.RED
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val w = size.width
                                val h = size.height
                                val path = Path().apply {
                                    moveTo(w * 0.7f, h * 0.18f)
                                    lineTo(w * 0.28f, h * 0.5f)
                                    lineTo(w * 0.7f, h * 0.82f)
                                }
                                drawPath(path, Color.White, style = Stroke(width = 4f))
                                drawLine(Color.White, Offset(w * 0.32f, h * 0.5f), Offset(w * 0.85f, h * 0.5f), strokeWidth = 4f)
                            }
                        }

                        // Compact Title Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xCC0D1B2A),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFD54F))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                VectorDiceIcon(size = 18.dp)
                                Text(
                                    text = "LUDO SUPREME",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }

                        GameIconButton(
                            onClick = {
                                viewModel.toggleMute()
                                isMuted = viewModel.soundManager.isMuted
                            },
                            size = 38.dp,
                            style = GameButtonStyle.GOLD
                        ) {
                            VectorSpeakerIcon(isMuted = isMuted, size = 18.dp)
                        }
                    }

                    // Top Pods Row (Red Left, Green Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (redPlayer != null) {
                            LudoKingPlayerPod(
                                player = redPlayer,
                                isActive = redPlayer.id == activePlayer.id,
                                isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE,
                                canRoll = gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL && !redPlayer.isBot,
                                diceValue = gameState.diceValue,
                                onDiceClick = { viewModel.onDiceClicked() },
                                diceOnRight = true,
                                onRollComplete = { dice -> viewModel.onDiceRollComplete(dice) }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (greenPlayer != null) {
                            LudoKingPlayerPod(
                                player = greenPlayer,
                                isActive = greenPlayer.id == activePlayer.id,
                                isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE,
                                canRoll = gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL && !greenPlayer.isBot,
                                diceValue = gameState.diceValue,
                                onDiceClick = { viewModel.onDiceClicked() },
                                diceOnRight = false,
                                onRollComplete = { dice -> viewModel.onDiceRollComplete(dice) }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                    }

                    // Maximized Board
                    Box(
                        modifier = Modifier
                            .size(boardSize)
                            .clip(RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        LudoBoardView(
                            players = gameState.players,
                            activePlayer = activePlayer,
                            legalMoves = gameState.legalMoves,
                            onTokenClicked = { token -> viewModel.onTokenClicked(token) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Bottom Pods Row (Blue Left, Yellow Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bluePlayer != null) {
                            LudoKingPlayerPod(
                                player = bluePlayer,
                                isActive = bluePlayer.id == activePlayer.id,
                                isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE,
                                canRoll = gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL && !bluePlayer.isBot,
                                diceValue = gameState.diceValue,
                                onDiceClick = { viewModel.onDiceClicked() },
                                diceOnRight = true,
                                onRollComplete = { dice -> viewModel.onDiceRollComplete(dice) }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (yellowPlayer != null) {
                            LudoKingPlayerPod(
                                player = yellowPlayer,
                                isActive = yellowPlayer.id == activePlayer.id,
                                isRolling = gameState.turnPhase == TurnPhase.ROLLING_DICE,
                                canRoll = gameState.turnPhase == TurnPhase.WAITING_FOR_ROLL && !yellowPlayer.isBot,
                                diceValue = gameState.diceValue,
                                onDiceClick = { viewModel.onDiceClicked() },
                                diceOnRight = false,
                                onRollComplete = { dice -> viewModel.onDiceRollComplete(dice) }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                    }

                    // Bottom Status Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xCC000000),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFD54F))
                    ) {
                        Text(
                            text = gameState.statusMessage,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFD54F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }
        }

        // Gameified Exit / Abandon Game Confirmation Dialog
        if (showExitDialog) {
            Dialog(onDismissRequest = { showExitDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF101C34),
                    border = androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFFFFD54F)),
                    shadowElevation = 24.dp,
                    modifier = Modifier.width(320.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Text(
                            text = "LEAVE GAME?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFFFFD54F)
                        )

                        Text(
                            text = "Are you sure you want to quit to the main menu? Current game progress will be lost.",
                            fontSize = 13.5.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        // 3D GameButtons for dialog options
                        GameButton(
                            text = "RESUME GAME",
                            style = GameButtonStyle.GREEN,
                            fontSize = 15,
                            height = 46.dp,
                            onClick = { showExitDialog = false }
                        )

                        GameButton(
                            text = "QUIT TO MENU",
                            style = GameButtonStyle.RED,
                            fontSize = 15,
                            height = 46.dp,
                            onClick = {
                                showExitDialog = false
                                onBackToMenu()
                            }
                        )
                    }
                }
            }
        }

        // Winner celebration dialog
        if (gameState.winner != null) {
            VictoryDialog(
                winner = gameState.winner!!,
                rankings = gameState.rankings,
                onPlayAgain = { viewModel.startNewGame(gameState.players.size, gameState.players.count { it.isBot }) },
                onMainMenu = onBackToMenu
            )
        }
    }
}

/**
 * Clean procedural vector speaker icon for audio toggle (no emojis).
 */
@Composable
fun VectorSpeakerIcon(isMuted: Boolean, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 18.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Speaker cone path
        val speakerPath = Path().apply {
            moveTo(w * 0.12f, h * 0.36f)
            lineTo(w * 0.36f, h * 0.36f)
            lineTo(w * 0.60f, h * 0.14f)
            lineTo(w * 0.60f, h * 0.86f)
            lineTo(w * 0.36f, h * 0.64f)
            lineTo(w * 0.12f, h * 0.64f)
            close()
        }
        drawPath(speakerPath, Color.White)

        if (isMuted) {
            // Crisp red slash (X mark)
            drawLine(
                color = Color(0xFFFF5252),
                start = Offset(w * 0.68f, h * 0.32f),
                end = Offset(w * 0.92f, h * 0.68f),
                strokeWidth = 3.5f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFF5252),
                start = Offset(w * 0.92f, h * 0.32f),
                end = Offset(w * 0.68f, h * 0.68f),
                strokeWidth = 3.5f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        } else {
            // Sound wave arcs
            drawArc(
                color = Color.White,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.44f, h * 0.28f),
                size = androidx.compose.ui.geometry.Size(w * 0.32f, h * 0.44f),
                style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFFFD54F),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.54f, h * 0.16f),
                size = androidx.compose.ui.geometry.Size(w * 0.40f, h * 0.68f),
                style = Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

