package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MySplitsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private var username = ""
    private lateinit var adapter: MySplitsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_splits)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarMySplits)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()

        // Create new custom split
        findViewById<ExtendedFloatingActionButton>(R.id.fabCreateSplit).setOnClickListener {
            showCreateSplitOptions()
        }

        // Manage schedule button
        try {
            findViewById<MaterialButton>(R.id.btnManageSchedule).setOnClickListener {
                val activeSplit = dbHelper.getActiveSplit(username)
                if (activeSplit != null) {
                    val intent = Intent(this, WeeklyScheduleActivity::class.java)
                    intent.putExtra("SPLIT_ID", activeSplit.id)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    Toast.makeText(this, "Please activate a split first", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) { }
    }

    override fun onResume() {
        super.onResume()
        loadSplits()
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvMySplits)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = MySplitsAdapter(
            emptyList(),
            username,
            dbHelper,
            onEdit = { split ->
                val intent = Intent(this, SplitEditorActivity::class.java)
                intent.putExtra("SPLIT_ID", split.id)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            },
            onSetActive = { split ->
                dbHelper.setActiveSplit(username, split.id)
                loadSplits()
                Toast.makeText(this, "${split.name} is now active!", Toast.LENGTH_SHORT).show()
            },
            onDelete = { split ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Split")
                    .setMessage("Are you sure you want to delete \"${split.name}\"? This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ ->
                        dbHelper.deleteSplit(split.id, username)
                        loadSplits()
                        Toast.makeText(this, "Split deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        rv.adapter = adapter
    }

    private fun loadSplits() {
        val splits = dbHelper.getAllSplitsForUser(username)
        val activeSplit = dbHelper.getActiveSplit(username)
        adapter.updateData(splits, activeSplit?.id ?: -1)

        try {
            val tvNoSplits = findViewById<TextView>(R.id.tvNoSplits)
            tvNoSplits.visibility = if (splits.isEmpty()) View.VISIBLE else View.GONE
        } catch (e: Exception) { }
    }

    private fun showCreateSplitOptions() {
        val options = arrayOf("📋 Use Predefined Split", "✏️ Create Custom Split")
        AlertDialog.Builder(this)
            .setTitle("Add Workout Split")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        startActivity(Intent(this, WorkoutPlannerSetupActivity::class.java))
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    1 -> {
                        startActivity(Intent(this, SplitEditorActivity::class.java))
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            }
            .show()
    }
}

class MySplitsAdapter(
    private var splits: List<WorkoutSplit>,
    private val username: String,
    private val db: DatabaseHelper,
    private val onEdit: (WorkoutSplit) -> Unit,
    private val onSetActive: (WorkoutSplit) -> Unit,
    private val onDelete: (WorkoutSplit) -> Unit
) : RecyclerView.Adapter<MySplitsAdapter.VH>() {

    private var activeSplitId = -1

    fun updateData(newSplits: List<WorkoutSplit>, newActiveSplitId: Int) {
        splits = newSplits
        activeSplitId = newActiveSplitId
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardMySplit)
        val tvName: TextView = view.findViewById(R.id.tvSplitName)
        val tvDesc: TextView = view.findViewById(R.id.tvSplitDesc)
        val tvDays: TextView = view.findViewById(R.id.tvSplitDays)
        val tvActiveBadge: TextView = view.findViewById(R.id.tvActiveBadge)
        val btnActivate: MaterialButton = view.findViewById(R.id.btnActivateSplit)
        val btnEditSplit: MaterialButton = view.findViewById(R.id.btnEditSplit)
        val btnDeleteSplit: MaterialButton = view.findViewById(R.id.btnDeleteSplit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_my_split, parent, false)
        return VH(v)
    }

    override fun getItemCount() = splits.size

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val split = splits[pos]
        val isActive = split.id == activeSplitId
        holder.tvName.text = split.name
        holder.tvDesc.text = split.description.ifEmpty { "Custom workout split" }
        val days = db.getWorkoutDaysForSplit(split.id)
        holder.tvDays.text = "${days.size} workout days"
        holder.tvActiveBadge.visibility = if (isActive) View.VISIBLE else View.GONE
        holder.card.strokeColor = if (isActive) holder.itemView.context.getColor(R.color.accent_red) else holder.itemView.context.getColor(R.color.stroke_subtle)

        holder.btnActivate.text = if (isActive) "✓ Active" else "Set Active"
        holder.btnActivate.setOnClickListener { if (!isActive) onSetActive(split) }

        holder.btnEditSplit.setOnClickListener { onEdit(split) }
        holder.btnDeleteSplit.setOnClickListener { onDelete(split) }
    }
}
