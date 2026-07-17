package com.fittrack.app

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class ProgressActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvProgress: RecyclerView
    private lateinit var tvEmptyProgress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarProgress)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvProgress = findViewById(R.id.rvProgress)
        tvEmptyProgress = findViewById(R.id.tvEmptyProgress)

        rvProgress.layoutManager = LinearLayoutManager(this)

        loadProgress()
    }

    private fun loadProgress() {
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        val progressList = dbHelper.getAllExerciseProgress(username)

        if (progressList.isEmpty()) {
            rvProgress.visibility = View.GONE
            tvEmptyProgress.visibility = View.VISIBLE
        } else {
            val adapter = ExerciseProgressAdapter(progressList)
            rvProgress.adapter = adapter
            rvProgress.visibility = View.VISIBLE
            tvEmptyProgress.visibility = View.GONE
        }
    }
}
