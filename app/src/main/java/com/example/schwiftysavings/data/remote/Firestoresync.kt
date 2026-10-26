package com.example.schwiftysavings.data.remote

import android.util.Log
import com.example.schwiftysavings.data.local.BudgetGoalEntity
import com.example.schwiftysavings.data.local.CategoryEntity
import com.example.schwiftysavings.data.local.ExpenseEntity
import com.example.schwiftysavings.data.local.UserEntity
import com.example.schwiftysavings.util.Badge
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Reads and writes Schwifty Savings data in Cloud Firestore.
 * Password values stored here are already hashed by PasswordHasher.
 * Pattern adapted from Firebase, 2026. Add data to Cloud Firestore.
 * https://firebase.google.com/docs/firestore/manage-data/add-data
 */
class FirestoreSync {
    private val tag = "FirestoreSync"
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun pushUser(user: UserEntity) {
        db.collection("users").document(user.id.toString()).set(
            mapOf(
                "id" to user.id,
                "username" to user.username,
                "passwordHash" to user.passwordHash,
                "salt" to user.salt,
                "displayName" to user.displayName,
                "leaderboardUsername" to user.leaderboardUsername
            )
        ).await()
        Log.i(tag, "Wrote user ${user.id} to Firestore")
    }

    suspend fun findUser(username: String): UserEntity? {
        val snap = db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()
        val doc = snap.documents.firstOrNull() ?: return null
        Log.i(tag, "Read user $username from Firestore")
        return UserEntity(
            id = doc.getLong("id") ?: return null,
            username = doc.getString("username") ?: username,
            passwordHash = doc.getString("passwordHash") ?: return null,
            salt = doc.getString("salt") ?: return null,
            displayName = doc.getString("displayName") ?: username,
            leaderboardUsername = doc.getString("leaderboardUsername") ?: username
        )
    }

    suspend fun pushCategory(category: CategoryEntity) {
        val docId = "${category.userId}_${category.id}"
        db.collection("categories").document(docId).set(
            mapOf(
                "id" to category.id,
                "userId" to category.userId,
                "name" to category.name,
                "createdAt" to category.createdAt
            )
        ).await()
        Log.i(tag, "Wrote category $docId to Firestore")
    }

    suspend fun pullCategories(userId: Long): List<CategoryEntity> {
        val snap = db.collection("categories")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        Log.i(tag, "Read ${snap.size()} categories for user $userId")
        return snap.documents.mapNotNull { doc ->
            val id = doc.getLong("id") ?: return@mapNotNull null
            CategoryEntity(
                id = id,
                userId = doc.getLong("userId") ?: userId,
                name = doc.getString("name") ?: return@mapNotNull null,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun pushExpense(expense: ExpenseEntity) {
        val docId = "${expense.userId}_${expense.id}"
        db.collection("expenses").document(docId).set(
            mapOf(
                "id" to expense.id,
                "userId" to expense.userId,
                "categoryId" to expense.categoryId,
                "description" to expense.description,
                "merchantName" to expense.merchantName,
                "dateEpochDay" to expense.dateEpochDay,
                "startMinute" to expense.startMinute,
                "endMinute" to expense.endMinute,
                "amountCents" to expense.amountCents,
                "photoPath" to expense.photoPath,
                "status" to expense.status,
                "referenceNo" to expense.referenceNo,
                "createdAt" to expense.createdAt
            )
        ).await()
        Log.i(tag, "Wrote expense $docId to Firestore")
    }

    suspend fun pullExpenses(userId: Long): List<ExpenseEntity> {
        val snap = db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        Log.i(tag, "Read ${snap.size()} expenses for user $userId")
        return snap.documents.mapNotNull { doc ->
            val id = doc.getLong("id") ?: return@mapNotNull null
            ExpenseEntity(
                id = id,
                userId = doc.getLong("userId") ?: userId,
                categoryId = doc.getLong("categoryId") ?: return@mapNotNull null,
                description = doc.getString("description") ?: "",
                merchantName = doc.getString("merchantName") ?: "",
                dateEpochDay = doc.getLong("dateEpochDay") ?: return@mapNotNull null,
                startMinute = doc.getLong("startMinute")?.toInt() ?: 0,
                endMinute = doc.getLong("endMinute")?.toInt() ?: 0,
                amountCents = doc.getLong("amountCents") ?: 0L,
                photoPath = doc.getString("photoPath"),
                status = doc.getString("status") ?: "SETTLED",
                referenceNo = doc.getString("referenceNo") ?: "",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun pushGoal(goal: BudgetGoalEntity) {
        val docId = "${goal.userId}_${goal.yearMonth}"
        db.collection("goals").document(docId).set(
            mapOf(
                "id" to goal.id,
                "userId" to goal.userId,
                "yearMonth" to goal.yearMonth,
                "minGoalCents" to goal.minGoalCents,
                "maxGoalCents" to goal.maxGoalCents
            )
        ).await()
        Log.i(tag, "Wrote goal $docId to Firestore")
    }

    suspend fun pullGoals(userId: Long): List<BudgetGoalEntity> {
        val snap = db.collection("goals")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        Log.i(tag, "Read ${snap.size()} goals for user $userId")
        return snap.documents.mapNotNull { doc ->
            BudgetGoalEntity(
                id = doc.getLong("id") ?: 0L,
                userId = doc.getLong("userId") ?: userId,
                yearMonth = doc.getString("yearMonth") ?: return@mapNotNull null,
                minGoalCents = doc.getLong("minGoalCents") ?: 0L,
                maxGoalCents = doc.getLong("maxGoalCents") ?: 0L
            )
        }
    }

    suspend fun pushBadges(userId: Long, badges: List<Badge>) {
        db.collection("badges").document(userId.toString()).set(
            mapOf(
                "userId" to userId,
                "badges" to badges.map { badge ->
                    mapOf(
                        "id" to badge.id,
                        "title" to badge.title,
                        "detail" to badge.detail,
                        "unlocked" to badge.unlocked
                    )
                }
            )
        ).await()
        Log.i(tag, "Wrote badges for user $userId to Firestore")
    }

    suspend fun pushLeaderboard(userId: Long, username: String, percent: Int, savedCents: Long) {
        db.collection("leaderboard").document(userId.toString()).set(
            mapOf(
                "userId" to userId,
                "username" to username,
                "savingsPercent" to percent,
                "savedAmountCents" to savedCents
            )
        ).await()
        Log.i(tag, "Wrote leaderboard score for user $userId")
    }
}