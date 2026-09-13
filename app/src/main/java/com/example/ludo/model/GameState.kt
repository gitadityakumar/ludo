package com.example.ludo.model

data class MoveOption(
    val token: Token,
    val targetState: TokenState,
    val targetTrackPos: Int = -1,
    val targetHomeStretchPos: Int = -1,
    val newStepsMoved: Int,
    val capturesToken: Token? = null,
    val leadsToFinish: Boolean = false
)

enum class TurnPhase {
    WAITING_FOR_ROLL,
    ROLLING_DICE,
    SELECT_TOKEN,
    ANIMATING_MOVE,
    TURN_ENDED,
    GAME_OVER
}

data class GameState(
    val players: List<Player>,
    val activePlayerIndex: Int = 0,
    val diceValue: Int = 1,
    val consecutiveSixes: Int = 0,
    val turnPhase: TurnPhase = TurnPhase.WAITING_FOR_ROLL,
    val legalMoves: List<MoveOption> = emptyList(),
    val movingTokenId: Pair<PlayerColor, Int>? = null, // (color, tokenId) during animation
    val statusMessage: String = "Roll the dice!",
    val isExtraTurnGranted: Boolean = false,
    val winner: Player? = null,
    val rankings: List<Player> = emptyList()
) {
    val activePlayer: Player get() = players[activePlayerIndex]
}
