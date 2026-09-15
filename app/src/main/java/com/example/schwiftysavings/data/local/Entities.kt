package com.example.schwiftysavings.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val displayName: String,
    val leaderboardUsername: String
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index(value = ["userId", "name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("userId"), Index("categoryId"), Index("dateEpochDay")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val description: String,
    val merchantName: String,
    /** Days since Unix epoch (LocalDate.toEpochDay). */
    val dateEpochDay: Long,
    /** Minutes from midnight — start time. */
    val startMinute: Int,
    /** Minutes from midnight — end time. */
    val endMinute: Int,
    /** Amount in cents. Negative = expense, positive = income. */
    val amountCents: Long,
    val photoPath: String? = null,
    val status: String = "SETTLED",
    val referenceNo: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budget_goals",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "yearMonth"], unique = true)]
)
data class BudgetGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    /** Format: YYYY-MM */
    val yearMonth: String,
    val minGoalCents: Long,
    val maxGoalCents: Long
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val savingsPercent: Int,
    val savedAmountCents: Long,
    val isCurrentUserPlaceholder: Boolean = false
)