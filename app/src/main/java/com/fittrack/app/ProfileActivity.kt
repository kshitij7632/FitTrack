package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var scoreCalculator: FitnessScoreCalculator
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        scoreCalculator = FitnessScoreCalculator(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarProfile)
        setSupportActionBar(toolbar)

        bottomNav = findViewById(R.id.bottomNavigation)
        setupBottomNav()
        loadProfile()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_profile
    }

    private fun setupBottomNav() {
        bottomNav.background = null
        bottomNav.menu.getItem(2).isEnabled = false
        bottomNav.selectedItemId = R.id.nav_profile

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
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
                    finish()
                    false
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun loadProfile() {
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        val role = prefs.getString("loggedInRole", "user") ?: "user"
        val displayName = username.replaceFirstChar { it.uppercase() }

        val tvUsername = findViewById<TextView>(R.id.tvProfileUsername)
        val tvRoleBadge = findViewById<TextView>(R.id.tvRoleBadge)
        val tvTotalWorkouts = findViewById<TextView>(R.id.tvTotalWorkoutsCount)
        val tvProfileStreak = findViewById<TextView>(R.id.tvProfileStreak)
        val tvUniqueExercises = findViewById<TextView>(R.id.tvProfileUniqueExercises)
        val tvVolume = findViewById<TextView>(R.id.tvProfileVolume)
        val tvFitnessScore = findViewById<TextView>(R.id.tvFitnessScore)

        tvUsername.text = displayName

        if (role == "admin") {
            tvRoleBadge.text = "ADMINISTRATOR"
            tvRoleBadge.setTextColor(getColor(R.color.accent_gold))
        } else {
            tvRoleBadge.text = "ATHLETE"
            tvRoleBadge.setTextColor(getColor(R.color.text_secondary))
        }

        // Stats
        tvTotalWorkouts.text = dbHelper.getTotalWorkoutCount(username).toString()
        tvProfileStreak.text = dbHelper.getWorkoutStreak(username).toString()
        tvUniqueExercises.text = dbHelper.getUniqueExerciseCount(username).toString()

        val volume = dbHelper.getTotalWeightLifted(username)
        tvVolume.text = String.format("%,.0f", volume)

        val score = scoreCalculator.calculateScore(username)
        tvFitnessScore.text = "Fitness Score: $score / 100"
    }

    private fun setupClickListeners() {
        findViewById<FloatingActionButton>(R.id.fabAddWorkout).setOnClickListener {
            startActivity(Intent(this, AddWorkoutActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<MaterialButton>(R.id.btnGoals).setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<MaterialButton>(R.id.btnAchievements).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<MaterialButton>(R.id.btnPhotos).setOnClickListener {
            startActivity(Intent(this, PhotoGalleryActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_confirm_title))
            .setMessage(getString(R.string.logout_confirm_message))
            .setPositiveButton(getString(R.string.btn_logout)) { _, _ ->
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
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
