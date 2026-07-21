package com.fittrack.app

import android.content.Context

/**
 * Adaptive, rule-based insights engine. Analyzes both workout sessions and body
 * measurements locally. No internet or AI required.
 */
class AdaptiveInsightsEngine(private val context: Context) {

    private val db by lazy { DatabaseHelper(context) }
    private val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

    fun getInsights(username: String): List<Insight> {
        val insights = mutableListOf<Insight>()

        val totalWorkouts = db.getTotalWorkoutCount(username) + db.getTotalSessionCount(username)
        if (totalWorkouts == 0) {
            return listOf(Insight("🚀", "Start Your Journey", "Log your first workout or set up your training plan to unlock personalized insights!", InsightType.MOTIVATION))
        }

        // ── STREAK INSIGHTS ───────────────────────────────────────────────
        val streak = db.getWorkoutStreak(username)
        when {
            streak >= 30 -> insights.add(Insight("🔥", "30-Day Streak!", "You've maintained a $streak day workout streak. Absolutely elite dedication!", InsightType.ACHIEVEMENT))
            streak >= 14 -> insights.add(Insight("🔥", "2-Week Streak!", "You've maintained a $streak day streak. Consistency is building your physique!", InsightType.ACHIEVEMENT))
            streak >= 7  -> insights.add(Insight("🔥", "7-Day Streak!", "A full week of workouts! You're building a powerful habit.", InsightType.ACHIEVEMENT))
            streak >= 3  -> insights.add(Insight("🔥", "$streak-Day Streak", "Keep going — you're building momentum!", InsightType.MOTIVATION))
            totalWorkouts > 0 && streak == 0 -> insights.add(Insight("⚡", "Break the Rest", "You haven't worked out today. Yesterday's you made plans — today's you delivers.", InsightType.MOTIVATION))
        }

        // ── WEEKLY TRAINING INSIGHTS ──────────────────────────────────────
        val weeklyCount = db.getWeeklyWorkoutCount(username)
        val goals = db.getGoals(username)
        when {
            weeklyCount == 0 && totalWorkouts > 0 -> insights.add(Insight("📅", "No Workouts This Week", "You haven't logged any workouts this week. Time to get back on track!", InsightType.WARNING))
            weeklyCount >= goals.weeklyWorkoutGoal && goals.weeklyWorkoutGoal > 0 -> insights.add(Insight("🎯", "Weekly Goal Achieved!", "You've completed all $weeklyCount planned workouts this week. Outstanding!", InsightType.ACHIEVEMENT))
            weeklyCount >= 5 -> insights.add(Insight("💪", "Excellent Week", "You've completed $weeklyCount workouts this week. That's incredible dedication!", InsightType.POSITIVE))
            weeklyCount >= 3 -> insights.add(Insight("✅", "Good Week", "You've completed $weeklyCount workouts this week. Solid consistency!", InsightType.POSITIVE))
        }

        // ── MUSCLE GROUP BALANCE INSIGHTS ────────────────────────────────
        val muscleDistribution = db.getMuscleGroupDistribution(username)
        if (muscleDistribution.size >= 2) {
            val bicepsCount = muscleDistribution.find { it.first == "Biceps" }?.second ?: 0
            val tricepsCount = muscleDistribution.find { it.first == "Triceps" }?.second ?: 0
            if (bicepsCount > 0 && tricepsCount > 0) {
                when {
                    bicepsCount > tricepsCount * 2 -> insights.add(Insight("⚖️", "Muscle Imbalance", "Your Biceps are trained ${bicepsCount - tricepsCount}x more than Triceps. Add more tricep work for balanced arms.", InsightType.WARNING))
                    tricepsCount > bicepsCount * 2 -> insights.add(Insight("⚖️", "Muscle Imbalance", "Your Triceps are trained more than Biceps. Balance your arm training for optimal development.", InsightType.WARNING))
                }
            }
            val leastTrained = db.getLeastTrainedMuscleGroup(username)
            if (leastTrained != "N/A" && leastTrained != "Other") {
                val daysSince = db.getDaysSinceLastTrainedMuscle(username, leastTrained)
                if (daysSince > 14) insights.add(Insight("⚠️", "Neglected Muscle", "You haven't trained $leastTrained in $daysSince days. Add it to your next workout for balanced development.", InsightType.WARNING))
                else if (daysSince > 7) insights.add(Insight("📌", "$leastTrained Reminder", "It's been $daysSince days since you trained $leastTrained. Consider scheduling a session.", InsightType.INFO))
            }
        }

        // ── EXERCISE PROGRESS INSIGHTS ────────────────────────────────────
        val mostImproved = db.getMostImprovedExercise(username)
        if (mostImproved != "N/A") {
            val improvement = db.getExerciseWeightImprovementLastMonth(username, mostImproved)
            if (improvement >= 5.0) {
                insights.add(Insight("📈", "Strength Surge", "Your $mostImproved has improved by +${String.format("%.1f", improvement)} kg over the last month. Outstanding!", InsightType.ACHIEVEMENT))
            } else if (improvement > 0) {
                insights.add(Insight("📈", "Progress Detected", "Your $mostImproved is improving. Stay consistent!", InsightType.POSITIVE))
            }
        }

        // ── BODY WEIGHT CHANGE ────────────────────────────────────────────
        val bodyWeightChange = db.getBodyWeightChangeThisMonth(username)
        when {
            bodyWeightChange < -0.5 -> insights.add(Insight("⬇️", "Weight Reduction", "Your body weight decreased by ${String.format("%.1f", Math.abs(bodyWeightChange))} kg this month. Great progress towards your goal!", InsightType.POSITIVE))
            bodyWeightChange > 1.0  -> insights.add(Insight("⬆️", "Weight Increase", "Your body weight increased by ${String.format("%.1f", bodyWeightChange)} kg this month. Check your nutrition if cutting.", InsightType.INFO))
            bodyWeightChange > 0.3  -> insights.add(Insight("💪", "Gaining Mass", "You've gained ${String.format("%.1f", bodyWeightChange)} kg this month. On track for muscle growth!", InsightType.POSITIVE))
        }

        // ── VOLUME INSIGHTS ───────────────────────────────────────────────
        val totalVolume = db.getTotalWeightLifted(username)
        when {
            totalVolume >= 1_000_000 -> insights.add(Insight("🏆", "1 Million kg Club", "You've lifted over 1,000,000 kg in total. Absolutely legendary!", InsightType.ACHIEVEMENT))
            totalVolume >= 100_000  -> insights.add(Insight("💯", "100,000 kg Milestone", "You've crossed 100,000 kg total volume. Elite performance!", InsightType.ACHIEVEMENT))
            totalVolume >= 10_000   -> insights.add(Insight("💪", "10,000 kg Club", "You've lifted over 10,000 kg total. You're officially serious!", InsightType.ACHIEVEMENT))
        }

        // ── RECENT MEASUREMENTS ───────────────────────────────────────────
        val latest = db.getLatestMeasurement(username)
        val previous = db.getPreviousMeasurement(username)
        if (latest != null && previous != null) {
            val chestDiff = latest.chest - previous.chest
            if (chestDiff > 0.5) insights.add(Insight("📏", "Chest Growth", "Your chest measurement has increased by +${String.format("%.1f", chestDiff)} cm. Your training is working!", InsightType.POSITIVE))
            val waistDiff = latest.waist - previous.waist
            if (waistDiff < -0.5) insights.add(Insight("📏", "Waist Reduction", "Your waist measurement decreased by ${String.format("%.1f", Math.abs(waistDiff))} cm. Looking leaner!", InsightType.POSITIVE))
        }

        // ── FAVOURITE MUSCLE ──────────────────────────────────────────────
        val favMuscle = db.getFavouriteMuscleGroup(username)
        if (favMuscle != "N/A") {
            val count = db.getMuscleGroupDistribution(username).find { it.first == favMuscle }?.second ?: 0
            if (count >= 10) insights.add(Insight("🎯", "$favMuscle Specialist", "You love training $favMuscle — you've hit it $count times. Make sure to balance with other groups!", InsightType.INFO))
        }

        // ── PERSONAL RECORD INSIGHTS ──────────────────────────────────────
        val prsCount = db.getPersonalRecordsCount(username)
        if (prsCount >= 10) insights.add(Insight("⭐", "Record Breaker", "You've set personal records on $prsCount different exercises. Keep pushing your limits!", InsightType.POSITIVE))

        return insights.shuffled().take(5)
    }

    data class Insight(
        val emoji: String,
        val title: String,
        val message: String,
        val type: InsightType
    )

    enum class InsightType { ACHIEVEMENT, POSITIVE, MOTIVATION, WARNING, INFO }
}
