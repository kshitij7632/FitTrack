package com.fittrack.app

data class SessionExerciseLog(
    val id: Int = 0,
    val sessionId: Int = 0,
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val weight: Double = 0.0,
    val sets: Int = 0,
    val reps: Int = 0,
    val notes: String = "",
    val isNewPR: Boolean = false,
    val sortOrder: Int = 0,
    // Joined from session for display purposes
    val date: String = ""
)
