package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Color
import android.view.ViewGroup
import android.view.animation.AnimationUtils

class DashboardActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var insightsEngine: InsightsEngine
    private lateinit var achievementChecker: AchievementChecker

    private lateinit var tvGreeting: TextView
    private lateinit var tvQuote: TextView
    private lateinit var tvDate: TextView
    private lateinit var ivWorkoutStatus: ImageView
    
    private lateinit var tvStreak: TextView
    private lateinit var tvTotalWorkouts: TextView
    private lateinit var tvVolume: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvGoalProgress: TextView
    private lateinit var tvPhotos: TextView
    private lateinit var tvAchievements: TextView
    private lateinit var tvProgress: TextView
    private lateinit var ivStreakFire: ImageView
    
    private lateinit var insightsContainer: LinearLayout
    private lateinit var bottomNav: BottomNavigationView

    private val quotes = listOf(
        "\"The only bad workout is the one that didn't happen.\"",
        "\"Wake up with determination. Go to bed with satisfaction.\"",
        "\"It never gets easier, you just get stronger.\"",
        "\"Push yourself, because no one else is going to do it for you.\"",
        "\"Success starts with self-discipline.\""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        insightsEngine = InsightsEngine(this)
        achievementChecker = AchievementChecker(this)

        toolbar = findViewById(R.id.toolbarDashboard)
        setSupportActionBar(toolbar)

        initViews()
        setupBottomNav()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_home
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        achievementChecker.checkAndUnlockAchievements(username)
        loadDashboardData()
    }

    private fun initViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        tvQuote = findViewById(R.id.tvQuote)
        tvDate = findViewById(R.id.tvDate)
        ivWorkoutStatus = findViewById(R.id.ivWorkoutStatus)
        
        tvStreak = findViewById(R.id.tvStreak)
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts)
        tvVolume = findViewById(R.id.tvVolume)
        tvTime = findViewById(R.id.tvTime)
        tvGoalProgress = findViewById(R.id.tvGoalProgress)
        tvPhotos = findViewById(R.id.tvPhotos)
        tvAchievements = findViewById(R.id.tvAchievements)
        tvProgress = findViewById(R.id.tvProgress)
        ivStreakFire = findViewById(R.id.ivStreakFire)
        
        insightsContainer = findViewById(R.id.insightsContainer)
        bottomNav = findViewById(R.id.bottomNavigation)
        
        val role = prefs.getString("loggedInRole", "user") ?: "user"
        val btnAdminPanel = findViewById<MaterialButton>(R.id.btnAdminPanel)
        
        if (role == "admin") {
            btnAdminPanel.visibility = View.VISIBLE
            btnAdminPanel.setOnClickListener {
                startActivity(Intent(this, AdminPanelActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }
    
    private fun setupBottomNav() {
        bottomNav.background = null // clear for custom background if needed
        bottomNav.menu.getItem(2).isEnabled = false // disable middle placeholder
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
                    false
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
                    false
                }
                else -> false
            }
        }
    }

    private fun loadDashboardData() {
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        val displayName = username.replaceFirstChar { it.uppercase() }
        
        tvGreeting.text = "Hi, $displayName"
        
        val sdfDate = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        tvDate.text = sdfDate.format(Date())
        
        // Random quote based on day
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        tvQuote.text = quotes[dayOfYear % quotes.size]
        
        // Workout status today
        val sdfSql = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todaySql = sdfSql.format(Date())
        val todayWorkouts = dbHelper.getWorkoutsOnDate(username, todaySql)
        if (todayWorkouts.isNotEmpty()) {
            ivWorkoutStatus.setImageResource(R.drawable.ic_check)
            ivWorkoutStatus.setColorFilter(Color.parseColor("#4CAF50"))
        } else {
            ivWorkoutStatus.setImageResource(R.drawable.ic_fitness)
            ivWorkoutStatus.setColorFilter(Color.parseColor("#757575"))
        }

        // Stats
        val streak = dbHelper.getWorkoutStreak(username)
        tvStreak.text = "$streak"
        if (streak > 0) {
            ivStreakFire.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse))
        }
        
        val totalWorkouts = dbHelper.getTotalWorkoutCount(username)
        tvTotalWorkouts.text = totalWorkouts.toString()
        
        val volume = dbHelper.getTotalWeightLifted(username)
        tvVolume.text = String.format("%,.0f", volume)
        
        val durationMins = dbHelper.getTotalWorkoutDuration(username)
        val hours = durationMins / 60
        tvTime.text = "${hours}h"
        
        val goals = dbHelper.getGoals(username)
        val progress = dbHelper.getWeeklyCompletionPercentage(username, goals.weeklyWorkoutGoal)
        tvGoalProgress.text = "$progress%"
        
        val photosCount = dbHelper.getProgressPhotoCount(username)
        tvPhotos.text = photosCount.toString()
        
        val achievementsCount = dbHelper.getUnlockedAchievements(username).size
        tvAchievements.text = achievementsCount.toString()
        
        val prsCount = dbHelper.getPersonalRecordsCount(username)
        tvProgress.text = prsCount.toString()
        
        // Load insights
        insightsContainer.removeAllViews()
        val insights = insightsEngine.getInsights(username)
        for (insight in insights) {
            val view = layoutInflater.inflate(R.layout.item_insight, insightsContainer, false)
            view.findViewById<TextView>(R.id.tvInsightText).text = insight
            insightsContainer.addView(view)
        }
    }

    private fun setupClickListeners() {
        findViewById<FloatingActionButton>(R.id.fabAddWorkout).setOnClickListener {
            startActivity(Intent(this, AddWorkoutActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        findViewById<MaterialButton>(R.id.btnWorkoutHistory).setOnClickListener {
            startActivity(Intent(this, WorkoutHistoryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        findViewById<MaterialButton>(R.id.btnRecords).setOnClickListener {
            startActivity(Intent(this, PersonalRecordsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        findViewById<MaterialCardView>(R.id.cardGoals).setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        findViewById<MaterialCardView>(R.id.cardAchievements).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        findViewById<MaterialCardView>(R.id.cardPhotos).setOnClickListener {
            startActivity(Intent(this, PhotoGalleryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        
        findViewById<MaterialCardView>(R.id.cardProgress).setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_confirm_title))
            .setMessage(getString(R.string.logout_confirm_message))
            .setPositiveButton(getString(R.string.btn_logout)) { _, _ -> logout() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun logout() {
        prefs.edit()
            .putBoolean("isLoggedIn", false)
            .remove("loggedInUser")
            .remove("loggedInRole")
            .apply()
        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
