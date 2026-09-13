package com.example.ludo.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ludo.model.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LudoBoardView(
    players: List<Player>,
    activePlayer: Player,
    legalMoves: List<MoveOption>,
    onTokenClicked: (Token) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for movable tokens
    val pulseTransition = rememberInfiniteTransition(label = "token_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    ) {
        val boardSizePx = constraints.maxWidth.toFloat()
        val cellSize = boardSizePx / 15f

        // 1. Static Ludo King board
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLudoBoard(cellSize, players)
        }

        // 2. Interactive tokens (Ludo King 3D map-pin pawns with touch detection)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(legalMoves, cellSize) {
                    detectTapGestures { tapOffset ->
                        val yardMove = legalMoves.find { it.token.state == TokenState.IN_YARD }
                        if (yardMove != null) {
                            for (tok in activePlayer.tokens.filter { it.state == TokenState.IN_YARD }) {
                                val (r, c) = getTokenScreenCoords(tok)
                                val tokenCenter = Offset(c * cellSize, r * cellSize)
                                if ((tapOffset - tokenCenter).getDistance() <= cellSize * 1.15f) {
                                    onTokenClicked(yardMove.token)
                                    return@detectTapGestures
                                }
                            }
                        }

                        for (move in legalMoves.filter { it.token.state != TokenState.IN_YARD }) {
                            val (r, c) = getTokenScreenCoords(move.token)
                            val tokenCenter = Offset(c * cellSize, r * cellSize)
                            val distance = (tapOffset - tokenCenter).getDistance()
                            if (distance <= cellSize * 1.15f) {
                                onTokenClicked(move.token)
                                return@detectTapGestures
                            }
                        }
                    }
                }
        ) {
            drawBoardTokens(
                players = players,
                activePlayer = activePlayer,
                legalMoves = legalMoves,
                cellSize = cellSize,
                pulseScale = pulseScale
            )
        }
    }
}

private fun getTokenScreenCoords(token: Token): Pair<Float, Float> {
    return when (token.state) {
        TokenState.IN_YARD -> {
            val coords = BoardCoordinates.YARD_POSITIONS[token.color] ?: listOf(Pair(1.92f, 1.92f))
            coords.getOrElse(token.id) { Pair(1.92f, 1.92f) }
        }
        TokenState.ON_TRACK -> {
            val coord = BoardCoordinates.TRACK_COORDINATES.getOrElse(token.trackPosition) { Pair(6, 1) }
            Pair(coord.first.toFloat() + 0.5f, coord.second.toFloat() + 0.5f)
        }
        TokenState.IN_HOME_STRETCH -> {
            val coords = BoardCoordinates.HOME_STRETCH_COORDINATES[token.color] ?: emptyList()
            val coord = coords.getOrElse(token.homeStretchPosition) { Pair(7, 7) }
            Pair(coord.first.toFloat() + 0.5f, coord.second.toFloat() + 0.5f)
        }
        TokenState.FINISHED -> {
            BoardCoordinates.HOME_CENTER_COORDINATES[token.color] ?: Pair(7.5f, 7.5f)
        }
    }
}

private fun DrawScope.drawLudoBoard(cellSize: Float, players: List<Player>) {
    val totalSize = cellSize * 15f

    // Board Base Background (Crisp pure white)
    drawRect(color = Color.White, size = Size(totalSize, totalSize))

    val redPlayer = players.find { it.color == PlayerColor.RED }
    val greenPlayer = players.find { it.color == PlayerColor.GREEN }
    val yellowPlayer = players.find { it.color == PlayerColor.YELLOW }
    val bluePlayer = players.find { it.color == PlayerColor.BLUE }

    // 1. Draw 4 Corner Home Yards (6x6 cells each)
    drawYard(0f, 0f, cellSize * 6, PlayerColor.RED, cellSize, redPlayer?.name)
    drawYard(cellSize * 9, 0f, cellSize * 6, PlayerColor.GREEN, cellSize, greenPlayer?.name)
    drawYard(cellSize * 9, cellSize * 9, cellSize * 6, PlayerColor.YELLOW, cellSize, yellowPlayer?.name)
    drawYard(0f, cellSize * 9, cellSize * 6, PlayerColor.BLUE, cellSize, bluePlayer?.name)

    // 2. Draw Track Grid (Arms)
    val gridBorderColor = Color(0xFF333333)
    val gridStroke = 1.4f

    for (r in 0..14) {
        for (c in 0..14) {
            val inYard = (r < 6 && c < 6) || (r < 6 && c > 8) || (r > 8 && c < 6) || (r > 8 && c > 8)
            val inCenter = (r in 6..8 && c in 6..8)
            if (!inYard && !inCenter) {
                val left = c * cellSize
                val top = r * cellSize

                var fillColor = Color.White

                // Colored home paths
                if (r == 7 && c in 1..5) fillColor = PlayerColor.RED.primaryColor
                if (c == 7 && r in 1..5) fillColor = PlayerColor.GREEN.primaryColor
                if (r == 7 && c in 9..13) fillColor = PlayerColor.YELLOW.primaryColor
                if (c == 7 && r in 9..13) fillColor = PlayerColor.BLUE.primaryColor

                // Starting squares
                if (r == 6 && c == 1) fillColor = PlayerColor.RED.primaryColor
                if (r == 1 && c == 8) fillColor = PlayerColor.GREEN.primaryColor
                if (r == 8 && c == 13) fillColor = PlayerColor.YELLOW.primaryColor
                if (r == 13 && c == 6) fillColor = PlayerColor.BLUE.primaryColor

                // Cell background & dark border
                drawRect(color = fillColor, topLeft = Offset(left, top), size = Size(cellSize, cellSize))
                drawRect(
                    color = gridBorderColor,
                    topLeft = Offset(left, top),
                    size = Size(cellSize, cellSize),
                    style = Stroke(width = gridStroke)
                )

                // Safe Stars (at track positions 9, 22, 35, 48, plus start squares)
                val posIndex = BoardCoordinates.TRACK_COORDINATES.indexOf(Pair(r, c))
                if (posIndex in setOf(9, 22, 35, 48)) {
                    drawStar(
                        cx = left + cellSize / 2f,
                        cy = top + cellSize / 2f,
                        radius = cellSize * 0.35f,
                        fillColor = Color.White,
                        strokeColor = Color(0xFF263238)
                    )
                } else if (posIndex in setOf(1, 14, 27, 40)) {
                    // Start square safe star
                    drawStar(
                        cx = left + cellSize / 2f,
                        cy = top + cellSize / 2f,
                        radius = cellSize * 0.32f,
                        fillColor = Color(0xDDFFFFFF),
                        strokeColor = Color(0x88000000)
                    )
                }

                // Directional entrance arrows pointing into colored stretch
                if (r == 7 && c == 0) drawArrow(left + cellSize / 2f, top + cellSize / 2f, cellSize * 0.42f, 0f, PlayerColor.RED.primaryColor)
                if (r == 0 && c == 7) drawArrow(left + cellSize / 2f, top + cellSize / 2f, cellSize * 0.42f, 90f, PlayerColor.GREEN.primaryColor)
                if (r == 7 && c == 14) drawArrow(left + cellSize / 2f, top + cellSize / 2f, cellSize * 0.42f, 180f, PlayerColor.YELLOW.primaryColor)
                if (r == 14 && c == 7) drawArrow(left + cellSize / 2f, top + cellSize / 2f, cellSize * 0.42f, 270f, PlayerColor.BLUE.primaryColor)
            }
        }
    }

    // 3. Draw Center Home Triangles (3x3 cells)
    drawCenterTriangles(cellSize)

    // 4. Outer Border
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset.Zero,
        size = Size(totalSize, totalSize),
        style = Stroke(width = 3.5f)
    )
}

private fun DrawScope.drawYard(
    left: Float,
    top: Float,
    size: Float,
    color: PlayerColor,
    cellSize: Float,
    playerName: String?
) {
    // Solid colored background
    drawRect(
        color = color.primaryColor,
        topLeft = Offset(left, top),
        size = Size(size, size)
    )

    // Yard inner white rounded base
    val pad = size * 0.13f
    val innerSize = size - pad * 2
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(left + pad, top + pad),
        size = Size(innerSize, innerSize),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // 4 circular resting spots matching Ludo King
    val slotRadius = size * 0.11f
    val centers = listOf(
        Offset(left + size * 0.32f, top + size * 0.32f),
        Offset(left + size * 0.68f, top + size * 0.32f),
        Offset(left + size * 0.32f, top + size * 0.68f),
        Offset(left + size * 0.68f, top + size * 0.68f)
    )

    for (c in centers) {
        drawCircle(color = color.primaryColor, radius = slotRadius, center = c)
        drawCircle(color = Color(0x33000000), radius = slotRadius, center = c, style = Stroke(width = 1.5f))
    }

    // Yard dark outer border
    drawRect(
        color = Color(0xFF263238),
        topLeft = Offset(left, top),
        size = Size(size, size),
        style = Stroke(width = 2f)
    )

    // Optional Player Label matching Ludo King
    if (playerName != null) {
        val labelY = if (color == PlayerColor.BLUE || color == PlayerColor.YELLOW) {
            top + size - cellSize * 0.22f
        } else {
            top + cellSize * 0.58f
        }
        val labelX = left + size * 0.5f

        drawContext.canvas.nativeCanvas.apply {
            val strokePaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = cellSize * 0.44f
                typeface = Typeface.DEFAULT_BOLD
                style = Paint.Style.STROKE
                strokeWidth = 4f
                this.color = android.graphics.Color.WHITE
            }
            val fillPaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = cellSize * 0.44f
                typeface = Typeface.DEFAULT_BOLD
                style = Paint.Style.FILL
                this.color = android.graphics.Color.BLACK
            }
            drawText(playerName, labelX, labelY, strokePaint)
            drawText(playerName, labelX, labelY, fillPaint)
        }
    }
}

private fun DrawScope.drawCenterTriangles(cellSize: Float) {
    val left = cellSize * 6f
    val top = cellSize * 6f
    val right = cellSize * 9f
    val bottom = cellSize * 9f
    val centerX = cellSize * 7.5f
    val centerY = cellSize * 7.5f

    // Red (Left) triangle
    val redPath = Path().apply {
        moveTo(left, top)
        lineTo(centerX, centerY)
        lineTo(left, bottom)
        close()
    }
    drawPath(redPath, color = PlayerColor.RED.primaryColor)

    // Green (Top) triangle
    val greenPath = Path().apply {
        moveTo(left, top)
        lineTo(centerX, centerY)
        lineTo(right, top)
        close()
    }
    drawPath(greenPath, color = PlayerColor.GREEN.primaryColor)

    // Yellow (Right) triangle
    val yellowPath = Path().apply {
        moveTo(right, top)
        lineTo(centerX, centerY)
        lineTo(right, bottom)
        close()
    }
    drawPath(yellowPath, color = PlayerColor.YELLOW.primaryColor)

    // Blue (Bottom) triangle
    val bluePath = Path().apply {
        moveTo(left, bottom)
        lineTo(centerX, centerY)
        lineTo(right, bottom)
        close()
    }
    drawPath(bluePath, color = PlayerColor.BLUE.primaryColor)

    // Center divider lines
    val dividerColor = Color(0xFF212121)
    val strokeWidth = 2.2f
    drawLine(dividerColor, Offset(left, top), Offset(right, bottom), strokeWidth = strokeWidth)
    drawLine(dividerColor, Offset(left, bottom), Offset(right, top), strokeWidth = strokeWidth)
    drawRect(dividerColor, topLeft = Offset(left, top), size = Size(cellSize * 3, cellSize * 3), style = Stroke(strokeWidth))
}

private fun DrawScope.drawStar(
    cx: Float,
    cy: Float,
    radius: Float,
    fillColor: Color,
    strokeColor: Color
) {
    val points = 5
    val innerRadius = radius * 0.42f
    val path = Path()
    val angleStep = Math.PI / points
    var currentAngle = -Math.PI / 2.0

    path.moveTo(
        (cx + radius * cos(currentAngle)).toFloat(),
        (cy + radius * sin(currentAngle)).toFloat()
    )

    for (i in 1 until points * 2) {
        currentAngle += angleStep
        val r = if (i % 2 == 1) innerRadius else radius
        path.lineTo(
            (cx + r * cos(currentAngle)).toFloat(),
            (cy + r * sin(currentAngle)).toFloat()
        )
    }
    path.close()
    drawPath(path, color = fillColor, style = Fill)
    drawPath(path, color = strokeColor, style = Stroke(width = 1.6f))
}

private fun DrawScope.drawArrow(
    cx: Float,
    cy: Float,
    size: Float,
    rotationDegrees: Float,
    color: Color
) {
    val rad = Math.toRadians(rotationDegrees.toDouble())
    val cosR = cos(rad).toFloat()
    val sinR = sin(rad).toFloat()

    fun rotate(x: Float, y: Float): Offset {
        return Offset(
            cx + (x * cosR - y * sinR),
            cy + (x * sinR + y * cosR)
        )
    }

    val arrowPath = Path().apply {
        val p1 = rotate(size * 0.45f, 0f)
        val p2 = rotate(-size * 0.35f, -size * 0.35f)
        val p3 = rotate(-size * 0.15f, 0f)
        val p4 = rotate(-size * 0.35f, size * 0.35f)

        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        lineTo(p4.x, p4.y)
        close()
    }

    drawPath(arrowPath, color = color, style = Fill)
    drawPath(arrowPath, color = Color(0x66000000), style = Stroke(width = 1.2f))
}

private fun DrawScope.drawBoardTokens(
    players: List<Player>,
    activePlayer: Player,
    legalMoves: List<MoveOption>,
    cellSize: Float,
    pulseScale: Float
) {
    val movableTokenKeys = legalMoves.map { it.token.id to it.token.color }.toSet()

    // Group active board tokens by cell coordinate to offset overlapping tokens cleanly
    val positionMap = mutableMapOf<Pair<Int, Int>, MutableList<Pair<Player, Token>>>()

    for (player in players) {
        for (token in player.tokens) {
            if (token.state == TokenState.ON_TRACK || token.state == TokenState.IN_HOME_STRETCH) {
                val key = if (token.state == TokenState.ON_TRACK) {
                    token.trackPosition to -1
                } else {
                    token.homeStretchPosition to token.color.ordinal
                }
                positionMap.getOrPut(key) { mutableListOf() }.add(player to token)
            }
        }
    }

    for (player in players) {
        for (token in player.tokens) {
            val (r, c) = getTokenScreenCoords(token)
            var cx = c * cellSize
            var cy = r * cellSize

            // Apply stacking offset if multiple tokens share the same track/stretch cell
            if (token.state == TokenState.ON_TRACK || token.state == TokenState.IN_HOME_STRETCH) {
                val key = if (token.state == TokenState.ON_TRACK) {
                    token.trackPosition to -1
                } else {
                    token.homeStretchPosition to token.color.ordinal
                }
                val tokensAtCell = positionMap[key] ?: emptyList()
                if (tokensAtCell.size > 1) {
                    val index = tokensAtCell.indexOfFirst { it.second.id == token.id && it.second.color == token.color }
                    val angle = (2.0 * Math.PI * index) / tokensAtCell.size
                    val offsetDist = cellSize * 0.16f
                    cx += (offsetDist * cos(angle)).toFloat()
                    cy += (offsetDist * sin(angle)).toFloat()
                }
            }

            val hasYardMove = legalMoves.any { it.token.state == TokenState.IN_YARD && it.token.color == activePlayer.color }
            val isMovable = if (token.state == TokenState.IN_YARD) {
                hasYardMove && token.color == activePlayer.color
            } else {
                movableTokenKeys.contains(token.id to token.color)
            }

            drawLudoKingPawn(
                cx = cx,
                cy = cy,
                cellSize = cellSize,
                playerColor = player.color,
                isMovable = isMovable,
                pulseScale = if (isMovable) pulseScale else 1.0f
            )
        }
    }
}

/**
 * Procedural rendering of the signature Ludo King 3D Map-Pin Pawn.
 */
private fun DrawScope.drawLudoKingPawn(
    cx: Float,
    cy: Float,
    cellSize: Float,
    playerColor: PlayerColor,
    isMovable: Boolean,
    pulseScale: Float
) {
    val bounceY = if (isMovable) -((pulseScale - 1f) * cellSize * 0.75f) else 0f

    // 1. Floor Base Ring
    val ringRadiusX = cellSize * 0.32f
    val ringRadiusY = cellSize * 0.20f
    
    drawOval(
        color = playerColor.primaryColor,
        topLeft = Offset(cx - ringRadiusX, cy - ringRadiusY * 0.5f),
        size = Size(ringRadiusX * 2, ringRadiusY * 2)
    )
    drawOval(
        color = playerColor.darkColor,
        topLeft = Offset(cx - ringRadiusX * 0.8f, cy - ringRadiusY * 0.5f + ringRadiusY * 0.2f),
        size = Size(ringRadiusX * 1.6f, ringRadiusY * 1.6f),
        style = Stroke(width = cellSize * 0.045f)
    )

    // Glowing selection indicator on ground
    if (isMovable) {
        drawCircle(
            color = Color(0xFFFFD700),
            radius = cellSize * 0.48f * pulseScale,
            center = Offset(cx, cy),
            style = Stroke(width = 3.5f)
        )
        drawCircle(
            color = Color(0x33FFD700),
            radius = cellSize * 0.44f * pulseScale,
            center = Offset(cx, cy)
        )
    }

    // 2. Drop Shadow of the elevated pin
    val shadowOffset = Offset(cellSize * 0.08f, cellSize * 0.10f + bounceY)
    val headRadius = cellSize * 0.22f
    val headCenterY = cy - cellSize * 0.32f + bounceY
    val tipY = cy + bounceY

    val shadowPath = Path().apply {
        val scx = cx + shadowOffset.x
        val scy = headCenterY + shadowOffset.y
        val stx = cx + shadowOffset.x
        val sty = tipY + shadowOffset.y

        moveTo(stx, sty)
        lineTo(scx - headRadius, scy)
        arcTo(
            rect = Rect(scx - headRadius, scy - headRadius, scx + headRadius, scy + headRadius),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        lineTo(stx, sty)
        close()
    }
    drawPath(shadowPath, color = Color(0x40000000))

    // 3. The 3D Map-Pin Body
    val pinPath = Path().apply {
        moveTo(cx, tipY)
        lineTo(cx - headRadius, headCenterY)
        arcTo(
            rect = Rect(cx - headRadius, headCenterY - headRadius, cx + headRadius, headCenterY + headRadius),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        lineTo(cx, tipY)
        close()
    }

    drawPath(
        path = pinPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E6EA), Color(0xFFB8BFC6)),
            start = Offset(cx - headRadius, headCenterY - headRadius),
            end = Offset(cx + headRadius, tipY)
        )
    )

    drawPath(
        path = pinPath,
        color = Color(0xFF2A2A2A),
        style = Stroke(width = 1.8f)
    )

    // 4. Inner Glossy Colored Dome
    val innerRadius = cellSize * 0.155f
    val innerCenter = Offset(cx, headCenterY)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                playerColor.lightColor,
                playerColor.primaryColor,
                playerColor.darkColor
            ),
            center = Offset(cx - innerRadius * 0.28f, headCenterY - innerRadius * 0.28f),
            radius = innerRadius * 1.1f
        ),
        radius = innerRadius,
        center = innerCenter
    )

    drawCircle(
        color = Color(0xFF1E242B),
        radius = innerRadius,
        center = innerCenter,
        style = Stroke(width = 1.4f)
    )

    drawCircle(
        color = Color(0xDDFFFFFF),
        radius = innerRadius * 0.32f,
        center = Offset(cx - innerRadius * 0.32f, headCenterY - innerRadius * 0.32f)
    )
}
