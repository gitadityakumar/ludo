package com.example.ludo

import com.example.ludo.logic.LudoGameEngine
import com.example.ludo.model.*
import org.junit.Assert.*
import org.junit.Test

class LudoGameEngineTest {

    private fun createSamplePlayer(color: PlayerColor, name: String = color.displayName): Player {
        return Player(
            id = color.ordinal,
            name = name,
            color = color,
            tokens = (0..3).map { Token(id = it, color = color) }
        )
    }

    @Test
    fun testTokenCannotLeaveYardWithoutRollingSixOrOne() {
        val player = createSamplePlayer(PlayerColor.RED)
        val moves = LudoGameEngine.computeLegalMoves(player, dice = 5, listOf(player))
        assertTrue("No moves should be available when rolling 5 with all tokens in yard", moves.isEmpty())

        val movesOnSix = LudoGameEngine.computeLegalMoves(player, dice = 6, listOf(player))
        assertEquals("All 4 tokens can leave yard on rolling 6", 4, movesOnSix.size)
        assertEquals("Target track position must be Red start square (1)", 1, movesOnSix[0].targetTrackPos)

        val movesOnOne = LudoGameEngine.computeLegalMoves(player, dice = 1, listOf(player))
        assertEquals("All 4 tokens can leave yard on rolling 1", 4, movesOnOne.size)
        assertEquals("Target track position must be Red start square (1)", 1, movesOnOne[0].targetTrackPos)
    }

    @Test
    fun testSafeSquarePreventsCapture() {
        // Star safe square is at track position 9
        val redPlayer = createSamplePlayer(PlayerColor.RED).copy(
            tokens = listOf(
                Token(id = 0, color = PlayerColor.RED, state = TokenState.ON_TRACK, trackPosition = 9, stepsMoved = 9),
                Token(id = 1, color = PlayerColor.RED),
                Token(id = 2, color = PlayerColor.RED),
                Token(id = 3, color = PlayerColor.RED)
            )
        )
        val bluePlayer = createSamplePlayer(PlayerColor.BLUE)

        val captured = LudoGameEngine.findCapturableToken(
            trackPos = 9,
            activeColor = PlayerColor.BLUE,
            allPlayers = listOf(redPlayer, bluePlayer)
        )
        assertNull("Safe squares must protect tokens from capture", captured)
    }

    @Test
    fun testUnsafeSquareCapture() {
        // Track square 8 is an unsafe track square
        val redPlayer = createSamplePlayer(PlayerColor.RED).copy(
            tokens = listOf(
                Token(id = 0, color = PlayerColor.RED, state = TokenState.ON_TRACK, trackPosition = 8, stepsMoved = 8),
                Token(id = 1, color = PlayerColor.RED),
                Token(id = 2, color = PlayerColor.RED),
                Token(id = 3, color = PlayerColor.RED)
            )
        )
        val bluePlayer = createSamplePlayer(PlayerColor.BLUE)

        val captured = LudoGameEngine.findCapturableToken(
            trackPos = 8,
            activeColor = PlayerColor.BLUE,
            allPlayers = listOf(redPlayer, bluePlayer)
        )
        assertNotNull("Unsafe squares allow capture", captured)
        assertEquals(PlayerColor.RED, captured?.color)
        assertEquals(0, captured?.id)
    }

    @Test
    fun testExactRollRequiredToReachHome() {
        // Token has 56 steps moved (1 step away from finish which is 57)
        val redPlayer = createSamplePlayer(PlayerColor.RED).copy(
            tokens = listOf(
                Token(id = 0, color = PlayerColor.RED, state = TokenState.IN_HOME_STRETCH, homeStretchPosition = 4, stepsMoved = 56),
                Token(id = 1, color = PlayerColor.RED),
                Token(id = 2, color = PlayerColor.RED),
                Token(id = 3, color = PlayerColor.RED)
            )
        )

        // Rolling a 2 (56 + 2 = 58 > 57) cannot move
        val movesOvershoot = LudoGameEngine.computeLegalMoves(redPlayer, dice = 2, listOf(redPlayer))
        assertTrue("Overshooting finish square is illegal", movesOvershoot.isEmpty())

        // Rolling a 1 (56 + 1 = 57) finishes token 0, and yard tokens can exit
        val movesExact = LudoGameEngine.computeLegalMoves(redPlayer, dice = 1, listOf(redPlayer))
        val token0Move = movesExact.find { it.token.id == 0 }
        assertNotNull("Token 0 must have a finishing move on rolling 1", token0Move)
        assertTrue(token0Move!!.leadsToFinish)
        assertEquals(TokenState.FINISHED, token0Move.targetState)
    }

    @Test
    fun testWinConditionWhenAllFourTokensReachHome() {
        val redPlayer = createSamplePlayer(PlayerColor.RED).copy(
            tokens = listOf(
                Token(id = 0, color = PlayerColor.RED, state = TokenState.FINISHED, stepsMoved = 57),
                Token(id = 1, color = PlayerColor.RED, state = TokenState.FINISHED, stepsMoved = 57),
                Token(id = 2, color = PlayerColor.RED, state = TokenState.FINISHED, stepsMoved = 57),
                Token(id = 3, color = PlayerColor.RED, state = TokenState.IN_HOME_STRETCH, homeStretchPosition = 4, stepsMoved = 56)
            )
        )
        val greenPlayer = createSamplePlayer(PlayerColor.GREEN)
        val state = GameState(
            players = listOf(redPlayer, greenPlayer),
            activePlayerIndex = 0,
            diceValue = 1
        )

        val move = MoveOption(
            token = redPlayer.tokens[3],
            targetState = TokenState.FINISHED,
            newStepsMoved = 57,
            leadsToFinish = true
        )

        val updatedState = LudoGameEngine.applyMove(state, move)
        assertEquals(TurnPhase.GAME_OVER, updatedState.turnPhase)
        assertNotNull(updatedState.winner)
        assertEquals("Red", updatedState.winner?.name)
    }
}
