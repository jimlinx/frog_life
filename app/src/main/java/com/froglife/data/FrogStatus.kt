package com.froglife.data

enum class FrogStatus(val displayName: String, val defaultThreshold: Int) {
    ROCK("Rock", 10),
    COPPER("Copper", 50),
    BRONZE("Bronze", 100),
    SILVER("Silver", 200),
    GOLD("Gold", 400),
    DIAMOND("Diamond", 800)
}
