package com.example.ludo.logic

import com.example.ludo.model.MoveOption
import com.example.ludo.model.TokenState

object BotAi {
    /**
     * Intelligently selects the best move for a bot player.
     * Heuristics ranking:
     * 1. Winning the game / Reaching home (highest priority)
     * 2. Capturing an opponent token
     * 3. Getting a token out of the yard (when 6 rolled)
     * 4. Moving a token into the home stretch (safety)
     * 5. Moving the token furthest along the board
     * 6. Fallback to any legal move
     */
    fun chooseBestMove(legalMoves: List<MoveOption>): MoveOption? {
        if (legalMoves.isEmpty()) return null
        if (legalMoves.size == 1) return legalMoves.first()

        // 1. Reaching home
        legalMoves.find { it.leadsToFinish }?.let { return it }

        // 2. Capturing an opponent
        legalMoves.find { it.capturesToken != null }?.let { return it }

        // 3. Exiting yard
        legalMoves.find { it.token.state == TokenState.IN_YARD }?.let { return it }

        // 4. Moving into home stretch
        legalMoves.find { it.targetState == TokenState.IN_HOME_STRETCH && it.token.state == TokenState.ON_TRACK }?.let { return it }

        // 5. Advancing token with greatest progress
        return legalMoves.maxByOrNull { it.newStepsMoved } ?: legalMoves.first()
    }
}
