package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout

class ExercisePerformanceActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var milestoneChecker: ExerciseMilestoneChecker
    private lateinit var overloadEngine: ProgressiveOverloadEngine

    private var username = ""
    private var exerciseName = ""
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_performance)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        milestoneChecker = ExerciseMilestoneChecker(this)
        overloadEngine = ProgressiveOverloadEngine(this)
        username = prefs.getString("loggedInUser", "") ?: ""
        exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: ""

        if (exerciseName.isEmpty()) { finish(); return }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarExercisePerformance)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = exerciseName

        lineChart = findViewById(R.id.chartExerciseWeight)

        loadAll()
    }

    private fun loadAll() {
        val stats = dbHelper.getExerciseStats(username, exerciseName)

        // Overview section
        bindOverview(stats)

        // Stat cards
        bindStatCards(stats)

        // Progressive overload suggestion
        bindOverloadSuggestion()

        // Charts
        bindCharts(stats)

        // PR History
        bindPRHistory()

        // Milestones
        bindMilestones()

        // Training history
        bindSessionHistory()

        // Comparison
        bindComparison()
    }

    private fun bindOverview(stats: DatabaseHelper.ExerciseStats) {
        findViewById<TextView>(R.id.tvPerfExerciseName).text = exerciseName
        val exerciseLib = dbHelper.getExerciseLibrary(username).find { it.name.equals(exerciseName, ignoreCase = true) }
        findViewById<TextView>(R.id.tvPerfMuscleGroup).text = exerciseLib?.muscleGroup ?: ""
        findViewById<TextView>(R.id.tvPerfEquipment).text = exerciseLib?.equipment ?: ""
        findViewById<TextView>(R.id.tvPerfTimesPerformed).text = "${stats.totalSessions} sessions"
        val lastStr = if (stats.daysSinceLastSession == 0) "Today" else "${stats.daysSinceLastSession} days ago"
        findViewById<TextView>(R.id.tvPerfLastPerformed).text = "Last: $lastStr"
        val prText = if (stats.bestWeight > 0) "${stats.bestWeight} kg" else "None"
        findViewById<TextView>(R.id.tvPerfCurrentPR).text = "PR: $prText"
        val est1RM = if (stats.estimated1RM > 0) "${String.format("%.1f", stats.estimated1RM)} kg" else "—"
        findViewById<TextView>(R.id.tvPerf1RM).text = "est. 1RM: $est1RM"
    }

    private fun bindStatCards(stats: DatabaseHelper.ExerciseStats) {
        val currentW = if (stats.currentWeight > 0) "${stats.currentWeight} kg" else "—"
        val bestW = if (stats.bestWeight > 0) "${stats.bestWeight} kg" else "—"
        val avgW = if (stats.avgWeight > 0) "${String.format("%.1f", stats.avgWeight)} kg" else "—"
        val totalVol = if (stats.totalVolume > 0) "${String.format("%,.0f", stats.totalVolume)} kg" else "—"

        try {
            findViewById<TextView>(R.id.tvStatCurrentWeight).text = currentW
            findViewById<TextView>(R.id.tvStatBestWeight).text = bestW
            findViewById<TextView>(R.id.tvStatAvgWeight).text = avgW
            findViewById<TextView>(R.id.tvStatTotalVolume).text = totalVol
            findViewById<TextView>(R.id.tvStatMaxReps).text = if (stats.maxReps > 0) "${stats.maxReps}" else "—"
            findViewById<TextView>(R.id.tvStatTotalSets).text = "${stats.totalSets}"
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun bindOverloadSuggestion() {
        try {
            val suggestion = overloadEngine.getSuggestion(username, exerciseName)
            val tvSuggestion = findViewById<TextView>(R.id.tvOverloadSuggestion)
            val message = when (suggestion.recommendation) {
                "increase" -> "💡 Progressive Overload: Try ${suggestion.suggestedWeight} kg this session (+${suggestion.improvementKg} kg)"
                "maintain" -> "✅ Keep ${suggestion.suggestedWeight} kg — aim for more reps before increasing"
                "deload"   -> "⚠️ Consider deloading to ${String.format("%.1f", suggestion.suggestedWeight)} kg for better form"
                else       -> "🚀 Start this exercise and track your progress!"
            }
            tvSuggestion.text = message
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun bindCharts(stats: DatabaseHelper.ExerciseStats) {
        // Default: weight progress chart
        drawWeightChart(stats.weightHistory)

        // Tab listener for chart type selection
        val tabs = findViewById<TabLayout>(R.id.tabsChart)
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> drawWeightChart(stats.weightHistory)
                    1 -> drawVolumeChart(stats.volumeHistory)
                    2 -> draw1RMChart(stats.weightHistory)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun drawWeightChart(data: List<Pair<String, Double>>) {
        if (data.isEmpty()) { lineChart.clear(); lineChart.invalidate(); return }
        val entries = data.mapIndexed { i, (_, v) -> Entry(i.toFloat(), v.toFloat()) }
        val labels = data.map { it.first }
        setupLineChart(entries, labels, "Weight (kg)", Color.parseColor("#D32F2F"))
    }

    private fun drawVolumeChart(data: List<Pair<String, Double>>) {
        if (data.isEmpty()) { lineChart.clear(); lineChart.invalidate(); return }
        val entries = data.mapIndexed { i, (_, v) -> Entry(i.toFloat(), v.toFloat()) }
        val labels = data.map { it.first }
        setupLineChart(entries, labels, "Volume (kg)", Color.parseColor("#D4AF37"))
    }

    private fun draw1RMChart(data: List<Pair<String, Double>>) {
        if (data.isEmpty()) { lineChart.clear(); lineChart.invalidate(); return }
        val entries = data.mapIndexed { i, (_, v) -> Entry(i.toFloat(), (v * 1.033).toFloat()) }
        val labels = data.map { it.first }
        setupLineChart(entries, labels, "Est. 1RM (kg)", Color.parseColor("#4CAF50"))
    }

    private fun setupLineChart(entries: List<Entry>, labels: List<String>, label: String, lineColor: Int) {
        val dataset = LineDataSet(entries, label).apply {
            color = lineColor
            setCircleColor(lineColor)
            lineWidth = 2.5f; circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            fillColor = lineColor; fillAlpha = 30; setDrawFilled(true)
        }
        lineChart.apply {
            this.data = LineData(dataset)
            description.isEnabled = false
            legend.textColor = Color.WHITE
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels.takeLast(10))
                setDrawGridLines(false)
            }
            axisLeft.apply { textColor = Color.WHITE; gridColor = Color.parseColor("#333333") }
            axisRight.isEnabled = false
            animateX(600)
            invalidate()
        }
    }

    private fun bindPRHistory() {
        try {
            val prHistory = dbHelper.getPRHistoryForExercise(username, exerciseName)
            val rv = findViewById<RecyclerView>(R.id.rvPRHistory)
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rv.adapter = PRHistoryAdapter(prHistory)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun bindMilestones() {
        try {
            val milestones = milestoneChecker.getMilestonesForExercise(username, exerciseName)
            val rv = findViewById<RecyclerView>(R.id.rvMilestones)
            rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rv.adapter = MilestoneAdapter(milestones)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun bindSessionHistory() {
        try {
            val history = dbHelper.getExerciseSessionHistory(username, exerciseName).take(20)
            val rv = findViewById<RecyclerView>(R.id.rvExerciseHistory)
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = ExerciseHistoryAdapter(history)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun bindComparison() {
        try {
            val comparison = dbHelper.getExerciseComparisonStats(username, exerciseName)
            val thisWeekVol = comparison["thisWeekVolume"] as? Double ?: 0.0
            val lastWeekVol = comparison["lastWeekVolume"] as? Double ?: 0.0
            val thisWeekBest = comparison["thisWeekBestWeight"] as? Double ?: 0.0
            val lastWeekBest = comparison["lastWeekBestWeight"] as? Double ?: 0.0

            try {
                val tvCompare = findViewById<TextView>(R.id.tvComparisonSummary)
                val volDiff = thisWeekVol - lastWeekVol
                val wDiff = thisWeekBest - lastWeekBest
                val volSign = if (volDiff >= 0) "+" else ""
                tvCompare.text = "This week vs last week:\nVolume: $volSign${String.format("%.0f", volDiff)} kg | Best: $volSign${String.format("%.1f", wDiff)} kg"
            } catch (e: Exception) { }
        } catch (e: Exception) { e.printStackTrace() }
    }
}

// ── PR History Adapter (horizontal) ─────────────────────────────────────────
class PRHistoryAdapter(private val prs: List<Pair<String, Double>>) : RecyclerView.Adapter<PRHistoryAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvWeight: TextView = view.findViewById(R.id.tvPRWeight)
        val tvDate: TextView = view.findViewById(R.id.tvPRDate)
    }
    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pr_history, parent, false)
        return VH(v)
    }
    override fun getItemCount() = prs.size
    override fun onBindViewHolder(holder: VH, pos: Int) {
        val (date, weight) = prs[pos]
        holder.tvWeight.text = "$weight kg"
        holder.tvDate.text = date
    }
}

// ── Milestone Adapter (horizontal) ──────────────────────────────────────────
class MilestoneAdapter(private val milestones: List<ExerciseMilestoneChecker.Milestone>) : RecyclerView.Adapter<MilestoneAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvMilestoneTitle)
        val card: com.google.android.material.card.MaterialCardView = view.findViewById(R.id.cardMilestone)
    }
    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_milestone, parent, false)
        return VH(v)
    }
    override fun getItemCount() = milestones.size
    override fun onBindViewHolder(holder: VH, pos: Int) {
        val milestone = milestones[pos]
        holder.tvTitle.text = milestone.title
        holder.tvTitle.alpha = if (milestone.isUnlocked) 1.0f else 0.4f
        holder.card.strokeColor = if (milestone.isUnlocked) holder.itemView.context.getColor(R.color.accent_gold) else holder.itemView.context.getColor(R.color.stroke_subtle)
    }
}

// ── Exercise Session History Adapter ─────────────────────────────────────────
class ExerciseHistoryAdapter(private val logs: List<SessionExerciseLog>) : RecyclerView.Adapter<ExerciseHistoryAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvHistDate)
        val tvStats: TextView = view.findViewById(R.id.tvHistStats)
        val tvVolume: TextView = view.findViewById(R.id.tvHistVolume)
        val tvPR: TextView = view.findViewById(R.id.tvHistPR)
    }
    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_history, parent, false)
        return VH(v)
    }
    override fun getItemCount() = logs.size
    override fun onBindViewHolder(holder: VH, pos: Int) {
        val log = logs[pos]
        holder.tvDate.text = log.date
        holder.tvStats.text = "${log.weight} kg × ${log.sets}×${log.reps}"
        holder.tvVolume.text = "${String.format("%,.0f", log.weight * log.sets * log.reps)} kg vol"
        holder.tvPR.visibility = if (log.isNewPR) View.VISIBLE else View.GONE
    }
}
