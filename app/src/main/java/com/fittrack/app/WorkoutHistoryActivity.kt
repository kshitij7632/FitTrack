package com.fittrack.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WorkoutHistoryActivity : AppCompatActivity() {

    private lateinit var rvWorkouts: RecyclerView
    private lateinit var layoutEmptyHistory: LinearLayout
    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var workoutList: MutableList<Workout> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_history)

        dbHelper = DatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHistory)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { 
            finish()
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_left)
        }

        rvWorkouts = findViewById(R.id.rvWorkouts)
        layoutEmptyHistory = findViewById(R.id.layoutEmptyHistory)
        
        rvWorkouts.layoutManager = LinearLayoutManager(this)

        workoutAdapter = WorkoutAdapter(
            workoutList,
            onEditClick = { workout ->
                val intent = Intent(this, AddWorkoutActivity::class.java).apply {
                    putExtra("WORKOUT_ID", workout.id)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            },
            onDeleteClick = { workout ->
                showDeleteConfirmation(workout.id)
            },
            onItemClick = { workout ->
                val intent = Intent(this, WorkoutDetailActivity::class.java).apply {
                    putExtra("WORKOUT_ID", workout.id)
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        )
        rvWorkouts.adapter = workoutAdapter

        findViewById<FloatingActionButton>(R.id.fabAddWorkout).setOnClickListener {
            startActivity(Intent(this, AddWorkoutActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onResume() {
        super.onResume()
        loadWorkouts()
    }

    private fun loadWorkouts() {
        workoutList.clear()
        val prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        workoutList.addAll(dbHelper.getAllWorkouts(username))
        workoutAdapter.notifyDataSetChanged()
        
        if (workoutList.isEmpty()) {
            layoutEmptyHistory.visibility = View.VISIBLE
            rvWorkouts.visibility = View.GONE
        } else {
            layoutEmptyHistory.visibility = View.GONE
            rvWorkouts.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmation(workoutId: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_workout_title))
            .setMessage(getString(R.string.delete_workout_message))
            .setPositiveButton(getString(R.string.delete_confirm)) { _, _ ->
                dbHelper.deleteWorkout(workoutId)
                Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
                loadWorkouts()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
