package com.fittrack.app

data class ExerciseInfo(
    val id: Int = 0,
    val name: String = "",
    val muscleGroup: String = "",
    val equipment: String = "",
    val difficulty: String = "Intermediate",
    val category: String = "",
    val description: String = "",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val username: String = ""
)
