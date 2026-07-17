package com.fittrack.app

import android.content.Context

class FitnessScoreCalculator(private val context: Context) {

    fun calculateScore(username: String): Int {
        val dbHelper = DatabaseHelper(context)
        
        val consistencyScore = dbHelper.getConsistencyScore(username)
        val frequencyScore = calculateFrequencyScore(dbHelper, username)
        val durationScore = calculateDurationScore(dbHelper, username)
        val volumeScore = calculateVolumeScore(dbHelper, username)

        // Weights: Frequency 25%, Consistency 25%, Duration 25%, Volume 25%
        val totalScore = (frequencyScore * 0.25) + (consistencyScore * 0.25) + (durationScore * 0.25) + (volumeScore * 0.25)
        
        return totalScore.toInt().coerceIn(0, 100)
    }

    private fun calculateFrequencyScore(dbHelper: DatabaseHelper, username: String): Double {
        val monthlyWorkouts = dbHelper.getMonthlyWorkoutCount(username)
        // 16 workouts per month (4 per week) = 100%
        val score = (monthlyWorkouts / 16.0) * 100.0
        return score.coerceIn(0.0, 100.0)
    }

    private fun calculateDurationScore(dbHelper: DatabaseHelper, username: String): Double {
        val avgDuration = dbHelper.getAverageWorkoutDuration(username)
        // 60 mins avg = 100%
        val score = (avgDuration / 60.0) * 100.0
        return score.coerceIn(0.0, 100.0)
    }

    private fun calculateVolumeScore(dbHelper: DatabaseHelper, username: String): Double {
        val totalVolume = dbHelper.getTotalWeightLifted(username)
        val months = (dbHelper.getWorkoutDatesSet(username).size / 4.0).coerceAtLeast(1.0) 
        val volumePerMonth = totalVolume / months
        // arbitrary target of 20000kg per month for 100%
        val score = (volumePerMonth / 20000.0) * 100.0
        return score.coerceIn(0.0, 100.0)
    }
}
