package com.fittrack.app

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalWorkouts: TextView
    private lateinit var rvUsers: RecyclerView
    private lateinit var tvNoUsers: TextView
    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)

        toolbar = findViewById(R.id.toolbarAdmin)
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkoutsAdmin)
        rvUsers = findViewById(R.id.rvUsers)
        tvNoUsers = findViewById(R.id.tvNoUsers)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        loadAdminData()
    }

    override fun onResume() {
        super.onResume()
        loadAdminData()
    }

    private fun loadAdminData() {
        val allKeys = prefs.all.keys

        // Collect unique usernames that have a password entry
        val usernames = allKeys
            .filter { it.startsWith("user_") && it.endsWith("_password") }
            .map { it.removePrefix("user_").removeSuffix("_password") }
            .sorted()

        tvTotalUsers.text = usernames.size.toString()
        tvTotalWorkouts.text = dbHelper.getGlobalTotalWorkoutCount().toString()

        if (usernames.isEmpty()) {
            rvUsers.visibility = View.GONE
            tvNoUsers.visibility = View.VISIBLE
            return
        }

        rvUsers.visibility = View.VISIBLE
        tvNoUsers.visibility = View.GONE

        // Build user info list
        val userInfoList = usernames.map { username ->
            val role = prefs.getString("user_${username}_role", "user") ?: "user"
            val workoutCount = dbHelper.getTotalWorkoutCount(username)
            Triple(username, role, workoutCount)
        }

        val adapter = UserAdapter(userInfoList)
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter
    }
}
