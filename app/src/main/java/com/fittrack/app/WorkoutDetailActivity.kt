package com.fittrack.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File

class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var workoutId: Int = -1
    private var workout: Workout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_detail)

        dbHelper = DatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { 
            finish()
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_left)
        }

        workoutId = intent.getIntExtra("WORKOUT_ID", -1)
        if (workoutId == -1) {
            Toast.makeText(this, "Error loading workout details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadWorkoutData()
    }

    private fun loadWorkoutData() {
        workout = dbHelper.getWorkoutById(workoutId)
        if (workout == null) {
            finish()
            return
        }

        val w = workout!!

        findViewById<TextView>(R.id.tvDetailExerciseName).text = w.exerciseName
        findViewById<TextView>(R.id.tvDetailMuscleGroup).text = "  ${w.muscleGroup}  "
        findViewById<TextView>(R.id.tvDetailDate).text = w.date
        findViewById<TextView>(R.id.tvDetailWeight).text = "${w.weight} kg"
        findViewById<TextView>(R.id.tvDetailSetsReps).text = "${w.sets} × ${w.reps}"
        findViewById<TextView>(R.id.tvDetailDuration).text = "${w.duration} min"

        val tvNotes = findViewById<TextView>(R.id.tvDetailNotes)
        val tvNotesTitle = findViewById<TextView>(R.id.tvDetailNotesTitle)
        if (w.notes.isNotEmpty()) {
            tvNotesTitle.visibility = View.VISIBLE
            tvNotes.visibility = View.VISIBLE
            tvNotes.text = w.notes
        } else {
            tvNotesTitle.visibility = View.GONE
            tvNotes.visibility = View.GONE
        }

        val ivDetailImage = findViewById<ImageView>(R.id.ivDetailImage)
        if (w.imagePath.isNotEmpty()) {
            val file = File(w.imagePath)
            if (file.exists()) {
                ivDetailImage.setImageURI(Uri.fromFile(file))
            } else {
                ivDetailImage.setImageResource(R.drawable.ic_image)
            }
        } else {
            ivDetailImage.setImageResource(R.drawable.ic_image)
        }
    }

    private fun setupClickListeners() {
        findViewById<MaterialButton>(R.id.btnDetailEdit).setOnClickListener {
            val intent = Intent(this, AddWorkoutActivity::class.java).apply {
                putExtra("WORKOUT_ID", workoutId)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<MaterialButton>(R.id.btnDetailDelete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_workout_title))
                .setMessage(getString(R.string.delete_workout_message))
                .setPositiveButton(getString(R.string.delete_confirm)) { _, _ ->
                    dbHelper.deleteWorkout(workoutId)
                    Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }
}
