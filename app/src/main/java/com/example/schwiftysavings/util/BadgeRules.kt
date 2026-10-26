package com.example.schwiftysavings.util

/**
 * Badges are earned from logging and saving behaviour.
 * A streak is a run of calendar days that each have at least one expense.
 */
data class Badge(
    val id: String,
    val title: String,
    val detail: String,
    val unlocked: Boolean
)

object BadgeRules {

    fun loggingStreak(daysWithExpenses: Set<Long>, todayEpochDay: Long): Int {
        var cursor = when {
            todayEpochDay in daysWithExpenses -> todayEpochDay
            (todayEpochDay - 1) in daysWithExpenses -> todayEpochDay - 1
            else -> return 0
        }
        var streak = 0
        while (cursor in daysWithExpenses) {
            streak++
            cursor--
        }
        return streak
    }

    fun evaluate(
        expenseCount: Int,
        photoCount: Int,
        distinctDays: Set<Long>,
        todayEpochDay: Long,
        hasGoal: Boolean,
        zone: GoalZone
    ): List<Badge> {
        val streak = loggingStreak(distinctDays, todayEpochDay)
        return listOf(
            Badge(
                id = "first_log",
                title = "First Entry",
                detail = "Log your first expense.",
                unlocked = expenseCount > 0
            ),
            Badge(
                id = "receipt",
                title = "Receipt Keeper",
                detail = "Attach a receipt photo to an expense.",
                unlocked = photoCount > 0
            ),
            Badge(
                id = "streak_3",
                title = "3-day streak",
                detail = "Log an expense on 3 days in a row.",
                unlocked = streak >= 3
            ),
            Badge(
                id = "streak_7",
                title = "7-day streak",
                detail = "Log an expense on 7 days in a row.",
                unlocked = streak >= 7
            ),
            Badge(
                id = "goal_setter",
                title = "Goal Setter",
                detail = "Save a minimum and a maximum spending goal.",
                unlocked = hasGoal
            ),
            Badge(
                id = "in_the_zone",
                title = "In the Zone",
                detail = "Keep the last 30 days of spending between your min and max.",
                unlocked = zone == GoalZone.IN_RANGE
            )
        )
    }
}