package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class WeeklyScheduleActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var rvSchedule: RecyclerView
    private lateinit var btnSave: MaterialButton

    private var splitId = -1
    private var isSetup = false
    private var username = ""
    private val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_schedule)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "") ?: ""
        splitId = intent.getIntExtra("SPLIT_ID", -1)
        isSetup = intent.getBooleanExtra("IS_SETUP", false)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarSchedule)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isSetup)
        toolbar.setNavigationOnClickListener { finish() }

        rvSchedule = findViewById(R.id.rvWeeklySchedule)
        btnSave = findViewById(R.id.btnSaveSchedule)

        rvSchedule.layoutManager = LinearLayoutManager(this)
        loadSchedule()

        btnSave.setOnClickListener { saveAndContinue() }
    }

    private fun loadSchedule() {
        if (splitId == -1) return
        val workoutDays = dbHelper.getWorkoutDaysForSplit(splitId)
        val existingSchedule = dbHelper.getWeeklyScheduleForSplit(username, splitId)
        val adapter = WeeklyScheduleAdapter(weekdays, workoutDays, existingSchedule)
        rvSchedule.adapter = adapter
        btnSave.setOnClickListener { saveAndContinue(adapter.getSelections()) }
    }

    private fun saveAndContinue(selections: Map<Int, Int?> = emptyMap()) {
        if (splitId == -1) return
        val workoutDays = dbHelper.getWorkoutDaysForSplit(splitId)

        for ((weekdayIndex, workoutDayId) in selections) {
            val weekday = weekdayIndex + 1 // 1=Mon
            val isRest = workoutDayId == null
            dbHelper.saveWeeklyScheduleEntry(username, splitId, weekday, workoutDayId ?: 0, isRest)
        }

        // Auto-populate planned workouts for the next 4 weeks
        autoScheduleNextWeeks()

        Toast.makeText(this, "Schedule saved!", Toast.LENGTH_SHORT).show()
        prefs.edit().putBoolean("setup_completed", true).apply()

        if (isSetup) {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }

    private fun autoScheduleNextWeeks() {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val schedule = dbHelper.getWeeklyScheduleForSplit(username, splitId)
        val cal = java.util.Calendar.getInstance()

        // Plan for next 28 days
        for (dayOffset in 0 until 28) {
            val targetCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, dayOffset) }
            val javaDow = targetCal.get(java.util.Calendar.DAY_OF_WEEK)
            val weekday = if (javaDow == java.util.Calendar.SUNDAY) 7 else javaDow - 1
            val dateStr = sdf.format(targetCal.time)

            // Skip if already planned
            val existing = dbHelper.getPlannedWorkoutForDate(username, dateStr)
            if (existing != null) continue

            val workoutDay = schedule[weekday]
            if (workoutDay != null) {
                dbHelper.insertPlannedWorkout(PlannedWorkout(username = username, date = dateStr, workoutDayId = workoutDay.id, workoutDayName = workoutDay.dayName, status = "planned"))
            } else {
                dbHelper.insertPlannedWorkout(PlannedWorkout(username = username, date = dateStr, workoutDayId = 0, workoutDayName = "Rest Day", status = "rest"))
            }
        }
    }
}

class WeeklyScheduleAdapter(
    private val weekdays: List<String>,
    private val workoutDays: List<WorkoutDay>,
    private val existingSchedule: Map<Int, WorkoutDay?>
) : RecyclerView.Adapter<WeeklyScheduleAdapter.VH>() {

    private val selections = mutableMapOf<Int, Int?>()

    init {
        for (i in weekdays.indices) {
            val existingDay = existingSchedule[i + 1]
            selections[i] = existingDay?.id
        }
    }

    fun getSelections() = selections.toMap()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvWeekday)
        val spinner: Spinner = view.findViewById(R.id.spinnerWorkoutDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_weekly_schedule_row, parent, false)
        return VH(v)
    }

    override fun getItemCount() = weekdays.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tvDay.text = weekdays[position]

        val options = mutableListOf("Rest Day") + workoutDays.map { it.dayName }
        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinner.adapter = adapter

        // Restore selection
        val currentDayId = selections[position]
        val spinnerIndex = if (currentDayId == null) 0 else workoutDays.indexOfFirst { it.id == currentDayId } + 1
        holder.spinner.setSelection(spinnerIndex.coerceAtLeast(0))

        holder.spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selections[position] = if (pos == 0) null else workoutDays[pos - 1].id
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
}
