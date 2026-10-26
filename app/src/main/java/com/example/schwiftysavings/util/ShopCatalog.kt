package com.example.schwiftysavings.util

data class ShopReward(
    val id: String,
    val title: String,
    val detail: String,
    val cost: Int
)

object ShopCatalog {
    val rewards = listOf(
        ShopReward("coffee", "Coffee treat", "A R50 voucher to use at a local coffee shop.", 20),
        ShopReward("movie", "Movie night", "A 20% discount on a movie ticket.", 40),
        ShopReward("takeaway", "Takeaway night", "A R100 voucher for any meal.", 60),
        ShopReward("day_out", "Day out", "A one-time-only 40% discount at any spa of your choice.", 80)
    )

    fun find(id: String): ShopReward? = rewards.find { it.id == id }
}
