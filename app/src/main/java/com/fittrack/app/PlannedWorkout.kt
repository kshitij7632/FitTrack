package com.fittrack.app

data class PlannedWorkout(
    val id: Int = 0,
    val username: String = "",
    val date: String = "",
    val workoutDayId: Int = 0,
    val workoutDayName: String = "",
    val status: String = "planned", // planned | completed | missed | rest | skipped
    val sessionId: Int = 0,
    val notes: String = ""
)
