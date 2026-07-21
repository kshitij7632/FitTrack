package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class WorkoutPlannerSetupActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var rvSplitTemplates: RecyclerView
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: MaterialButton

    private var selectedTemplateIndex = -1
    private val templates = PredefinedSplitsLibrary.getAllSplits()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_planner_setup)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)

        rvSplitTemplates = findViewById(R.id.rvSplitTemplates)
        btnNext = findViewById(R.id.btnSetupNext)
        btnSkip = findViewById(R.id.btnSetupSkip)

        setupRecyclerView()

        btnNext.setOnClickListener { onNextClicked() }
        btnSkip.setOnClickListener { onSkipClicked() }
    }

    private fun setupRecyclerView() {
        rvSplitTemplates.layoutManager = LinearLayoutManager(this)
        rvSplitTemplates.adapter = SplitTemplateAdapter(templates) { index ->
            selectedTemplateIndex = index
            (rvSplitTemplates.adapter as SplitTemplateAdapter).setSelected(index)
        }
    }

    private fun onNextClicked() {
        val username = prefs.getString("loggedInUser", "") ?: ""
        if (username.isEmpty()) { finish(); return }

        if (selectedTemplateIndex == -1) {
            Toast.makeText(this, "Please select a workout split or tap Skip", Toast.LENGTH_SHORT).show()
            return
        }

        val template = templates[selectedTemplateIndex]

        // Save selected split to DB
        val splitId = dbHelper.insertSplit(username, template.name, template.description, template.goal)
        dbHelper.setActiveSplit(username, splitId.toInt())

        // Save workout days and exercises
        template.days.forEachIndexed { dayIndex, dayTemplate ->
            val dayId = dbHelper.insertWorkoutDay(splitId.toInt(), dayTemplate.dayName, dayTemplate.muscleGroups, dayTemplate.estimatedDuration, "", dayIndex)
            dayTemplate.exercises.forEachIndexed { exIndex, exTemplate ->
                dbHelper.insertDayExercise(dayId.toInt(), exTemplate.name, exTemplate.muscleGroup, exTemplate.equipment, exTemplate.sets, exTemplate.reps, exIndex)
            }
        }

        // Seed exercise library
        PredefinedSplitsLibrary.seedExerciseLibrary(dbHelper)

        // Navigate to weekly schedule setup
        val intent = Intent(this, WeeklyScheduleActivity::class.java)
        intent.putExtra("SPLIT_ID", splitId.toInt())
        intent.putExtra("IS_SETUP", true)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun onSkipClicked() {
        // Seed exercise library anyway
        PredefinedSplitsLibrary.seedExerciseLibrary(dbHelper)
        prefs.edit().putBoolean("setup_completed", true).apply()
        navigateToDashboard()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}

// ── Adapter for split template cards ───────────────────────────────────────
class SplitTemplateAdapter(
    private val templates: List<PredefinedSplitsLibrary.SplitTemplate>,
    private val onSelected: (Int) -> Unit
) : RecyclerView.Adapter<SplitTemplateAdapter.VH>() {

    private var selectedIndex = -1

    fun setSelected(index: Int) { 
        val old = selectedIndex
        selectedIndex = index
        if (old >= 0) notifyItemChanged(old)
        notifyItemChanged(index)
    }

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvSplitName)
        val tvMeta: TextView = view.findViewById(R.id.tvSplitMeta)
        val tvDescription: TextView = view.findViewById(R.id.tvSplitDescription)
        val tvGoal: TextView = view.findViewById(R.id.tvSplitGoal)
        val tvDifficulty: TextView = view.findViewById(R.id.tvDifficultyBadge)
        val tvSelected: TextView = view.findViewById(R.id.tvSelected)
        val card: com.google.android.material.card.MaterialCardView = view.findViewById(R.id.cardSplitTemplate)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val v = android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_split_template, parent, false)
        return VH(v)
    }

    override fun getItemCount() = templates.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val template = templates[position]
        holder.tvName.text = template.name
        holder.tvMeta.text = "${template.daysPerWeek} Days • ${template.difficulty}"
        holder.tvDescription.text = template.description
        holder.tvGoal.text = template.goal
        holder.tvDifficulty.text = template.difficulty.take(3).uppercase()

        val isSelected = position == selectedIndex
        holder.tvSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.card.strokeColor = if (isSelected) {
            holder.view.context.getColor(R.color.accent_gold)
        } else {
            holder.view.context.getColor(R.color.stroke_subtle)
        }
        holder.card.strokeWidth = if (isSelected) 2 else 1

        holder.card.setOnClickListener { onSelected(position) }
    }
}
