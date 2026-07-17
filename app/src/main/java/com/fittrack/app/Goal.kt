package com.fittrack.app

data class Goal(
    val weeklyWorkoutGoal: Int = 4,
    val monthlyWorkoutGoal: Int = 16,
    val targetWeight: Double = 0.0,
    val targetDuration: Int = 60
)
