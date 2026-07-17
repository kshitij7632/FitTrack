package com.fittrack.app

data class ExerciseProgress(
    val exerciseName: String,
    val currentWeight: Double,
    val previousWeight: Double,
    val bestWeight: Double,
    val improvementPercent: Double,
    val frequency: Int,
    val estimatedOneRepMax: Double,
    val history: List<Pair<String, Double>> = emptyList()
)
