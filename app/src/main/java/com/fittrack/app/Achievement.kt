package com.fittrack.app

data class Achievement(
    val key: String,
    val name: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val unlockedDate: String = ""
)
