package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ExerciseLibraryActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private var username = ""
    
    private lateinit var rvLibrary: RecyclerView
    private lateinit var adapter: ExerciseLibraryAdapter
    private var allExercises = listOf<ExerciseInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_library)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarExerciseLibrary)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvLibrary = findViewById(R.id.rvExerciseLibrary)
        rvLibrary.layoutManager = LinearLayoutManager(this)

        setupChips()
        loadExercises()
    }

    private fun setupChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupMuscleFilter)
        chipGroup.check(R.id.chipAll)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = findViewById<Chip>(checkedIds[0])
            filterExercises(chip.text.toString())
        }
    }

    private fun loadExercises() {
        // Automatically seed exercises if none exist.
        if (!dbHelper.isExerciseLibrarySeeded()) {
            val libraryHelper = PredefinedSplitsLibrary(this)
            libraryHelper.seedExerciseLibraryIfEmpty()
        }
        
        allExercises = dbHelper.getExerciseLibrary(username)
        adapter = ExerciseLibraryAdapter(allExercises) { exercise ->
            val intent = Intent(this, ExercisePerformanceActivity::class.java)
            intent.putExtra("EXERCISE_NAME", exercise.name)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        rvLibrary.adapter = adapter
    }

    private fun filterExercises(muscleGroup: String) {
        val filtered = if (muscleGroup == "All") {
            allExercises
        } else {
            allExercises.filter { it.muscleGroup.contains(muscleGroup, ignoreCase = true) }
        }
        adapter.updateData(filtered)
    }
}

class ExerciseLibraryAdapter(
    private var exercises: List<ExerciseInfo>,
    private val onClick: (ExerciseInfo) -> Unit
) : RecyclerView.Adapter<ExerciseLibraryAdapter.VH>() {

    fun updateData(newExercises: List<ExerciseInfo>) {
        exercises = newExercises
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvLibExerciseName)
        val tvMuscle: TextView = view.findViewById(R.id.tvLibMuscle)
        val tvEquipment: TextView = view.findViewById(R.id.tvLibEquipment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_library, parent, false)
        return VH(view)
    }

    override fun getItemCount() = exercises.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ex = exercises[position]
        holder.tvName.text = ex.name
        holder.tvMuscle.text = ex.muscleGroup
        holder.tvEquipment.text = ex.equipment
        holder.itemView.setOnClickListener { onClick(ex) }
    }
}
