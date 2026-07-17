package com.fittrack.app

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class GoalsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var etWeeklyGoal: TextInputEditText
    private lateinit var etMonthlyGoal: TextInputEditText
    private lateinit var etWeightGoal: TextInputEditText
    private lateinit var pbWeekly: ProgressBar
    private lateinit var pbMonthly: ProgressBar
    private lateinit var tvWeeklyStatus: TextView
    private lateinit var tvMonthlyStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarGoals)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        initViews()
        loadGoals()
    }

    private fun initViews() {
        etWeeklyGoal = findViewById(R.id.etWeeklyGoal)
        etMonthlyGoal = findViewById(R.id.etMonthlyGoal)
        etWeightGoal = findViewById(R.id.etWeightGoal)
        pbWeekly = findViewById(R.id.pbWeekly)
        pbMonthly = findViewById(R.id.pbMonthly)
        tvWeeklyStatus = findViewById(R.id.tvWeeklyStatus)
        tvMonthlyStatus = findViewById(R.id.tvMonthlyStatus)

        findViewById<MaterialButton>(R.id.btnSaveGoals).setOnClickListener {
            saveGoals()
        }
    }

    private fun loadGoals() {
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        val goals = dbHelper.getGoals(username)

        if (goals.weeklyWorkoutGoal > 0) etWeeklyGoal.setText(goals.weeklyWorkoutGoal.toString())
        if (goals.monthlyWorkoutGoal > 0) etMonthlyGoal.setText(goals.monthlyWorkoutGoal.toString())
        if (goals.targetWeight > 0) etWeightGoal.setText(goals.targetWeight.toString())

        // Weekly progress
        val weeklyCount = dbHelper.getWeeklyWorkoutCount(username)
        val weeklyGoal = goals.weeklyWorkoutGoal
        if (weeklyGoal > 0) {
            val pct = ((weeklyCount.toFloat() / weeklyGoal) * 100).toInt().coerceAtMost(100)
            pbWeekly.progress = pct
            tvWeeklyStatus.text = "$weeklyCount / $weeklyGoal workouts this week"
        } else {
            pbWeekly.progress = 0
            tvWeeklyStatus.text = "Set a weekly goal to track progress"
        }

        // Monthly progress
        val monthlyCount = dbHelper.getMonthlyWorkoutCount(username)
        val monthlyGoal = goals.monthlyWorkoutGoal
        if (monthlyGoal > 0) {
            val pct = ((monthlyCount.toFloat() / monthlyGoal) * 100).toInt().coerceAtMost(100)
            pbMonthly.progress = pct
            tvMonthlyStatus.text = "$monthlyCount / $monthlyGoal workouts this month"
        } else {
            pbMonthly.progress = 0
            tvMonthlyStatus.text = "Set a monthly goal to track progress"
        }
    }

    private fun saveGoals() {
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        val weekly = etWeeklyGoal.text.toString().toIntOrNull() ?: 0
        val monthly = etMonthlyGoal.text.toString().toIntOrNull() ?: 0
        val weight = etWeightGoal.text.toString().toDoubleOrNull() ?: 0.0

        val goal = Goal(
            weeklyWorkoutGoal = weekly,
            monthlyWorkoutGoal = monthly,
            targetWeight = weight
        )
        dbHelper.saveGoals(username, goal)

        Toast.makeText(this, "Goals saved!", Toast.LENGTH_SHORT).show()
        loadGoals() // Refresh progress bars
    }
}
