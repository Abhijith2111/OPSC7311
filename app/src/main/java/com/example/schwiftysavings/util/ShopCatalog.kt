package com.example.schwiftysavings.util

data class ShopReward(
    val id: String,
    val title: String,
    val detail: String,
    val cost: Int
)

object ShopCatalog {
    val rewards = listOf(
        ShopReward("coffee", "Coffee treat", "Redeem a coffee on the app.", 20),
        ShopReward("movie", "Movie night", "Redeem a movie night.", 40),
        ShopReward("takeaway", "Takeaway night", "Redeem a takeaway meal.", 60),
        ShopReward("day_out", "Day out", "Redeem a day out.", 80)
    )

    fun find(id: String): ShopReward? = rewards.find { it.id == id }
}
