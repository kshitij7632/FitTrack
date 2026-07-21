package com.fittrack.app

data class WorkoutSession(
    val id: Int = 0,
    val username: String = "",
    val workoutDayId: Int = 0,
    val workoutDayName: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val durationMinutes: Int = 0,
    val totalVolume: Double = 0.0,
    val notes: String = "",
    val mood: Int = 0,           // 1–5
    val energyLevel: Int = 0,    // 1–5
    val sleepQuality: Int = 0,   // 1–5
    val status: String = "completed", // completed | missed | skipped
    val caloriesBurned: Int = 0
)
