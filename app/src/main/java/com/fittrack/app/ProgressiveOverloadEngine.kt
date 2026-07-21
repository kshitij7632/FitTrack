package com.fittrack.app

import android.content.Context

/**
 * Suggests the next training weight based on the user's recent performance.
 * All calculations done locally using SQLite data.
 */
class ProgressiveOverloadEngine(private val context: Context) {

    private val db by lazy { DatabaseHelper(context) }

    data class OverloadSuggestion(
        val exerciseName: String,
        val lastWeight: Double,
        val lastSets: Int,
        val lastReps: Int,
        val suggestedWeight: Double,
        val suggestedSets: Int,
        val suggestedReps: Int,
        val improvementKg: Double,
        val improvementPercent: Double,
        val recommendation: String  // "increase" | "maintain" | "deload"
    )

    fun getSuggestion(username: String, exerciseName: String): OverloadSuggestion {
        val history = db.getExerciseSessionHistory(username, exerciseName)
        val legacyHistory = db.getWeightProgressForExercise(username, exerciseName)

        // No history → suggest starting weight
        if (history.isEmpty() && legacyHistory.isEmpty()) {
            return OverloadSuggestion(exerciseName, 0.0, 3, 10, 0.0, 3, 10, 0.0, 0.0, "start")
        }

        val lastLog = history.firstOrNull()
        val lastWeight = lastLog?.weight ?: legacyHistory.lastOrNull()?.second ?: 0.0
        val lastSets = lastLog?.sets ?: 3
        val lastReps = lastLog?.reps ?: 10

        // Determine if last session hit the target (3 sets, 10+ reps at weight)
        val hitTarget = lastSets >= 3 && lastReps >= 10

        val (suggestedWeight, recommendation) = when {
            hitTarget -> {
                // Progressive overload: add 2.5kg for isolation, 5kg for compounds
                val isCompound = isCompoundExercise(exerciseName)
                val increment = if (isCompound) 5.0 else 2.5
                Pair(lastWeight + increment, "increase")
            }
            lastReps >= 8 -> Pair(lastWeight, "maintain")  // Good — keep weight, aim for more reps
            lastReps < 6  -> Pair(lastWeight * 0.9, "deload") // Deload 10%
            else           -> Pair(lastWeight, "maintain")
        }

        val impKg = suggestedWeight - lastWeight
        val impPct = if (lastWeight > 0) (impKg / lastWeight) * 100 else 0.0

        return OverloadSuggestion(
            exerciseName = exerciseName,
            lastWeight = lastWeight,
            lastSets = lastSets,
            lastReps = lastReps,
            suggestedWeight = suggestedWeight,
            suggestedSets = 3,
            suggestedReps = if (recommendation == "increase") 8 else 10,
            improvementKg = impKg,
            improvementPercent = impPct,
            recommendation = recommendation
        )
    }

    fun getSuggestionsForDay(username: String, dayId: Int): List<OverloadSuggestion> {
        val exercises = db.getDayExercisesForDay(dayId)
        return exercises.map { getSuggestion(username, it.exerciseName) }
    }

    private fun isCompoundExercise(name: String): Boolean {
        val compoundKeywords = listOf("squat", "deadlift", "bench", "press", "row", "pull-up", "chin", "dip", "lunge", "thrust")
        return compoundKeywords.any { name.lowercase().contains(it) }
    }
}
