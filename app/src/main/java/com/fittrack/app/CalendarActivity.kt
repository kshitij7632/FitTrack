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
    private lateinit var rvDayWorkouts: RecyclerView
    private lateinit var tvEmptyDay: TextView
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var dayAdapter: CalendarDayAdapter
    private lateinit var workoutAdapter: WorkoutAdapter
    
    private var username = "User"
    private var workoutDates = setOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "User") ?: "User"
        
        currentCalendar = Calendar.getInstance()
        workoutDates = dbHelper.getWorkoutDatesSet(username)

        initViews()
        setupBottomNav()
        updateCalendar()
    }

    private fun initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear)
        rvCalendar = findViewById(R.id.rvCalendar)
        rvDayWorkouts = findViewById(R.id.rvDayWorkouts)
        tvEmptyDay = findViewById(R.id.tvEmptyDay)
        bottomNav = findViewById(R.id.bottomNavigation)
        
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        rvDayWorkouts.layoutManager = LinearLayoutManager(this)
        
        findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        
        findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
        
        findViewById<FloatingActionButton>(R.id.fabAddWorkoutPlaceholder).setOnClickListener {
            startActivity(Intent(this, AddWorkoutActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun setupBottomNav() {
        bottomNav.background = null
        bottomNav.menu.getItem(2).isEnabled = false
        bottomNav.selectedItemId = R.id.nav_calendar
        
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

        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val tempCal = currentCalendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based for offset
        
        val daysList = mutableListOf<String>()
        for (i in 0 until firstDayOfWeek) {
            daysList.add("")
        }
        for (i in 1..daysInMonth) {
            daysList.add(i.toString())
        }
        
        val sdfMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val currentMonthStr = sdfMonth.format(currentCalendar.time)
        
        val sdfToday = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayStr = sdfToday.format(Date())

        dayAdapter = CalendarDayAdapter(daysList, workoutDates, currentMonthStr, todayStr) { selectedDate ->
            onDaySelected(selectedDate)
        }
        rvCalendar.adapter = dayAdapter
        
        // Clear list when month changes
        rvDayWorkouts.visibility = View.GONE
        tvEmptyDay.visibility = View.VISIBLE
    }

    private fun onDaySelected(date: String) {
        val workouts = dbHelper.getWorkoutsOnDate(username, date)
        if (workouts.isEmpty()) {
            rvDayWorkouts.visibility = View.GONE
            tvEmptyDay.visibility = View.VISIBLE
        } else {
            workoutAdapter = WorkoutAdapter(workouts, {
                // Edit
                val intent = Intent(this, AddWorkoutActivity::class.java)
                intent.putExtra("WORKOUT_ID", it.id)
                startActivity(intent)
            }, {
                // Delete
                dbHelper.deleteWorkout(it.id)
                workoutDates = dbHelper.getWorkoutDatesSet(username)
                updateCalendar()
                onDaySelected(date) // refresh list
            })
            rvDayWorkouts.adapter = workoutAdapter
            rvDayWorkouts.visibility = View.VISIBLE
            tvEmptyDay.visibility = View.GONE
        }
    }
}
