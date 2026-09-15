package com.example.schwiftysavings.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        BudgetGoalEntity::class,
        LeaderboardEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SchwiftyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun leaderboardDao(): LeaderboardDao

    companion object {
        private const val TAG = "SchwiftyDB"
        @Volatile
        private var INSTANCE: SchwiftyDatabase? = null

        fun getInstance(context: Context): SchwiftyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SchwiftyDatabase::class.java,
                    "schwifty_savings.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also {
                        INSTANCE = it
                        Log.d(TAG, "Room database schwifty_savings.db ready")
                    }
            }
        }
    }
}