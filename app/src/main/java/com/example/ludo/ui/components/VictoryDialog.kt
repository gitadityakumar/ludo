package com.example.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ludo.model.Player
import kotlin.random.Random

@Composable
fun VictoryDialog(
    winner: Player,
    rankings: List<Player>,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    // Trophy scale entrance
    val transition = rememberInfiniteTransition(label = "trophy_bounce")
    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Confetti particles generator
    val confettiParticles = remember {
        List(40) {
            Confetti(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 8f + 4f,
                color = listOf(
                    Color(0xFFFFEB3B), Color(0xFFE53935), Color(0xFF43A047),
                    Color(0xFF1E88E5), Color(0xFFFF4081), Color(0xFF00E676)
                ).random()
            )
        }
    }

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background confetti canvas
            Canvas(modifier = Modifier.matchParentSize()) {
                for (p in confettiParticles) {
                    drawCircle(
                        color = p.color,
                        radius = p.size,
                        center = Offset(p.x * size.width, p.y * size.height)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🏆",
                    fontSize = 58.sp,
                    modifier = Modifier.scale(scale)
                )

                Text(
                    text = "${winner.name} Wins!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Congratulations! All 4 tokens made it safely home!",
                    fontSize = 14.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Play Again Button
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = winner.color.primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Play Again",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Main Menu Button
                OutlinedButton(
                    onClick = onMainMenu,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Back to Menu",
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private data class Confetti(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color
)
