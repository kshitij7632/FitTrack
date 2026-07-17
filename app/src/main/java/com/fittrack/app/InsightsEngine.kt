package com.fittrack.app

import android.content.Context

class InsightsEngine(private val context: Context) {

    fun getInsights(username: String): List<String> {
        val dbHelper = DatabaseHelper(context)
        val insights = mutableListOf<String>()

        val totalWorkouts = dbHelper.getTotalWorkoutCount(username)
        if (totalWorkouts == 0) {
            insights.add("Log your first workout to get personalized insights!")
            return insights
        }

        val streak = dbHelper.getWorkoutStreak(username)
        if (streak > 3) {
            insights.add("You're on fire! You have maintained a $streak day streak. Keep it up!")
        }

        val mostImproved = dbHelper.getMostImprovedExercise(username)
        if (mostImproved != "N/A") {
            insights.add("Great progress! Your $mostImproved has significantly improved.")
        }

        val leastTrained = dbHelper.getLeastTrainedMuscleGroup(username)
        if (leastTrained != "N/A") {
            insights.add("Balance is key. It looks like you've been skipping $leastTrained. Consider adding it to your next routine.")
        }

        val favMuscle = dbHelper.getFavouriteMuscleGroup(username)
        if (favMuscle != "N/A") {
            val count = dbHelper.getMuscleGroupDistribution(username).find { it.first == favMuscle }?.second ?: 0
            if (count > 0) {
                insights.add("You love training $favMuscle! You've hit it $count times.")
            }
        }
        
        val weeklyCount = dbHelper.getWeeklyWorkoutCount(username)
        if (weeklyCount > 2) {
            insights.add("You've completed $weeklyCount workouts this week. Solid consistency!")
        } else if (weeklyCount == 0 && totalWorkouts > 0) {
            insights.add("You haven't logged any workouts this week. Time to hit the gym!")
        }

        if (insights.isEmpty()) {
            insights.add("Consistency is the key to unlocking your potential.")
        }

        return insights.shuffled().take(3)
    }
}
