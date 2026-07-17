package com.fittrack.app

data class Workout(
    val id: Int = 0,
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val weight: Double = 0.0,
    val sets: Int = 0,
    val reps: Int = 0,
    val duration: Int = 0,
    val date: String = "",
    val notes: String = "",
    val imagePath: String = "",
    val username: String = ""
)
