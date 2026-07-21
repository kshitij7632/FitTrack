package com.fittrack.app

data class WorkoutDay(
    val id: Int = 0,
    val splitId: Int = 0,
    val dayName: String = "",
    val muscleGroups: String = "",
    val estimatedDuration: Int = 60,
    val notes: String = "",
    val sortOrder: Int = 0
)
