package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class WorkoutCompleteActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper

    private var sessionId = -1
    private var prCount = 0
    private var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_complete)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "") ?: ""
        sessionId = intent.getIntExtra("SESSION_ID", -1)
        prCount = intent.getIntExtra("PR_COUNT", 0)

        loadSessionSummary()

        findViewById<MaterialButton>(R.id.btnSaveSession).setOnClickListener { saveAndFinish() }
        findViewById<MaterialButton>(R.id.btnAddMeasurementAfter).setOnClickListener {
            startActivity(Intent(this, AddMeasurementActivity::class.java))
        }
    }

    private fun loadSessionSummary() {
        val session = if (sessionId > 0) dbHelper.getSessionById(sessionId) else null
        val logs = if (sessionId > 0) dbHelper.getLogsForSession(sessionId) else emptyList()

        // Stats
        val tvVolume = findViewById<TextView>(R.id.tvCompleteVolume)
        val tvDuration = findViewById<TextView>(R.id.tvCompleteDuration)
        val tvCalories = findViewById<TextView>(R.id.tvCompleteCalories)
        val tvPRs = findViewById<TextView>(R.id.tvCompletePRs)

        session?.let { s ->
            tvVolume.text = "${String.format("%,.0f", s.totalVolume)} kg"
            tvDuration.text = "${s.durationMinutes} min"
            tvCalories.text = "${s.caloriesBurned}"
        }

        if (prCount > 0) {
            tvPRs.text = "⭐ $prCount Personal Record${if (prCount > 1) "s" else ""} Set!"
            tvPRs.visibility = View.VISIBLE
        }

        // Exercise list
        val rvExercises = findViewById<RecyclerView>(R.id.rvCompleteExercises)
        rvExercises.layoutManager = LinearLayoutManager(this)
        rvExercises.adapter = CompletedExerciseAdapter(logs)
    }

    private fun saveAndFinish() {
        val session = dbHelper.getSessionById(sessionId) ?: return
        val mood = findViewById<RatingBar>(R.id.ratingMood).rating.toInt()
        val energy = findViewById<RatingBar>(R.id.ratingEnergy).rating.toInt()
        val sleep = findViewById<RatingBar>(R.id.ratingSleep).rating.toInt()
        val notes = findViewById<TextInputEditText>(R.id.etCompleteNotes).text.toString()

        val updatedSession = session.copy(mood = mood, energyLevel = energy, sleepQuality = sleep, notes = notes)
        dbHelper.updateSession(updatedSession)

        navigateToDashboard()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onBackPressed() { navigateToDashboard() }
}

class CompletedExerciseAdapter(private val logs: List<SessionExerciseLog>) : RecyclerView.Adapter<CompletedExerciseAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvExName)
        val tvStats: TextView = view.findViewById(R.id.tvExStats)
        val tvPR: TextView = view.findViewById(R.id.tvExPR)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_completed_exercise, parent, false)
        return VH(v)
    }
    override fun getItemCount() = logs.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val log = logs[position]
        holder.tvName.text = log.exerciseName
        holder.tvStats.text = "${log.weight} kg × ${log.sets} × ${log.reps}"
        holder.tvPR.visibility = if (log.isNewPR) View.VISIBLE else View.GONE
    }
}
