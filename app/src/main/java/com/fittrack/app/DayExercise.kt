package com.fittrack.app

data class DayExercise(
    val id: Int = 0,
    val dayId: Int = 0,
    val exerciseName: String = "",
    val muscleGroup: String = "",
    val equipment: String = "Barbell",
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val sortOrder: Int = 0,
    val notes: String = "",
    val isFavorite: Boolean = false
)
