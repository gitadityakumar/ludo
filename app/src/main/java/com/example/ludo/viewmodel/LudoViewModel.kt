package com.example.ludo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ludo.audio.SoundManager
import com.example.ludo.logic.BotAi
import com.example.ludo.logic.LudoGameEngine
import com.example.ludo.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    val soundManager = SoundManager(application)

    private val _gameState = MutableStateFlow(createInitialGame(playerCount = 2, botCount = 1))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var diceRollFallbackJob: Job? = null
    private var moveJob: Job? = null
    private var botJob: Job? = null
    private var turnJob: Job? = null

    private fun cancelAllJobs() {
        diceRollFallbackJob?.cancel()
        diceRollFallbackJob = null
        moveJob?.cancel()
        moveJob = null
        botJob?.cancel()
        botJob = null
        turnJob?.cancel()
        turnJob = null
    }

    fun startNewGame(playerCount: Int, botCount: Int) {
        cancelAllJobs()
        _gameState.value = createInitialGame(playerCount, botCount)
        soundManager.play(SoundManager.SoundType.BUTTON_CLICK)
        checkAndTriggerBot()
    }

    private fun createInitialGame(playerCount: Int, botCount: Int): GameState {
        val selectedColors = when (playerCount) {
            2 -> listOf(PlayerColor.BLUE, PlayerColor.GREEN)
            3 -> listOf(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN)
            else -> listOf(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW)
        }
        val humanCount = playerCount - botCount

        val players = selectedColors.mapIndexed { index, color ->
            val isBot = index >= humanCount
            val baseName = "Player ${index + 1}"
            Player(
                id = index,
                name = if (isBot) "$baseName (Bot)" else baseName,
                color = color,
                isBot = isBot
            )
        }

        return GameState(
            players = players,
            activePlayerIndex = 0,
            diceValue = 1,
            turnPhase = TurnPhase.WAITING_FOR_ROLL,
            statusMessage = "${players[0].name}'s turn. Tap dice to roll!"
        )
    }

    fun onDiceClicked() {
        val state = _gameState.value
        if (state.turnPhase != TurnPhase.WAITING_FOR_ROLL) return
        if (state.activePlayer.isBot) return // human tap only for human
        if (moveJob?.isActive == true || turnJob?.isActive == true) return

        rollDice()
    }

    private fun rollDice() {
        cancelAllJobs()
        val finalRoll = Random.nextInt(1, 7)
        _gameState.update {
            it.copy(
                turnPhase = TurnPhase.ROLLING_DICE,
                diceValue = finalRoll,
                legalMoves = emptyList()
            )
        }
        soundManager.play(SoundManager.SoundType.DICE_ROLL)

        // Safety fallback: only triggers if UI roll animation callback never arrives within 650ms
        diceRollFallbackJob = viewModelScope.launch {
            delay(650)
            if (_gameState.value.turnPhase == TurnPhase.ROLLING_DICE) {
                diceRollFallbackJob = null
                processRollResult(finalRoll)
            }
        }
    }

    fun onDiceRollComplete(dice: Int) {
        // Cancel the fallback immediately so it never triggers
        diceRollFallbackJob?.cancel()
        diceRollFallbackJob = null

        val state = _gameState.value
        if (state.turnPhase != TurnPhase.ROLLING_DICE) return
        processRollResult(dice)
    }

    private fun processRollResult(dice: Int) {
        diceRollFallbackJob?.cancel()
        diceRollFallbackJob = null
        turnJob?.cancel()

        val state = _gameState.value
        val activePlayer = state.activePlayer
        val legalMoves = LudoGameEngine.computeLegalMoves(activePlayer, dice, state.players)

        val movableTokens = legalMoves.map { it.token.id }.distinct()
        val allYardMoves = legalMoves.isNotEmpty() && legalMoves.all { it.token.state == TokenState.IN_YARD }

        if (legalMoves.isEmpty()) {
            // No valid moves, display rolled dice briefly then advance to next player
            val nextIdx = LudoGameEngine.getNextActivePlayerIndex(state.activePlayerIndex, state.players)
            val nextPlayer = state.players[nextIdx]
            _gameState.update {
                it.copy(
                    turnPhase = TurnPhase.ANIMATING_MOVE, // locks UI from interactions while showing rolled dice
                    diceValue = dice,
                    legalMoves = emptyList(),
                    statusMessage = "No moves for ${activePlayer.name}. ${nextPlayer.name}'s turn!"
                )
            }
            turnJob = viewModelScope.launch {
                delay(800) // Keep the rolled dice visible for 800ms
                _gameState.update {
                    it.copy(
                        turnPhase = TurnPhase.WAITING_FOR_ROLL,
                        diceValue = dice,
                        legalMoves = emptyList(),
                        activePlayerIndex = nextIdx,
                        statusMessage = "${nextPlayer.name}'s turn. Tap dice to roll!"
                    )
                }
                delay(300)
                checkAndTriggerBot()
            }
        } else if (movableTokens.size == 1 || allYardMoves) {
            // Auto move when only one token can play this turn, or when all available moves are opening a token from the yard
            _gameState.update {
                it.copy(
                    turnPhase = TurnPhase.ANIMATING_MOVE,
                    legalMoves = emptyList()
                )
            }
            val moveToExecute = legalMoves.first()
            turnJob = viewModelScope.launch {
                delay(400) // Pause so player can clearly see the rolled dice number
                executeMove(moveToExecute)
            }
        } else if (activePlayer.isBot) {
            // Bot chooses best move
            _gameState.update {
                it.copy(
                    turnPhase = TurnPhase.ANIMATING_MOVE,
                    legalMoves = emptyList()
                )
            }
            turnJob = viewModelScope.launch {
                delay(450)
                val chosenMove = BotAi.chooseBestMove(legalMoves) ?: legalMoves.first()
                executeMove(chosenMove)
            }
        } else {
            // Human player with multiple choices: show selectable token indicators
            _gameState.update {
                it.copy(
                    turnPhase = TurnPhase.SELECT_TOKEN,
                    legalMoves = legalMoves,
                    statusMessage = "Select a highlighted token to move"
                )
            }
        }
    }

    fun onTokenClicked(token: Token) {
        val state = _gameState.value
        if (state.turnPhase != TurnPhase.SELECT_TOKEN) return
        if (state.activePlayer.isBot) return
        if (moveJob?.isActive == true) return

        val move = state.legalMoves.find { it.token.id == token.id && it.token.color == token.color }
            ?: if (token.state == TokenState.IN_YARD) {
                state.legalMoves.find { it.token.state == TokenState.IN_YARD && it.token.color == token.color }
            } else null

        if (move != null) {
            // Immediately lock UI and clear legalMoves to prevent double-tap
            _gameState.update {
                it.copy(
                    turnPhase = TurnPhase.ANIMATING_MOVE,
                    legalMoves = emptyList()
                )
            }
            executeMove(move)
        }
    }

    private fun executeMove(move: MoveOption) {
        moveJob?.cancel()
        moveJob = viewModelScope.launch {
            _gameState.update {
                it.copy(
                    turnPhase = TurnPhase.ANIMATING_MOVE,
                    legalMoves = emptyList()
                )
            }

            val startSteps = move.token.stepsMoved
            val endSteps = move.newStepsMoved

            if (move.token.state == TokenState.IN_YARD && move.targetState == TokenState.ON_TRACK) {
                // Exiting yard to start square
                _gameState.update { current ->
                    current.copy(
                        players = current.players.map { p ->
                            if (p.color == move.token.color) {
                                p.copy(
                                    tokens = p.tokens.map { tok ->
                                        if (tok.id == move.token.id) {
                                            tok.copy(
                                                state = TokenState.ON_TRACK,
                                                trackPosition = move.targetTrackPos,
                                                stepsMoved = 1
                                            )
                                        } else tok
                                    }
                                )
                            } else p
                        }
                    )
                }
                soundManager.play(SoundManager.SoundType.TOKEN_EXIT)
                delay(250)
            } else {
                // Hop step by step along track playing exact Ludo King step sound
                for (s in (startSteps + 1)..endSteps) {
                    val intermediateTrackPos = if (s <= 51) {
                        (move.token.color.startSquareIndex + (s - 1)) % LudoGameEngine.TOTAL_TRACK_SQUARES
                    } else -1
                    val intermediateHomeStretchPos = if (s in 52..56) s - 52 else -1
                    val intermediateState = when {
                        s == 57 -> TokenState.FINISHED
                        s >= 52 -> TokenState.IN_HOME_STRETCH
                        else -> TokenState.ON_TRACK
                    }

                    _gameState.update { current ->
                        current.copy(
                            players = current.players.map { p ->
                                if (p.color == move.token.color) {
                                    p.copy(
                                        tokens = p.tokens.map { tok ->
                                            if (tok.id == move.token.id) {
                                                tok.copy(
                                                    state = intermediateState,
                                                    trackPosition = intermediateTrackPos,
                                                    homeStretchPosition = intermediateHomeStretchPos,
                                                    stepsMoved = s
                                                )
                                            } else tok
                                        }
                                    )
                                } else p
                            }
                        )
                    }
                    soundManager.play(SoundManager.SoundType.TOKEN_STEP)
                    delay(120) // Snappy Ludo King hop cadence
                }
            }

            // Post-arrival SFX
            if (move.leadsToFinish) {
                soundManager.play(SoundManager.SoundType.TOKEN_HOME)
                delay(300)
            } else if (move.capturesToken != null) {
                soundManager.play(SoundManager.SoundType.CAPTURE)
                delay(350)
            } else if (move.targetTrackPos in BoardCoordinates.SAFE_POSITIONS) {
                soundManager.play(SoundManager.SoundType.TOKEN_EXIT)
                delay(200)
            }

            val newState = LudoGameEngine.applyMove(_gameState.value, move)
            _gameState.value = newState

            if (newState.winner != null) {
                soundManager.play(SoundManager.SoundType.VICTORY)
            } else {
                checkAndTriggerBot()
            }
        }
    }

    private fun checkAndTriggerBot() {
        botJob?.cancel()
        val state = _gameState.value
        if (state.winner != null) return

        if (state.activePlayer.isBot && state.turnPhase == TurnPhase.WAITING_FOR_ROLL) {
            botJob = viewModelScope.launch {
                delay(700) // Realistic bot thinking delay
                rollDice()
            }
        }
    }

    fun toggleMute() {
        soundManager.isMuted = !soundManager.isMuted
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
        soundManager.release()
    }
}
