package com.example.schwiftysavings.ui

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Cards : Screen("cards")
    data object Transactions : Screen("transactions")
    data object Leaderboard : Screen("leaderboard")
    data object Shop : Screen("shop")
    data object Settings : Screen("settings")
    data object Categories : Screen("categories")
    data object AddExpense : Screen("add_expense/{entry}") {
        fun create(entry: String = "expense") = "add_expense/$entry"
    }
    data object Goals : Screen("goals")
    data object Payments : Screen("payments")
    data object CategoryTotals : Screen("category_totals")
    data object SpendingChart : Screen("spending_chart")
    data object GoalProgress : Screen("goal_progress")
    data object Badges : Screen("badges")
    data object Receipt : Screen("receipt/{expenseId}") {
        fun create(expenseId: Long) = "receipt/$expenseId"
    }
    data object Photo : Screen("photo/{expenseId}") {
        fun create(expenseId: Long) = "photo/$expenseId"
    }
}

val bottomBarRoutes = listOf(
    Screen.Home.route,
    Screen.Cards.route,
    Screen.Transactions.route,
    Screen.Leaderboard.route,
    Screen.Shop.route,
    Screen.Settings.route
)