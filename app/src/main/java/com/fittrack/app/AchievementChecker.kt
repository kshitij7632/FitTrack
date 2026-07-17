package com.fittrack.app

import android.content.Context

class AchievementChecker(private val context: Context) {

    fun checkAndUnlockAchievements(username: String) {
        val dbHelper = DatabaseHelper(context)
        
        val totalWorkouts = dbHelper.getTotalWorkoutCount(username)
        if (totalWorkouts >= 1) dbHelper.unlockAchievement(username, "first_workout")
        if (totalWorkouts >= 10) dbHelper.unlockAchievement(username, "workouts_10")
        if (totalWorkouts >= 50) dbHelper.unlockAchievement(username, "workouts_50")
        if (totalWorkouts >= 100) dbHelper.unlockAchievement(username, "workouts_100")
        
        val streak = dbHelper.getWorkoutStreak(username)
        val longestStreak = dbHelper.getLongestStreak(username)
        val maxStreak = maxOf(streak, longestStreak)
        
        if (maxStreak >= 7) dbHelper.unlockAchievement(username, "streak_7")
        if (maxStreak >= 30) dbHelper.unlockAchievement(username, "streak_30")
        
        val totalVolume = dbHelper.getTotalWeightLifted(username)
        if (totalVolume >= 1000) dbHelper.unlockAchievement(username, "volume_1000")
        if (totalVolume >= 10000) dbHelper.unlockAchievement(username, "volume_10000")
        if (totalVolume >= 100000) dbHelper.unlockAchievement(username, "volume_100000")
        
        val prs = dbHelper.getPersonalRecords(username)
        val benchPR = prs.find { it.first.lowercase().contains("bench") }?.second ?: 0.0
        val deadliftPR = prs.find { it.first.lowercase().contains("deadlift") }?.second ?: 0.0
        
        if (benchPR >= 100) dbHelper.unlockAchievement(username, "bench_100")
        if (deadliftPR >= 150) dbHelper.unlockAchievement(username, "deadlift_150")
        
        val photos = dbHelper.getProgressPhotoCount(username)
        if (photos >= 1) dbHelper.unlockAchievement(username, "photo_first")
        if (photos >= 10) dbHelper.unlockAchievement(username, "photo_10")
        
        val goal = dbHelper.getGoals(username)
        val completion = dbHelper.getWeeklyCompletionPercentage(username, goal.weeklyWorkoutGoal)
        if (completion >= 100) dbHelper.unlockAchievement(username, "weekly_goal")
    }

    fun getAllAchievements(username: String): List<Achievement> {
        val dbHelper = DatabaseHelper(context)
        val unlockedKeys = dbHelper.getUnlockedAchievements(username)
        
        return listOf(
            Achievement("first_workout", "🥇 First Workout", "Log your first workout", "ic_workout", unlockedKeys.contains("first_workout")),
            Achievement("streak_7", "🔥 7-Day Streak", "Maintain a workout streak for 7 days", "ic_fire", unlockedKeys.contains("streak_7")),
            Achievement("streak_30", "🔥 30-Day Streak", "Maintain a workout streak for 30 days", "ic_fire", unlockedKeys.contains("streak_30")),
            Achievement("workouts_10", "🏋️ 10 Workouts", "Log 10 total workouts", "ic_fitness", unlockedKeys.contains("workouts_10")),
            Achievement("workouts_50", "🏋️ 50 Workouts", "Log 50 total workouts", "ic_fitness", unlockedKeys.contains("workouts_50")),
            Achievement("workouts_100", "🏋️ 100 Workouts", "Log 100 total workouts", "ic_fitness", unlockedKeys.contains("workouts_100")),
            Achievement("volume_1000", "💪 1,000 kg Volume", "Lift a total of 1,000 kg", "ic_dumbbell", unlockedKeys.contains("volume_1000")),
            Achievement("volume_10000", "💯 10,000 kg Volume", "Lift a total of 10,000 kg", "ic_dumbbell", unlockedKeys.contains("volume_10000")),
            Achievement("volume_100000", "🏆 100,000 kg Volume", "Lift a total of 100,000 kg", "ic_trophy", unlockedKeys.contains("volume_100000")),
            Achievement("bench_100", "💪 Bench Press 100kg", "Reach a 100kg Personal Record on Bench Press", "ic_dumbbell", unlockedKeys.contains("bench_100")),
            Achievement("deadlift_150", "🏆 Deadlift 150kg", "Reach a 150kg Personal Record on Deadlift", "ic_trophy", unlockedKeys.contains("deadlift_150")),
            Achievement("weekly_goal", "🎯 Weekly Goal", "Complete your weekly workout goal", "ic_goal", unlockedKeys.contains("weekly_goal")),
            Achievement("photo_first", "📸 First Photo", "Upload your first progress photo", "ic_gallery", unlockedKeys.contains("photo_first")),
            Achievement("photo_10", "📸 Photo Journal", "Upload 10 progress photos", "ic_gallery", unlockedKeys.contains("photo_10"))
        )
    }
}
