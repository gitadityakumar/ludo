package com.example.ludo.model

/**
 * Standard Indian Ludo 15x15 board coordinate layout.
 * Rows: 0..14 (top to bottom)
 * Cols: 0..14 (left to right)
 *
 * Board Zones:
 * - Top-Left: Red Yard (Rows 0..5, Cols 0..5)
 * - Top-Right: Green Yard (Rows 0..5, Cols 9..14)
 * - Bottom-Right: Yellow Yard (Rows 9..14, Cols 9..14)
 * - Bottom-Left: Blue Yard (Rows 9..14, Cols 0..5)
 * - Center: Home Triangle (Rows 6..8, Cols 6..8)
 * - 4 Arms: Top (Rows 0..5, Cols 6..8), Right (Rows 6..8, Cols 9..14),
 *           Bottom (Rows 9..14, Cols 6..8), Left (Rows 6..8, Cols 0..5)
 */
object BoardCoordinates {
    // 52-square global outer track coordinates (row, col)
    // Starting at Red's entry track square (row 6, col 1) as index 1,
    // index 0 is (row 6, col 0) right next to it.
    // Red Start = 1, Green Start = 14, Yellow Start = 27, Blue Start = 40.
    val TRACK_COORDINATES: List<Pair<Int, Int>> = listOf(
        Pair(6, 0),  // 0
        Pair(6, 1),  // 1 (Red Start, Safe)
        Pair(6, 2),  // 2
        Pair(6, 3),  // 3
        Pair(6, 4),  // 4
        Pair(6, 5),  // 5
        Pair(5, 6),  // 6
        Pair(4, 6),  // 7
        Pair(3, 6),  // 8
        Pair(2, 6),  // 9 (Safe Star)
        Pair(1, 6),  // 10
        Pair(0, 6),  // 11
        Pair(0, 7),  // 12
        Pair(0, 8),  // 13
        Pair(1, 8),  // 14 (Green Start, Safe)
        Pair(2, 8),  // 15
        Pair(3, 8),  // 16
        Pair(4, 8),  // 17
        Pair(5, 8),  // 18
        Pair(6, 9),  // 19
        Pair(6, 10), // 20
        Pair(6, 11), // 21
        Pair(6, 12), // 22 (Safe Star)
        Pair(6, 13), // 23
        Pair(6, 14), // 24
        Pair(7, 14), // 25
        Pair(8, 14), // 26
        Pair(8, 13), // 27 (Yellow Start, Safe)
        Pair(8, 12), // 28
        Pair(8, 11), // 29
        Pair(8, 10), // 30
        Pair(8, 9),  // 31
        Pair(9, 8),  // 32
        Pair(10, 8), // 33
        Pair(11, 8), // 34
        Pair(12, 8), // 35 (Safe Star)
        Pair(13, 8), // 36
        Pair(14, 8), // 37
        Pair(14, 7), // 38
        Pair(14, 6), // 39
        Pair(13, 6), // 40 (Blue Start, Safe)
        Pair(12, 6), // 41
        Pair(11, 6), // 42
        Pair(10, 6), // 43
        Pair(9, 6),  // 44
        Pair(8, 5),  // 45
        Pair(8, 4),  // 46
        Pair(8, 3),  // 47
        Pair(8, 2),  // 48 (Safe Star)
        Pair(8, 1),  // 49
        Pair(8, 0),  // 50
        Pair(7, 0)   // 51
    )

    // Safe track positions where capturing is disallowed
    // Starting squares (1, 14, 27, 40) + Star safe squares (9, 22, 35, 48)
    val SAFE_POSITIONS: Set<Int> = setOf(1, 9, 14, 22, 27, 35, 40, 48)

    // Colored home stretch corridors (5 squares each leading into center home)
    val HOME_STRETCH_COORDINATES: Map<PlayerColor, List<Pair<Int, Int>>> = mapOf(
        PlayerColor.RED to listOf(
            Pair(7, 1), Pair(7, 2), Pair(7, 3), Pair(7, 4), Pair(7, 5)
        ),
        PlayerColor.GREEN to listOf(
            Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7)
        ),
        PlayerColor.YELLOW to listOf(
            Pair(7, 13), Pair(7, 12), Pair(7, 11), Pair(7, 10), Pair(7, 9)
        ),
        PlayerColor.BLUE to listOf(
            Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7)
        )
    )

    // Final Home center coordinate for each player
    val HOME_CENTER_COORDINATES: Map<PlayerColor, Pair<Float, Float>> = mapOf(
        PlayerColor.RED to Pair(7f, 6.35f),
        PlayerColor.GREEN to Pair(6.35f, 7f),
        PlayerColor.YELLOW to Pair(7f, 7.65f),
        PlayerColor.BLUE to Pair(7.65f, 7f)
    )

    // Token resting positions inside each yard (4 positions per color)
    val YARD_POSITIONS: Map<PlayerColor, List<Pair<Float, Float>>> = mapOf(
        PlayerColor.RED to listOf(
            Pair(1.92f, 1.92f), Pair(1.92f, 4.08f),
            Pair(4.08f, 1.92f), Pair(4.08f, 4.08f)
        ),
        PlayerColor.GREEN to listOf(
            Pair(1.92f, 10.92f), Pair(1.92f, 13.08f),
            Pair(4.08f, 10.92f), Pair(4.08f, 13.08f)
        ),
        PlayerColor.YELLOW to listOf(
            Pair(10.92f, 10.92f), Pair(10.92f, 13.08f),
            Pair(13.08f, 10.92f), Pair(13.08f, 13.08f)
        ),
        PlayerColor.BLUE to listOf(
            Pair(10.92f, 1.92f), Pair(10.92f, 4.08f),
            Pair(13.08f, 1.92f), Pair(13.08f, 4.08f)
        )
    )
}
