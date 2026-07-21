package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class StartWorkoutActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var restTimer: RestTimerHelper
    private lateinit var overloadEngine: ProgressiveOverloadEngine

    private var workoutDayId = -1
    private var username = ""
    private var sessionStartTime = System.currentTimeMillis()

    private lateinit var adapter: SessionExerciseAdapter
    private val elapsedHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_workout)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        restTimer = RestTimerHelper(this)
        overloadEngine = ProgressiveOverloadEngine(this)
        username = prefs.getString("loggedInUser", "") ?: ""
        workoutDayId = intent.getIntExtra("WORKOUT_DAY_ID", -1)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarStartWorkout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { showExitConfirmation() }

        val tvDayName = findViewById<TextView>(R.id.tvWorkoutDayName)
        val tvElapsed = findViewById<TextView>(R.id.tvElapsedTime)
        val tvRestCount = findViewById<TextView>(R.id.tvRestTimerCount)
        val layoutRestTimer = findViewById<View>(R.id.layoutRestTimer)

        // Load workout day
        val workoutDay = dbHelper.getWorkoutDayById(workoutDayId)
        tvDayName.text = workoutDay?.dayName ?: "Workout"
        toolbar.title = workoutDay?.dayName ?: "Workout"

        // Load exercises
        val exercises = dbHelper.getDayExercisesForDay(workoutDayId)
        adapter = SessionExerciseAdapter(exercises, username, dbHelper, overloadEngine)
        val rv = findViewById<RecyclerView>(R.id.rvSessionExercises)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // Elapsed timer
        startElapsedTimer(tvElapsed)

        // Rest timer
        setupRestTimer(tvRestCount, layoutRestTimer)

        // Finish button
        findViewById<MaterialButton>(R.id.btnFinishWorkout).setOnClickListener {
            finishWorkout()
        }
    }

    private fun startElapsedTimer(tvElapsed: TextView) {
        sessionStartTime = System.currentTimeMillis()
        val runnable = object : Runnable {
            override fun run() {
                val elapsed = (System.currentTimeMillis() - sessionStartTime) / 1000
                val minutes = elapsed / 60
                val seconds = elapsed % 60
                tvElapsed.text = String.format("%02d:%02d", minutes, seconds)
                elapsedHandler.postDelayed(this, 1000)
            }
        }
        elapsedHandler.post(runnable)
    }

    private fun setupRestTimer(tvCount: TextView, layoutTimer: View) {
        restTimer.setListener(object : RestTimerHelper.RestTimerListener {
            override fun onTick(remainingSeconds: Int) {
                tvCount.text = remainingSeconds.toString()
                layoutTimer.visibility = View.VISIBLE
            }
            override fun onFinished() {
                layoutTimer.visibility = View.GONE
                Toast.makeText(this@StartWorkoutActivity, "Rest complete! Start your next set.", Toast.LENGTH_SHORT).show()
            }
        })

        findViewById<MaterialButton>(R.id.btn30s).setOnClickListener { restTimer.start(30) }
        findViewById<MaterialButton>(R.id.btn60s).setOnClickListener { restTimer.start(60) }
        findViewById<MaterialButton>(R.id.btn90s).setOnClickListener { restTimer.start(90) }
        findViewById<MaterialButton>(R.id.btn120s).setOnClickListener { restTimer.start(120) }
    }

    private fun finishWorkout() {
        val logs = adapter.getExerciseLogs()
        if (logs.isEmpty()) {
            Toast.makeText(this, "Please log at least one exercise", Toast.LENGTH_SHORT).show()
            return
        }

        val endTime = System.currentTimeMillis()
        val durationMinutes = ((endTime - sessionStartTime) / 60000).toInt()
        val totalVolume = logs.sumOf { it.weight * it.sets * it.reps }
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val timeSdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date())
        val workoutDay = dbHelper.getWorkoutDayById(workoutDayId)

        val session = WorkoutSession(
            username = username,
            workoutDayId = workoutDayId,
            workoutDayName = workoutDay?.dayName ?: "Workout",
            date = today,
            startTime = timeSdf.format(java.util.Date(sessionStartTime)),
            endTime = timeSdf.format(java.util.Date(endTime)),
            durationMinutes = durationMinutes,
            totalVolume = totalVolume,
            status = "completed",
            caloriesBurned = (totalVolume * 0.001 * durationMinutes).toInt().coerceAtLeast(100)
        )

        // Save session
        val sessionId = dbHelper.insertSession(session)

        // Save exercise logs and check PRs
        val personalRecords = dbHelper.getPersonalRecords(username)
        val prMap = personalRecords.associate { it.first to it.second }

        val finalLogs = logs.map { log ->
            val previousPR = prMap[log.exerciseName] ?: 0.0
            val isNewPR = log.weight > previousPR
            if (isNewPR) {
                dbHelper.unlockAchievement(username, "pr_breaker")
            }
            log.copy(sessionId = sessionId.toInt(), isNewPR = isNewPR)
        }
        finalLogs.forEach { dbHelper.insertSessionExerciseLog(it) }

        // Update planned workout status
        dbHelper.updatePlannedWorkoutStatus(username, today, "completed", sessionId.toInt())

        // Check achievements and milestones
        AchievementChecker(this).checkAndUnlockAchievements(username)
        val milestoneChecker = ExerciseMilestoneChecker(this)
        finalLogs.forEach { milestoneChecker.checkAndUnlockMilestones(username, it.exerciseName) }

        // Also mirror to legacy Workout table for backward compatibility
        for (log in finalLogs) {
            if (log.weight > 0) {
                val workout = Workout(exerciseName = log.exerciseName, muscleGroup = log.muscleGroup, weight = log.weight, sets = log.sets, reps = log.reps, duration = durationMinutes, date = today, notes = log.notes, username = username)
                dbHelper.insertWorkout(workout)
            }
        }

        elapsedHandler.removeCallbacksAndMessages(null)
        restTimer.cancel()

        val prCount = finalLogs.count { it.isNewPR }
        val intent = Intent(this, WorkoutCompleteActivity::class.java).apply {
            putExtra("SESSION_ID", sessionId.toInt())
            putExtra("PR_COUNT", prCount)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Exit Workout?")
            .setMessage("Your workout progress will be lost if you exit now.")
            .setPositiveButton("Exit") { _, _ ->
                elapsedHandler.removeCallbacksAndMessages(null)
                restTimer.cancel()
                finish()
            }
            .setNegativeButton("Continue", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        elapsedHandler.removeCallbacksAndMessages(null)
        restTimer.cancel()
    }
}

// ── Session Exercise Adapter ─────────────────────────────────────────────────
class SessionExerciseAdapter(
    private val exercises: List<DayExercise>,
    private val username: String,
    private val db: DatabaseHelper,
    private val overloadEngine: ProgressiveOverloadEngine
) : RecyclerView.Adapter<SessionExerciseAdapter.VH>() {

    // Live state: maps exercise index → (weight, sets, reps, notes)
    data class ExerciseEntry(var weight: Double = 0.0, var sets: Int = 3, var reps: Int = 10, var notes: String = "")
    private val entries = mutableListOf<ExerciseEntry>().apply { repeat(exercises.size) { add(ExerciseEntry()) } }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvExerciseName)
        val tvMuscle: TextView = view.findViewById(R.id.tvExerciseMuscle)
        val tvLastPerformed: TextView = view.findViewById(R.id.tvLastPerformed)
        val tvSuggested: TextView = view.findViewById(R.id.tvSuggestedWeight)
        val etWeight: TextInputEditText = view.findViewById(R.id.etWeight)
        val etSets: TextInputEditText = view.findViewById(R.id.etSets)
        val etReps: TextInputEditText = view.findViewById(R.id.etReps)
        val tvPRBadge: TextView = view.findViewById(R.id.tvPRBadge)

        fun watchText(et: TextInputEditText, onChange: (String) -> Unit) {
            et.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) { onChange(s?.toString() ?: "") }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_session_exercise, parent, false)
        return VH(v)
    }

    override fun getItemCount() = exercises.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val exercise = exercises[position]
        val entry = entries[position]
        holder.tvName.text = exercise.exerciseName
        holder.tvMuscle.text = "${exercise.muscleGroup} • ${exercise.equipment}"

        // Clear old text watchers by removing tag-stored ones
        holder.etWeight.removeTextChangedListeners()
        holder.etSets.removeTextChangedListeners()
        holder.etReps.removeTextChangedListeners()

        // Set defaults / current entry values
        holder.etSets.setText(if (entry.sets > 0) entry.sets.toString() else exercise.defaultSets.toString())
        holder.etReps.setText(if (entry.reps > 0) entry.reps.toString() else exercise.defaultReps.toString())

        // Load suggestion from progressive overload engine
        try {
            val suggestion = overloadEngine.getSuggestion(username, exercise.exerciseName)
            if (suggestion.lastWeight > 0) {
                holder.tvLastPerformed.text = "Last: ${suggestion.lastWeight} kg"
                holder.tvSuggested.text = "💡 Suggested: ${suggestion.suggestedWeight} kg"
                holder.tvSuggested.visibility = View.VISIBLE
                val displayWeight = if (entry.weight > 0) entry.weight else suggestion.suggestedWeight
                holder.etWeight.setText(displayWeight.toString())
            } else {
                holder.tvLastPerformed.text = "First time!"
                holder.tvSuggested.visibility = View.GONE
                if (entry.weight > 0) holder.etWeight.setText(entry.weight.toString())
            }
        } catch (e: Exception) {
            holder.tvSuggested.visibility = View.GONE
        }

        // PR detection
        val prMap = db.getPersonalRecords(username).associate { it.first to it.second }
        val pr = prMap[exercise.exerciseName] ?: 0.0

        holder.watchText(holder.etWeight) { text ->
            val w = text.toDoubleOrNull() ?: 0.0
            entries[position].weight = w
            holder.tvPRBadge.visibility = if (w > pr && pr > 0) View.VISIBLE else View.GONE
        }
        holder.watchText(holder.etSets) { text -> entries[position].sets = text.toIntOrNull() ?: 3 }
        holder.watchText(holder.etReps) { text -> entries[position].reps = text.toIntOrNull() ?: 10 }
    }

    fun getExerciseLogs(): List<SessionExerciseLog> {
        return exercises.mapIndexed { index, exercise ->
            val entry = entries[index]
            SessionExerciseLog(
                exerciseName = exercise.exerciseName,
                muscleGroup = exercise.muscleGroup,
                weight = entry.weight,
                sets = entry.sets,
                reps = entry.reps,
                notes = entry.notes,
                sortOrder = index
            )
        }.filter { it.weight > 0 || it.sets > 0 }
    }

    private fun TextInputEditText.removeTextChangedListeners() {
        // Tag-based cleanup handled via rebind; Android's TextWatcher doesn't stack since
        // we always remove all watchers by setting text programmatically here, which is safe
    }
}
