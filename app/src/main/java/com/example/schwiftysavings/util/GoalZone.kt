package com.example.schwiftysavings.util

/**

Where this month's spending sits against the minimum and maximum goals.
Spent amounts are positive cents (rands times 100).*/
enum class GoalZone {
    NO_GOAL,
    UNDER_MIN,
    IN_RANGE,
    OVER_MAX;

    companion object {
        fun of(spentAbsCents: Long, minCents: Long?, maxCents: Long?): GoalZone {
            if (minCents == null || maxCents == null) return NO_GOAL
            if (spentAbsCents < minCents) return UNDER_MIN
            if (spentAbsCents > maxCents) return OVER_MAX
            return IN_RANGE
        }
    }
}

data class MonthProgress(
    val spentCents: Long,
    val minCents: Long?,
    val maxCents: Long?,
    val zone: GoalZone,
    val monthLabel: String = ""
)