package com.example.schwiftysavings.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.schwiftysavings.data.SchwiftyRepository
import com.example.schwiftysavings.ui.screens.AddExpenseScreen
import com.example.schwiftysavings.ui.screens.CardsScreen
import com.example.schwiftysavings.ui.screens.CategoriesScreen
import com.example.schwiftysavings.ui.screens.CategoryTotalsScreen
import com.example.schwiftysavings.ui.screens.GoalsScreen
import com.example.schwiftysavings.ui.screens.HomeScreen
import com.example.schwiftysavings.ui.screens.LeaderboardScreen
import com.example.schwiftysavings.ui.screens.PaymentsScreen
import com.example.schwiftysavings.ui.screens.PhotoScreen
import com.example.schwiftysavings.ui.screens.ReceiptScreen
import com.example.schwiftysavings.ui.screens.SettingsScreen
import com.example.schwiftysavings.ui.screens.TransactionsScreen
import com.example.schwiftysavings.ui.theme.Forest
import com.example.schwiftysavings.ui.theme.Mint

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun SchwiftyNavHost(
    repository: SchwiftyRepository,
    loggedInUserId: Long
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    val tabs = listOf(
        Tab(Screen.Home.route, "Home", Icons.Default.Home),
        Tab(Screen.Cards.route, "Card", Icons.Default.CreditCard),
        Tab(Screen.Transactions.route, "Transactions", Icons.Default.ReceiptLong),
        Tab(Screen.Leaderboard.route, "Leaderboard", Icons.Default.Leaderboard),
        Tab(Screen.Settings.route, "Settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Forest, tonalElevation = 0.dp) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                // Home is start destination — pop back to it (fixes stuck on Transactions)
                                if (tab.route == Screen.Home.route) {
                                    navController.popBackStack(Screen.Home.route, inclusive = false)
                                } else {
                                    navController.navigate(tab.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Mint,
                                unselectedTextColor = Mint,
                                indicatorColor = Color(0xFF2D6A4F)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    onAddExpense = { entry ->
                        navController.navigate(Screen.AddExpense.create(entry))
                    },
                    onOpenTransactions = { navController.navigate(Screen.Transactions.route) },
                    onOpenExpense = { id -> navController.navigate(Screen.Receipt.create(id)) }
                )
            }
            composable(Screen.Cards.route) {
                CardsScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    onOpenGoals = { navController.navigate(Screen.Goals.route) },
                    onOpenPayments = { navController.navigate(Screen.Payments.route) },
                    onAddExpense = { navController.navigate(Screen.AddExpense.create()) }
                )
            }
            composable(Screen.Payments.route) {
                PaymentsScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onAddExpense = { navController.navigate(Screen.AddExpense.create()) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    onOpenExpense = { id -> navController.navigate(Screen.Receipt.create(id)) },
                    onOpenPhoto = { id -> navController.navigate(Screen.Photo.create(id)) },
                    onOpenTotals = { navController.navigate(Screen.CategoryTotals.route) },
                    onAddExpense = { navController.navigate(Screen.AddExpense.create()) }
                )
            }
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen(userId = loggedInUserId, repository = repository)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    onCategories = { navController.navigate(Screen.Categories.route) },
                    onGoals = { navController.navigate(Screen.Goals.route) },
                    onLoggedOut = {
                        // MainActivity observes session and returns to LoginActivity via Intent
                    }
                )
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.AddExpense.route,
                arguments = listOf(
                    navArgument("entry") {
                        type = NavType.StringType
                        defaultValue = "expense"
                    }
                )
            ) { backStackEntry ->
                val entry = backStackEntry.arguments?.getString("entry") ?: "expense"
                AddExpenseScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    entry = entry,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Goals.route) {
                GoalsScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.CategoryTotals.route) {
                CategoryTotalsScreen(
                    userId = loggedInUserId,
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Receipt.route,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("expenseId") ?: return@composable
                ReceiptScreen(
                    expenseId = id,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onOpenPhoto = { navController.navigate(Screen.Photo.create(id)) }
                )
            }
            composable(
                route = Screen.Photo.route,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("expenseId") ?: return@composable
                PhotoScreen(
                    expenseId = id,
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}