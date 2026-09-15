package com.example.schwiftysavings.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class CategoryTotalRow(
    val categoryId: Long,
    val categoryName: String,
    val totalCents: Long
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Update
    suspend fun update(user: UserEntity)
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity): Long

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun observeForUser(userId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun listForUser(userId: Long): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): CategoryEntity?
}

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Query(
        """
        SELECT * FROM expenses
        WHERE userId = :userId
          AND dateEpochDay BETWEEN :startDay AND :endDay
        ORDER BY dateEpochDay DESC, startMinute DESC
        """
    )
    fun observeInPeriod(userId: Long, startDay: Long, endDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ExpenseEntity?

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS categoryName, SUM(e.amountCents) AS totalCents
        FROM expenses e
        INNER JOIN categories c ON c.id = e.categoryId
        WHERE e.userId = :userId
          AND e.dateEpochDay BETWEEN :startDay AND :endDay
          AND e.amountCents < 0
        GROUP BY c.id, c.name
        ORDER BY totalCents ASC
        """
    )
    suspend fun categoryTotals(userId: Long, startDay: Long, endDay: Long): List<CategoryTotalRow>

    @Query(
        """
        SELECT COALESCE(SUM(amountCents), 0) FROM expenses
        WHERE userId = :userId AND amountCents < 0
          AND dateEpochDay BETWEEN :startDay AND :endDay
        """
    )
    suspend fun totalSpentCents(userId: Long, startDay: Long, endDay: Long): Long

    @Query(
        """
        SELECT COALESCE(SUM(amountCents), 0) FROM expenses
        WHERE userId = :userId
        """
    )
    fun observeNetBalance(userId: Long): Flow<Long>
}

@Dao
interface BudgetGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: BudgetGoalEntity): Long

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun find(userId: Long, yearMonth: String): BudgetGoalEntity?

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND yearMonth = :yearMonth LIMIT 1")
    fun observe(userId: Long, yearMonth: String): Flow<BudgetGoalEntity?>
}

@Dao
interface LeaderboardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LeaderboardEntryEntity>)

    @Query("SELECT COUNT(*) FROM leaderboard_entries")
    suspend fun count(): Int

    @Query("SELECT * FROM leaderboard_entries ORDER BY savingsPercent DESC")
    fun observeAll(): Flow<List<LeaderboardEntryEntity>>
}