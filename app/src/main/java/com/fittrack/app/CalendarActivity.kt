package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var currentCalendar: Calendar
    
    private lateinit var tvMonthYear: TextView
    private lateinit var rvCalendar: RecyclerView
    private lateinit var bottomNav: BottomNavigationView

    // Context views
    private lateinit var tvSelectedDate: TextView
    private lateinit var rvCompletedSessions: RecyclerView
    private lateinit var cardPlannedWorkout: MaterialCardView
    private lateinit var layoutEmptyDay: View
    private lateinit var tvEmptyStateText: TextView

    private lateinit var dayAdapter: CalendarDayAdapter
    private var username = "User"
    
    // Status maps for the month
    private var monthStatuses = mutableMapOf<String, DayStatus>()

    enum class DayType { COMPLETED, PLANNED, MISSED, REST }
    data class DayStatus(val type: DayType, val hasPR: Boolean = false, val hasStreak: Boolean = false)

    private val sdfKey = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "User") ?: "User"
        
        currentCalendar = Calendar.getInstance()

        initViews()
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_calendar
        updateCalendar()
    }

    private fun initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear)
        rvCalendar = findViewById(R.id.rvCalendar)
        bottomNav = findViewById(R.id.bottomNavigation)
        
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        rvCompletedSessions = findViewById(R.id.rvCompletedSessions)
        cardPlannedWorkout = findViewById(R.id.cardPlannedWorkout)
        layoutEmptyDay = findViewById(R.id.layoutEmptyDay)
        tvEmptyStateText = findViewById(R.id.tvEmptyStateText)

        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvCompletedSessions.layoutManager = LinearLayoutManager(this)
        
        findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        
        findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
        
        findViewById<FloatingActionButton>(R.id.fabPlannerAction).setOnClickListener {
            startActivity(Intent(this, MySplitsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun setupBottomNav() {
        bottomNav.background = null
        bottomNav.menu.getItem(2).isEnabled = false
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TOP })
                    finish()
                    false
                }
                R.id.nav_analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
                    finish()
                    false
                }
                R.id.nav_calendar -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
                    finish()
                    false
                }
                else -> false
            }
        }
    }

    private fun updateCalendar() {
        val sdfMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = sdfMonthYear.format(currentCalendar.time)

        // Build list of days
        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val tempCal = currentCalendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        
        // Android Calendar.DAY_OF_WEEK: Sunday=1, Monday=2. We want Monday=0, Sunday=6
        var firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 2
        if (firstDayOfWeek < 0) firstDayOfWeek = 6
        
        val daysList = mutableListOf<String>()
        for (i in 0 until firstDayOfWeek) daysList.add("")
        for (i in 1..daysInMonth) daysList.add(i.toString())
        
        // Calculate statuses for the month
        calculateMonthStatuses()

        val todayStr = sdfKey.format(Date())
        dayAdapter = CalendarDayAdapter(daysList, monthStatuses, currentCalendar.time, todayStr) { selectedDate ->
            onDaySelected(selectedDate)
        }
        rvCalendar.adapter = dayAdapter
        
        // Select today if in current month, else 1st
        val todayCal = Calendar.getInstance()
        if (todayCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) && todayCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
            onDaySelected(todayStr)
        } else {
            val firstDateCal = currentCalendar.clone() as Calendar
            firstDateCal.set(Calendar.DAY_OF_MONTH, 1)
            onDaySelected(sdfKey.format(firstDateCal.time))
        }
    }

    private fun calculateMonthStatuses() {
        monthStatuses.clear()
        
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0)
        
        val cal = currentCalendar.clone() as Calendar
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val activeSplit = dbHelper.getActiveSplit(username)
        val weeklySchedule = activeSplit?.let { dbHelper.getWeeklyScheduleForSplit(username, it.id) }
        
        for (i in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            val dateStr = sdfKey.format(cal.time)
            
            val sessions = dbHelper.getSessionsOnDate(username, dateStr)
            var hasPR = false
            for (s in sessions) {
                val logs = dbHelper.getLogsForSession(s.id)
                if (logs.any { it.isNewPR }) hasPR = true
            }
            // For streak, simple approximation based on streak > 0 but we'd need history. Just mock false for now.
            
            if (sessions.isNotEmpty()) {
                monthStatuses[dateStr] = DayStatus(DayType.COMPLETED, hasPR, false)
                continue
            }
            
            // Check if planned
            var isPlanned = false
            if (weeklySchedule != null) {
                var javaDow = cal.get(Calendar.DAY_OF_WEEK)
                var weekday = if (javaDow == Calendar.SUNDAY) 7 else javaDow - 1
                val plannedDay = weeklySchedule[weekday]
                if (plannedDay != null) isPlanned = true
            }
            
            if (isPlanned) {
                if (cal.before(today)) {
                    monthStatuses[dateStr] = DayStatus(DayType.MISSED)
                } else {
                    monthStatuses[dateStr] = DayStatus(DayType.PLANNED)
                }
            } else {
                monthStatuses[dateStr] = DayStatus(DayType.REST)
            }
        }
    }

    private fun onDaySelected(dateStr: String) {
        val displaySdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        try {
            val date = sdfKey.parse(dateStr)
            tvSelectedDate.text = displaySdf.format(date).uppercase()
        } catch (e: Exception) { tvSelectedDate.text = dateStr }

        val status = monthStatuses[dateStr] ?: DayStatus(DayType.REST)
        val sessions = dbHelper.getSessionsOnDate(username, dateStr)

        rvCompletedSessions.visibility = View.GONE
        cardPlannedWorkout.visibility = View.GONE
        layoutEmptyDay.visibility = View.GONE

        if (sessions.isNotEmpty()) {
            rvCompletedSessions.visibility = View.VISIBLE
            rvCompletedSessions.adapter = CalendarSessionAdapter(sessions, dbHelper) { session ->
                val intent = Intent(this, WorkoutDetailActivity::class.java)
                intent.putExtra("SESSION_ID", session.id)
                startActivity(intent)
            }
            return
        }

        // Try to get planned
        try {
            val dateObj = sdfKey.parse(dateStr) ?: Date()
            val cal = Calendar.getInstance().apply { time = dateObj }
            var javaDow = cal.get(Calendar.DAY_OF_WEEK)
            var weekday = if (javaDow == Calendar.SUNDAY) 7 else javaDow - 1
            
            val activeSplit = dbHelper.getActiveSplit(username)
            val plannedDay = activeSplit?.let { dbHelper.getWeeklyScheduleForSplit(username, it.id)[weekday] }

            if (plannedDay != null) {
                cardPlannedWorkout.visibility = View.VISIBLE
                findViewById<TextView>(R.id.tvPlannedName).text = plannedDay.dayName
                findViewById<TextView>(R.id.tvPlannedMuscles).text = plannedDay.muscleGroups
                
                val btnStart = findViewById<MaterialButton>(R.id.btnStartPlanned)
                val todayCal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
                if (cal.before(todayCal)) {
                    btnStart.text = "Log Missed Workout"
                } else if (cal == todayCal) {
                    btnStart.text = "Start Workout"
                } else {
                    btnStart.text = "Preview Workout"
                }
                
                btnStart.setOnClickListener {
                    val intent = Intent(this, StartWorkoutActivity::class.java)
                    intent.putExtra("WORKOUT_DAY_ID", plannedDay.id)
                    startActivity(intent)
                }
                return
            }
        } catch (e: Exception) { e.printStackTrace() }

        // Rest Day
        layoutEmptyDay.visibility = View.VISIBLE
        tvEmptyStateText.text = if (status.type == DayType.MISSED) "Missed Workout" else "Rest Day"
        findViewById<MaterialButton>(R.id.btnLogAnyway).setOnClickListener {
            startActivity(Intent(this, MySplitsActivity::class.java))
        }
    }
}

class CalendarSessionAdapter(
    private val sessions: List<WorkoutSession>,
    private val dbHelper: DatabaseHelper,
    private val onClick: (WorkoutSession) -> Unit
) : RecyclerView.Adapter<CalendarSessionAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tvHistWorkoutName)
        val tvStats = view.findViewById<TextView>(R.id.tvHistStats)
    }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, vt: Int): VH {
        val v = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_workout_history, parent, false)
        return VH(v)
    }
    override fun getItemCount() = sessions.size
    override fun onBindViewHolder(holder: VH, pos: Int) {
        val session = sessions[pos]
        holder.tvName.text = "✅ ${session.workoutDayName}"
        val exCount = dbHelper.getLogsForSession(session.id).map { it.exerciseName }.distinct().size
        holder.tvStats.text = "${session.durationMinutes} min • $exCount exercises • ${String.format("%,.0f", session.totalVolume)} kg"
        holder.itemView.setOnClickListener { onClick(session) }
    }
}
