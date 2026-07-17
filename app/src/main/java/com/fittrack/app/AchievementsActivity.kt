package com.fittrack.app

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class AchievementsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var achievementChecker: AchievementChecker
    private lateinit var rvAchievements: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        achievementChecker = AchievementChecker(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAchievements)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvAchievements = findViewById(R.id.rvAchievements)
        rvAchievements.layoutManager = LinearLayoutManager(this)

        loadAchievements()
    }

    private fun loadAchievements() {
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        
        // Ensure achievements are checked/generated first
        achievementChecker.checkAndUnlockAchievements(username)
        
        val achievements = achievementChecker.getAllAchievements(username)
        val adapter = AchievementAdapter(achievements)
        rvAchievements.adapter = adapter
    }
}
