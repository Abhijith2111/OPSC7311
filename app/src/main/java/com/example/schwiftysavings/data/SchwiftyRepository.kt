package com.example.schwiftysavings.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.schwiftysavings.data.local.BudgetGoalEntity
import com.example.schwiftysavings.data.local.CategoryEntity
import com.example.schwiftysavings.data.local.CategoryTotalRow
import com.example.schwiftysavings.data.local.ExpenseEntity
import com.example.schwiftysavings.data.local.LeaderboardEntryEntity
import com.example.schwiftysavings.data.local.SchwiftyDatabase
import com.example.schwiftysavings.data.local.UserEntity
import com.example.schwiftysavings.util.DateUtils
import com.example.schwiftysavings.util.PeriodFilter
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID
import com.example.schwiftysavings.data.remote.FirestoreSync
import com.example.schwiftysavings.util.Badge
import com.example.schwiftysavings.util.BadgeRules
import com.example.schwiftysavings.util.GoalZone
import com.example.schwiftysavings.util.MonthProgress
import kotlin.math.abs


class SchwiftyRepository(
    private val db: SchwiftyDatabase,
    private val sessionStore: SessionStore,
    private val appContext: Context,
    private val cloudSync: FirestoreSync
) {
    private val tag = "SchwiftyRepo"

    val sessionUserId: Flow<Long?> = sessionStore.userIdFlow

    suspend fun register(username: String, password: String, displayName: String): Result<Long> {
        val clean = username.trim()
        if (clean.isEmpty() || password.length < 4) {
            Log.w(tag, "Register failed: invalid input")
            return Result.failure(IllegalArgumentException("Username required; password min 4 chars"))
        }
        if (db.userDao().findByUsername(clean) != null) {
            return Result.failure(IllegalArgumentException("Username already taken"))
        }
        val takenOnline = try {
            cloudSync.findUser(clean) != null
        } catch (e: Exception) {
            Log.e(tag, "Username check skipped: ${e.message}")
            false
        }
        if (takenOnline) {
            return Result.failure(IllegalArgumentException("Username already taken"))
        }
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(password, salt)
        val id = db.userDao().insert(
            UserEntity(
                username = clean,
                passwordHash = hash,
                salt = salt,
                displayName = displayName.ifBlank { clean },
                leaderboardUsername = clean.uppercase()
            )
        )
        seedDefaultsForUser(id)
        sessionStore.setUserId(id)
        cloud {
            val savedUser = db.userDao().findById(id)
            if (savedUser != null) cloudSync.pushUser(savedUser)
            pushLocalSnapshot(id)
        }
        Log.i(tag, "Registered user id=$id")
        return Result.success(id)
    }

    suspend fun login(username: String, password: String): Result<Long> {
        val clean = username.trim()
        var user = db.userDao().findByUsername(clean)
        if (user == null) {
            try {
                val remote = cloudSync.findUser(clean)
                if (remote != null) {
                    db.userDao().insert(remote)
                    user = remote
                    Log.i(tag, "User restored from Firestore id=${remote.id}")
                }
            } catch (e: Exception) {
                Log.e(tag, "Could not read user from Firestore: ${e.message}")
            }
        }
        if (user == null || !PasswordHasher.matches(password, user.salt, user.passwordHash)) {
            Log.w(tag, "Login failed for $clean")
            return Result.failure(IllegalArgumentException("Invalid username or password"))
        }
        sessionStore.setUserId(user.id)
        cloud { pullRemote(user.id) }
        Log.i(tag, "Login ok userId=${user.id}")
        return Result.success(user.id)
    }

    suspend fun logout() {
        Log.i(tag, "Logout")
        sessionStore.setUserId(null)
    }

    suspend fun currentUser(userId: Long): UserEntity? = db.userDao().findById(userId)

    suspend fun updateLeaderboardUsername(userId: Long, username: String) {
        val user = db.userDao().findById(userId) ?: return
        db.userDao().update(user.copy(leaderboardUsername = username.trim()))
    }

    fun observeCategories(userId: Long) = db.categoryDao().observeForUser(userId)

    suspend fun addCategory(userId: Long, name: String): Result<Long> {
        val clean = name.trim()
        if (clean.isEmpty()) return Result.failure(IllegalArgumentException("Name required"))
        return try {
            val id = db.categoryDao().insert(CategoryEntity(userId = userId, name = clean))
            Log.d(tag, "Category created id=$id name=$clean")
            val created = db.categoryDao().findById(id)
            cloud {
                if (created != null) cloudSync.pushCategory(created)
                pushBadgesAndScore(userId)
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Category already exists"))
        }
    }

    fun observeExpenses(userId: Long, startDay: Long, endDay: Long) =
        db.expenseDao().observeInPeriod(userId, startDay, endDay)

    fun observeNetBalance(userId: Long) = db.expenseDao().observeNetBalance(userId)

    suspend fun getExpense(id: Long) = db.expenseDao().findById(id)

    suspend fun getCategory(id: Long) = db.categoryDao().findById(id)

    suspend fun categoryTotals(userId: Long, startDay: Long, endDay: Long): List<CategoryTotalRow> =
        db.expenseDao().categoryTotals(userId, startDay, endDay)

    suspend fun addExpense(
        userId: Long,
        categoryId: Long,
        description: String,
        merchantName: String,
        dateEpochDay: Long,
        startMinute: Int,
        endMinute: Int,
        amountCents: Long,
        photoUri: Uri?
    ): Result<Long> {
        if (description.isBlank()) {
            return Result.failure(IllegalArgumentException("Description required"))
        }
        if (endMinute < startMinute) {
            return Result.failure(IllegalArgumentException("End time cannot be before start time"))
        }
        val photoPath = photoUri?.let { copyPhoto(it) }
        val id = db.expenseDao().insert(
            ExpenseEntity(
                userId = userId,
                categoryId = categoryId,
                description = description.trim(),
                merchantName = merchantName.ifBlank { description.trim() },
                dateEpochDay = dateEpochDay,
                startMinute = startMinute,
                endMinute = endMinute,
                amountCents = amountCents,
                photoPath = photoPath,
                status = "SETTLED",
                referenceNo = "#BLT-${(100000..999999).random()}-APL"
            )
        )
        Log.i(tag, "Expense saved id=$id amountCents=$amountCents photo=${photoPath != null}")
        val saved = db.expenseDao().findById(id)
        cloud {
            if (saved != null) cloudSync.pushExpense(saved)
            pushBadgesAndScore(userId)
        }
        return Result.success(id)
    }

    fun observeGoal(userId: Long, yearMonth: String = DateUtils.currentYearMonth()) =
        db.budgetGoalDao().observe(userId, yearMonth)

    suspend fun setGoals(userId: Long, minCents: Long, maxCents: Long): Result<Unit> {
        if (minCents < 0 || maxCents < 0) {
            return Result.failure(IllegalArgumentException("Goals must be >= 0"))
        }
        if (minCents > maxCents) {
            return Result.failure(IllegalArgumentException("Minimum cannot exceed maximum"))
        }
        db.budgetGoalDao().upsert(
            BudgetGoalEntity(
                userId = userId,
                yearMonth = DateUtils.currentYearMonth(),
                minGoalCents = minCents,
                maxGoalCents = maxCents
            )
        )
        Log.i(tag, "Goals saved min=$minCents max=$maxCents")
        val goal = db.budgetGoalDao().find(userId, DateUtils.currentYearMonth())
        cloud {
            if (goal != null) cloudSync.pushGoal(goal)
            pushBadgesAndScore(userId)
        }
        return Result.success(Unit)
    }

    fun observeLeaderboard() = db.leaderboardDao().observeAll()

    suspend fun savingsPercent(userId: Long): Int {
        val (start, end) = DateUtils.periodRange(PeriodFilter.THIS_MONTH)
        val received = db.expenseDao().totalReceivedCents(userId, start, end)
        if (received <= 0L) return 0
        val saved = savedAmountCents(userId)
        return ((saved * 100) / received).toInt().coerceAtMost(100)
    }

    /** Money received this month minus money spent this month. */
    suspend fun savedAmountCents(userId: Long): Long {
        val (start, end) = DateUtils.periodRange(PeriodFilter.THIS_MONTH)
        val received = db.expenseDao().totalReceivedCents(userId, start, end)
        val spent = kotlin.math.abs(db.expenseDao().totalSpentCents(userId, start, end))
        return received - spent
    }

    suspend fun pullFromCloud(userId: Long) {
        cloud { pullRemote(userId) }
    }

    suspend fun pastMonthProgress(userId: Long): MonthProgress {
        val (start, end) = DateUtils.periodRange(PeriodFilter.LAST_30_DAYS)
        val spent = abs(db.expenseDao().totalSpentCents(userId, start, end))
        val goal = db.budgetGoalDao().find(userId, DateUtils.currentYearMonth())
        val zone = GoalZone.of(spent, goal?.minGoalCents, goal?.maxGoalCents)
        return MonthProgress(spent, goal?.minGoalCents, goal?.maxGoalCents, zone)
    }

    suspend fun currentBadges(userId: Long): List<Badge> {
        val expenses = db.expenseDao().listForUser(userId)
        val days = expenses.map { it.dateEpochDay }.toSet()
        val photos = expenses.count { !it.photoPath.isNullOrBlank() }
        val goal = db.budgetGoalDao().find(userId, DateUtils.currentYearMonth())
        val (start, end) = DateUtils.periodRange(PeriodFilter.LAST_30_DAYS)
        val spent = abs(db.expenseDao().totalSpentCents(userId, start, end))
        val zone = GoalZone.of(spent, goal?.minGoalCents, goal?.maxGoalCents)
        val today = java.time.LocalDate.now().toEpochDay()
        return BadgeRules.evaluate(
            expenseCount = expenses.size,
            photoCount = photos,
            distinctDays = days,
            todayEpochDay = today,
            hasGoal = goal != null,
            zone = zone
        )
    }

    suspend fun publishScore(userId: Long): String {
        val user = db.userDao().findById(userId) ?: return "Not signed in"
        return try {
            cloudSync.pushLeaderboard(
                userId,
                user.leaderboardUsername,
                savingsPercent(userId),
                savedAmountCents(userId)
            )
            Log.i(tag, "Leaderboard score written to Firestore")
            "Your score is stored online"
        } catch (e: Exception) {
            Log.e(tag, "Leaderboard upload failed: ${e.message}")
            "Online save failed. Check your internet connection."
        }
    }

    private suspend fun pushBadgesAndScore(userId: Long) {
        cloudSync.pushBadges(userId, currentBadges(userId))
        val user = db.userDao().findById(userId) ?: return
        cloudSync.pushLeaderboard(
            userId,
            user.leaderboardUsername,
            savingsPercent(userId),
            savedAmountCents(userId)
        )
    }

    private suspend fun pushLocalSnapshot(userId: Long) {
        db.categoryDao().listForUser(userId).forEach { cloudSync.pushCategory(it) }
        db.expenseDao().listForUser(userId).forEach { cloudSync.pushExpense(it) }
        val goal = db.budgetGoalDao().find(userId, DateUtils.currentYearMonth())
        if (goal != null) cloudSync.pushGoal(goal)
        pushBadgesAndScore(userId)
    }

    private suspend fun pullRemote(userId: Long) {
        cloudSync.pullCategories(userId).forEach { category ->
            if (db.categoryDao().findById(category.id) == null) {
                db.categoryDao().insert(category)
            }
        }
        cloudSync.pullExpenses(userId).forEach { expense ->
            if (db.expenseDao().findById(expense.id) == null) {
                db.expenseDao().insert(expense)
            }
        }
        cloudSync.pullGoals(userId).forEach { goal ->
            db.budgetGoalDao().upsert(goal)
        }
        Log.i(tag, "Pulled Firestore data for user $userId into Room")
    }

    private suspend fun cloud(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(tag, "Firestore sync failed: ${e.message}")
        }
    }
    private suspend fun seedDefaultsForUser(userId: Long) {
        listOf("Software", "Hardware", "Transport", "Revenue", "Yield Payment", "Groceries").forEach { name ->
            db.categoryDao().insert(CategoryEntity(userId = userId, name = name))
        }
        db.budgetGoalDao().upsert(
            BudgetGoalEntity(
                userId = userId,
                yearMonth = DateUtils.currentYearMonth(),
                minGoalCents = 50_000,
                maxGoalCents = 120_000
            )
        )
        if (db.leaderboardDao().count() == 0) {
            db.leaderboardDao().insertAll(
                listOf(
                    LeaderboardEntryEntity(username = "Bowie_Knife78", savingsPercent = 45, savedAmountCents = 450_000),
                    LeaderboardEntryEntity(username = "BlueberryCrane", savingsPercent = 34, savedAmountCents = 340_000),
                    LeaderboardEntryEntity(username = "ZombieBuster", savingsPercent = 31, savedAmountCents = 310_000),
                    LeaderboardEntryEntity(username = "Bigster", savingsPercent = 30, savedAmountCents = 300_000),
                    LeaderboardEntryEntity(username = "LazyTall", savingsPercent = 24, savedAmountCents = 240_000),
                    LeaderboardEntryEntity(username = "Jealous_Crown", savingsPercent = 19, savedAmountCents = 190_000)
                )
            )
        }
    }

    private fun copyPhoto(uri: Uri): String {
        val dir = File(appContext.filesDir, "expense_photos").apply { mkdirs() }
        val out = File(dir, "${UUID.randomUUID()}.jpg")
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not read selected photo")
        return out.absolutePath
    }
}