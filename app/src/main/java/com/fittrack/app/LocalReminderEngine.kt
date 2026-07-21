package com.fittrack.app

import android.content.Context

/**
 * Generates contextual, in-app workout reminders from the planning data.
 * No push notifications or cloud services — these display as in-app banners.
 */
class LocalReminderEngine(private val context: Context) {

    private val db by lazy { DatabaseHelper(context) }
    private val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

    data class Reminder(
        val emoji: String,
        val title: String,
        val message: String,
        val priority: Int  // 1 = high, 2 = medium, 3 = low
    )

    fun getReminders(username: String): List<Reminder> {
        val reminders = mutableListOf<Reminder>()
        val today = sdf.format(java.util.Date())
        val todayWorkoutDay = db.getTodayScheduledWorkoutDay(username)
        val todayPlanned = db.getPlannedWorkoutForDate(username, today)
        val todaySessions = db.getSessionsOnDate(username, today)

        // ── TODAY'S WORKOUT ───────────────────────────────────────────────
        if (todayWorkoutDay != null && todaySessions.isEmpty()) {
            reminders.add(Reminder("💪", "Today is ${todayWorkoutDay.dayName}!", "You have ${todayWorkoutDay.muscleGroups} scheduled. Tap to start your workout.", 1))
        }

        // ── COMPLETED TODAY ───────────────────────────────────────────────
        if (todaySessions.isNotEmpty()) {
            val session = todaySessions.first()
            reminders.add(Reminder("✅", "Workout Complete!", "You finished today's ${session.workoutDayName}. Volume: ${String.format("%,.0f", session.totalVolume)} kg.", 2))
        }

        // ── MISSED YESTERDAY ──────────────────────────────────────────────
        val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = sdf.format(yesterday.time)
        val yesterdayPlanned = db.getPlannedWorkoutForDate(username, yesterdayStr)
        val yesterdaySessions = db.getSessionsOnDate(username, yesterdayStr)
        if (yesterdayPlanned != null && yesterdaySessions.isEmpty() && yesterdayPlanned.status == "planned") {
            reminders.add(Reminder("⚠️", "Missed Yesterday", "You missed your ${yesterdayPlanned.workoutDayName}. Reschedule it or mark as skipped.", 1))
        }

        // ── TOMORROW PREVIEW ──────────────────────────────────────────────
        val tomorrow = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 1) }
        val tomorrowCal = java.util.Calendar.getInstance().apply { time = tomorrow.time }
        val tomorrowDow = tomorrowCal.get(java.util.Calendar.DAY_OF_WEEK)
        val tomorrowWeekday = if (tomorrowDow == java.util.Calendar.SUNDAY) 7 else tomorrowDow - 1
        val activeSplit = db.getActiveSplit(username)
        if (activeSplit != null) {
            val schedule = db.getWeeklyScheduleForSplit(username, activeSplit.id)
            val tomorrowDay = schedule[tomorrowWeekday]
            if (tomorrowDay != null) {
                reminders.add(Reminder("📅", "Tomorrow: ${tomorrowDay.dayName}", "You have ${tomorrowDay.muscleGroups} scheduled tomorrow. Rest well!", 3))
            }
        }

        // ── STREAK WARNING ────────────────────────────────────────────────
        val streak = db.getWorkoutStreak(username)
        if (streak > 0 && todaySessions.isEmpty() && todayWorkoutDay != null) {
            reminders.add(Reminder("🔥", "Don't Break Your $streak-Day Streak!", "Complete today's workout to keep the streak alive.", 1))
        }

        // ── WEEKLY COMPLETION ─────────────────────────────────────────────
        val goals = db.getGoals(username)
        val weeklyCount = db.getWeeklyWorkoutCount(username)
        if (goals.weeklyWorkoutGoal > 0 && weeklyCount >= goals.weeklyWorkoutGoal) {
            reminders.add(Reminder("🎯", "Weekly Goal Reached!", "You've completed all ${goals.weeklyWorkoutGoal} workouts this week. Keep it up!", 2))
        }

        return reminders.sortedBy { it.priority }.take(3)
    }

    fun getTodaySummary(username: String): String {
        val today = sdf.format(java.util.Date())
        val todaySessions = db.getSessionsOnDate(username, today)
        if (todaySessions.isNotEmpty()) {
            return "✅ Workout complete"
        }
        val todayWorkoutDay = db.getTodayScheduledWorkoutDay(username)
        return if (todayWorkoutDay != null) {
            "Today: ${todayWorkoutDay.dayName}"
        } else {
            "Rest Day"
        }
    }
}
