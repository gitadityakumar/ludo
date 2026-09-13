package com.example.ludo.logic

import com.example.ludo.model.*

object LudoGameEngine {

    const val TOTAL_TRACK_SQUARES = 52
    const val HOME_STRETCH_LENGTH = 5
    // Steps to reach finish: 1 step to start square + 50 more track steps + 5 home stretch steps + 1 step to home = 57 steps
    const val TOTAL_STEPS_TO_FINISH = 57

    /**
     * Compute all legal moves for the active player given the dice roll.
     */
    fun computeLegalMoves(player: Player, dice: Int, allPlayers: List<Player>): List<MoveOption> {
        if (player.hasWon) return emptyList()

        val legalMoves = mutableListOf<MoveOption>()

        for (token in player.tokens) {
            when (token.state) {
                TokenState.IN_YARD -> {
                    // Token can exit yard on rolling a 6 or a 1 (classic Indian Ludo rules)
                    if (dice == 6 || dice == 1) {
                        val startPos = player.color.startSquareIndex
                        legalMoves.add(
                            MoveOption(
                                token = token,
                                targetState = TokenState.ON_TRACK,
                                targetTrackPos = startPos,
                                newStepsMoved = 1,
                                capturesToken = null, // starting square is safe
                                leadsToFinish = false
                            )
                        )
                    }
                }
                TokenState.ON_TRACK -> {
                    val currentSteps = token.stepsMoved
                    val newSteps = currentSteps + dice

                    if (newSteps <= 51) {
                        // Still on the outer global track
                        val newTrackPos = (player.color.startSquareIndex + (newSteps - 1)) % TOTAL_TRACK_SQUARES
                        val captured = findCapturableToken(newTrackPos, player.color, allPlayers)
                        legalMoves.add(
                            MoveOption(
                                token = token,
                                targetState = TokenState.ON_TRACK,
                                targetTrackPos = newTrackPos,
                                newStepsMoved = newSteps,
                                capturesToken = captured,
                                leadsToFinish = false
                            )
                        )
                    } else if (newSteps in 52..56) {
                        // Enters colored home stretch (indices 0..4)
                        val homeStretchPos = newSteps - 52
                        legalMoves.add(
                            MoveOption(
                                token = token,
                                targetState = TokenState.IN_HOME_STRETCH,
                                targetHomeStretchPos = homeStretchPos,
                                newStepsMoved = newSteps,
                                capturesToken = null, // Opponents cannot enter home stretch
                                leadsToFinish = false
                            )
                        )
                    } else if (newSteps == TOTAL_STEPS_TO_FINISH) {
                        // Exact roll to finish/home!
                        legalMoves.add(
                            MoveOption(
                                token = token,
                                targetState = TokenState.FINISHED,
                                newStepsMoved = newSteps,
                                capturesToken = null,
                                leadsToFinish = true
                            )
                        )
                    }
                    // If newSteps > TOTAL_STEPS_TO_FINISH, cannot move (requires exact roll)
                }
                TokenState.IN_HOME_STRETCH -> {
                    val currentSteps = token.stepsMoved
                    val newSteps = currentSteps + dice

                    if (newSteps in 52..56) {
                        val homeStretchPos = newSteps - 52
                        legalMoves.add(
                            MoveOption(
                                token = token,
                                targetState = TokenState.IN_HOME_STRETCH,
                                targetHomeStretchPos = homeStretchPos,
                                newStepsMoved = newSteps,
                                capturesToken = null,
                                leadsToFinish = false
                            )
                        )
                    } else if (newSteps == TOTAL_STEPS_TO_FINISH) {
                        legalMoves.add(
                            MoveOption(
                                token = token,
                                targetState = TokenState.FINISHED,
                                newStepsMoved = newSteps,
                                capturesToken = null,
                                leadsToFinish = true
                            )
                        )
                    }
                    // Overshooting requires exact roll
                }
                TokenState.FINISHED -> {
                    // Already finished
                }
            }
        }

        return legalMoves
    }

    /**
     * Look for an opponent token on trackPosition that is vulnerable to capture.
     * Tokens on Safe Squares cannot be captured.
     */
    fun findCapturableToken(trackPos: Int, activeColor: PlayerColor, allPlayers: List<Player>): Token? {
        if (BoardCoordinates.SAFE_POSITIONS.contains(trackPos)) {
            return null // Position is safe!
        }
        for (player in allPlayers) {
            if (player.color != activeColor && !player.hasWon) {
                for (token in player.tokens) {
                    if (token.state == TokenState.ON_TRACK && token.trackPosition == trackPos) {
                        return token
                    }
                }
            }
        }
        return null
    }

    /**
     * Applies the selected move to the game state and returns the updated game state.
     */
    fun applyMove(currentState: GameState, move: MoveOption): GameState {
        val activePlayer = currentState.activePlayer
        val updatedTokens = activePlayer.tokens.map { tok ->
            if (tok.id == move.token.id) {
                tok.copy(
                    state = move.targetState,
                    trackPosition = move.targetTrackPos,
                    homeStretchPosition = move.targetHomeStretchPos,
                    stepsMoved = move.newStepsMoved
                )
            } else {
                tok
            }
        }

        val allHome = updatedTokens.all { it.isHome }
        val updatedRank = if (allHome && !activePlayer.hasWon) {
            currentState.rankings.size + 1
        } else {
            activePlayer.rank
        }

        val updatedActivePlayer = activePlayer.copy(
            tokens = updatedTokens,
            hasWon = allHome,
            rank = updatedRank
        )

        // Handle opponent token capture
        val updatedPlayers = currentState.players.map { player ->
            when {
                player.id == updatedActivePlayer.id -> updatedActivePlayer
                move.capturesToken != null && player.color == move.capturesToken.color -> {
                    player.copy(
                        tokens = player.tokens.map { oppToken ->
                            if (oppToken.id == move.capturesToken.id) {
                                oppToken.copy(
                                    state = TokenState.IN_YARD,
                                    trackPosition = -1,
                                    homeStretchPosition = -1,
                                    stepsMoved = 0
                                )
                            } else {
                                oppToken
                            }
                        }
                    )
                }
                else -> player
            }
        }

        val newRankings = if (allHome && !currentState.rankings.any { it.id == activePlayer.id }) {
            currentState.rankings + updatedActivePlayer
        } else {
            currentState.rankings
        }

        // Game ends when a player wins (or only 1 active player left)
        val winner = if (allHome && currentState.winner == null) updatedActivePlayer else currentState.winner
        val isGameOver = winner != null

        // Extra turn rules:
        // 1. Rolled a 6
        // 2. Captured an opponent token
        // 3. Reached home
        val isExtraTurn = (currentState.diceValue == 6 || move.capturesToken != null || move.leadsToFinish) && !allHome

        val nextIndex = if (isGameOver || isExtraTurn) {
            currentState.activePlayerIndex
        } else {
            getNextActivePlayerIndex(currentState.activePlayerIndex, updatedPlayers)
        }

        val statusMsg = when {
            isGameOver -> "${winner!!.name} has won the game! 🎉"
            move.capturesToken != null -> "${activePlayer.name} captured ${move.capturesToken.color.displayName}'s token! Extra turn!"
            move.leadsToFinish -> "${activePlayer.name}'s token reached Home! Extra turn!"
            currentState.diceValue == 6 -> "${activePlayer.name} rolled a 6! Extra turn!"
            else -> "${updatedPlayers[nextIndex].name}'s turn."
        }

        return currentState.copy(
            players = updatedPlayers,
            activePlayerIndex = nextIndex,
            diceValue = currentState.diceValue,
            turnPhase = if (isGameOver) TurnPhase.GAME_OVER else TurnPhase.WAITING_FOR_ROLL,
            legalMoves = emptyList(),
            statusMessage = statusMsg,
            isExtraTurnGranted = isExtraTurn,
            winner = winner,
            rankings = newRankings
        )
    }

    /**
     * Determines next active player, skipping players who have already won.
     */
    fun getNextActivePlayerIndex(currentIndex: Int, players: List<Player>): Int {
        var idx = (currentIndex + 1) % players.size
        var attempts = 0
        while (players[idx].hasWon && attempts < players.size) {
            idx = (idx + 1) % players.size
            attempts++
        }
        return idx
    }
}
