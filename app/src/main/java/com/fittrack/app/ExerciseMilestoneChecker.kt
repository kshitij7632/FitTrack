package com.fittrack.app

import android.content.Context

/**
 * Checks and unlocks per-exercise milestones based on session history.
 * Called automatically after each workout session is saved.
 */
class ExerciseMilestoneChecker(private val context: Context) {

    private val db by lazy { DatabaseHelper(context) }

    data class Milestone(
        val key: String,
        val title: String,
        val description: String,
        val emoji: String,
        val isUnlocked: Boolean,
        val unlockedAt: String = ""
    )

    fun checkAndUnlockMilestones(username: String, exerciseName: String) {
        val history = db.getExerciseSessionHistory(username, exerciseName)
        val legacyCount = db.getWeightProgressForExercise(username, exerciseName).size
        val totalSessions = history.map { it.sessionId }.distinct().size + legacyCount

        // Session count milestones
        if (totalSessions >= 1)   db.unlockExerciseMilestone(username, exerciseName, "session_1")
        if (totalSessions >= 10)  db.unlockExerciseMilestone(username, exerciseName, "session_10")
        if (totalSessions >= 25)  db.unlockExerciseMilestone(username, exerciseName, "session_25")
        if (totalSessions >= 50)  db.unlockExerciseMilestone(username, exerciseName, "session_50")
        if (totalSessions >= 100) db.unlockExerciseMilestone(username, exerciseName, "session_100")

        // Weight milestones
        val allWeights = history.map { it.weight } + db.getWeightProgressForExercise(username, exerciseName).map { it.second }
        val bestWeight = if (allWeights.isNotEmpty()) allWeights.max() else 0.0
        if (bestWeight >= 60)  db.unlockExerciseMilestone(username, exerciseName, "weight_60")
        if (bestWeight >= 100) db.unlockExerciseMilestone(username, exerciseName, "weight_100")
        if (bestWeight >= 140) db.unlockExerciseMilestone(username, exerciseName, "weight_140")
        if (bestWeight >= 200) db.unlockExerciseMilestone(username, exerciseName, "weight_200")

        // Volume milestones
        val totalVolume = history.sumOf { it.weight * it.sets * it.reps }
        if (totalVolume >= 1_000)   db.unlockExerciseMilestone(username, exerciseName, "volume_1000")
        if (totalVolume >= 5_000)   db.unlockExerciseMilestone(username, exerciseName, "volume_5000")
        if (totalVolume >= 10_000)  db.unlockExerciseMilestone(username, exerciseName, "volume_10000")
        if (totalVolume >= 50_000)  db.unlockExerciseMilestone(username, exerciseName, "volume_50000")
        if (totalVolume >= 100_000) db.unlockExerciseMilestone(username, exerciseName, "volume_100000")
    }

    fun checkAllExercises(username: String) {
        val allExercises = db.getAllExerciseNames(username)
        for (exercise in allExercises) checkAndUnlockMilestones(username, exercise)
    }

    fun getMilestonesForExercise(username: String, exerciseName: String): List<Milestone> {
        val unlocked = db.getMilestonesForExercise(username, exerciseName)
        val allMilestones = listOf(
            Triple("session_1",    "🥇 First Rep", "Performed this exercise for the first time"),
            Triple("session_10",   "💪 10 Sessions", "Completed 10 sessions with this exercise"),
            Triple("session_25",   "🏋️ 25 Sessions", "Completed 25 sessions with this exercise"),
            Triple("session_50",   "⭐ 50 Sessions", "Completed 50 sessions — you're dedicated!"),
            Triple("session_100",  "🏆 100 Sessions", "100 sessions — absolute mastery!"),
            Triple("weight_60",    "🔢 60 kg Lift", "Lifted 60 kg on this exercise"),
            Triple("weight_100",   "💯 100 kg Club", "Reached 100 kg — a major milestone!"),
            Triple("weight_140",   "🔥 140 kg Beast", "Lifted 140 kg — elite level!"),
            Triple("weight_200",   "👑 200 kg Legend", "Lifted 200 kg — truly legendary!"),
            Triple("volume_1000",  "📦 1,000 kg Volume", "Accumulated 1,000 kg total volume"),
            Triple("volume_5000",  "🏋️ 5,000 kg Volume", "Accumulated 5,000 kg total volume"),
            Triple("volume_10000", "💪 10,000 kg Volume", "Accumulated 10,000 kg total volume"),
            Triple("volume_50000", "⭐ 50,000 kg Volume", "Accumulated 50,000 kg total volume"),
            Triple("volume_100000","🏆 100,000 kg Volume", "Accumulated 100,000 kg — legendary!")
        )
        return allMilestones.map { (key, title, desc) ->
            val unlockedAt = unlocked[key] ?: ""
            Milestone(key, title, desc, title.first().toString(), unlockedAt.isNotEmpty(), unlockedAt)
        }
    }
}
