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

class SchwiftyRepository(
    private val db: SchwiftyDatabase,
    private val sessionStore: SessionStore,
    private val appContext: Context
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
        Log.i(tag, "Registered user id=$id")
        return Result.success(id)
    }

    suspend fun login(username: String, password: String): Result<Long> {
        val user = db.userDao().findByUsername(username.trim())
            ?: return Result.failure(IllegalArgumentException("Invalid username or password"))
        if (!PasswordHasher.matches(password, user.salt, user.passwordHash)) {
            Log.w(tag, "Login failed for ${username.trim()}")
            return Result.failure(IllegalArgumentException("Invalid username or password"))
        }
        sessionStore.setUserId(user.id)
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
            return Result.failure(IllegalArgumentException("End time must be after start time"))
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
        return Result.success(Unit)
    }

    fun observeLeaderboard() = db.leaderboardDao().observeAll()

    suspend fun savingsPercent(userId: Long): Int {
        val goal = db.budgetGoalDao().find(userId, DateUtils.currentYearMonth()) ?: return 0
        val (start, end) = DateUtils.periodRange(PeriodFilter.THIS_MONTH)
        val spentAbs = kotlin.math.abs(db.expenseDao().totalSpentCents(userId, start, end))
        if (goal.maxGoalCents <= 0L) return 0
        val remaining = (goal.maxGoalCents - spentAbs).coerceAtLeast(0)
        return ((remaining * 100) / goal.maxGoalCents).toInt().coerceIn(0, 100)
    }

    suspend fun savedAmountCents(userId: Long): Long {
        val goal = db.budgetGoalDao().find(userId, DateUtils.currentYearMonth()) ?: return 0
        val (start, end) = DateUtils.periodRange(PeriodFilter.THIS_MONTH)
        val spentAbs = kotlin.math.abs(db.expenseDao().totalSpentCents(userId, start, end))
        return (goal.maxGoalCents - spentAbs).coerceAtLeast(0)
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