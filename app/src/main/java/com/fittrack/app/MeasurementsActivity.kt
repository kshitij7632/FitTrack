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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MeasurementsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private var username = ""
    private var selectedField = "bodyWeight"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurements)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        username = prefs.getString("loggedInUser", "") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarMeasurements)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<ExtendedFloatingActionButton>(R.id.fabLogMeasurement).setOnClickListener {
            startActivity(Intent(this, AddMeasurementActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        loadMeasurements()
        setupChartFieldButtons()
    }

    override fun onResume() {
        super.onResume()
        loadMeasurements()
        updateChart(selectedField)
    }

    private fun loadMeasurements() {
        val latest = dbHelper.getLatestMeasurement(username)
        val previous = dbHelper.getPreviousMeasurement(username)

        val measurements = buildMeasurementRows(latest, previous)

        val rv = findViewById<RecyclerView>(R.id.rvMeasurements)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = MeasurementRowAdapter(measurements)
    }

    private fun buildMeasurementRows(latest: BodyMeasurement?, prev: BodyMeasurement?): List<MeasurementRow> {
        val rows = mutableListOf<MeasurementRow>()
        if (latest == null) return rows

        data class MField(val label: String, val current: Double, val previous: Double)
        val fields = listOf(
            MField("Body Weight", latest.bodyWeight, prev?.bodyWeight ?: 0.0),
            MField("Chest", latest.chest, prev?.chest ?: 0.0),
            MField("Waist", latest.waist, prev?.waist ?: 0.0),
            MField("Hips", latest.hips, prev?.hips ?: 0.0),
            MField("L. Arm", latest.leftArm, prev?.leftArm ?: 0.0),
            MField("R. Arm", latest.rightArm, prev?.rightArm ?: 0.0),
            MField("L. Forearm", latest.leftForearm, prev?.leftForearm ?: 0.0),
            MField("R. Forearm", latest.rightForearm, prev?.rightForearm ?: 0.0),
            MField("L. Thigh", latest.leftThigh, prev?.leftThigh ?: 0.0),
            MField("R. Thigh", latest.rightThigh, prev?.rightThigh ?: 0.0),
            MField("L. Calf", latest.leftCalf, prev?.leftCalf ?: 0.0),
            MField("R. Calf", latest.rightCalf, prev?.rightCalf ?: 0.0),
            MField("Neck", latest.neck, prev?.neck ?: 0.0),
            MField("Shoulders", latest.shoulderWidth, prev?.shoulderWidth ?: 0.0),
            MField("Body Fat %", latest.bodyFat, prev?.bodyFat ?: 0.0)
        ).filter { it.current > 0 }

        for (f in fields) {
            val diff = if (f.previous > 0) f.current - f.previous else 0.0
            val pct = if (f.previous > 0) (diff / f.previous) * 100 else 0.0
            val unit = if (f.label == "Body Weight") " kg" else if (f.label == "Body Fat %") "%" else " cm"
            rows.add(MeasurementRow(f.label, f.current, f.previous, diff, pct, unit))
        }
        return rows
    }

    private fun setupChartFieldButtons() {
        val container = findViewById<ViewGroup>(R.id.layoutChartSelector)
        container.removeAllViews()

        val fields = listOf("bodyWeight" to "Weight", "chest" to "Chest", "waist" to "Waist", "leftArm" to "L.Arm", "rightArm" to "R.Arm", "leftThigh" to "L.Thigh", "bodyFat" to "BF%")
        for ((fieldKey, label) in fields) {
            val btn = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                text = label; textSize = 12f; setPadding(24, 4, 24, 4)
                setOnClickListener { selectedField = fieldKey; updateChart(fieldKey) }
                layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = 8 }
            }
            container.addView(btn)
        }
        updateChart("bodyWeight")
    }

    private fun updateChart(fieldKey: String) {
        val chart = findViewById<LineChart>(R.id.chartMeasurement)
        val data = dbHelper.getMeasurementTrend(username, fieldKey)
        if (data.isEmpty()) { chart.clear(); chart.invalidate(); return }

        val entries = data.mapIndexed { i, (_, v) -> Entry(i.toFloat(), v.toFloat()) }
        val labels = data.map { it.first }

        val dataSet = LineDataSet(entries, fieldKey).apply {
            color = Color.parseColor("#D32F2F")
            setCircleColor(Color.parseColor("#D4AF37"))
            lineWidth = 2.5f; circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            valueTextColor = Color.WHITE
        }

        chart.apply {
            description.isEnabled = false
            legend.textColor = Color.WHITE
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            xAxis.apply { valueFormatter = IndexAxisValueFormatter(labels); textColor = Color.WHITE; position = XAxis.XAxisPosition.BOTTOM; granularity = 1f; setDrawGridLines(false) }
            axisLeft.apply { textColor = Color.WHITE; setDrawGridLines(true); gridColor = Color.parseColor("#333333") }
            axisRight.isEnabled = false
            this.data = LineData(dataSet)
            animateX(800)
            invalidate()
        }
    }

    data class MeasurementRow(val label: String, val current: Double, val previous: Double, val diff: Double, val pctChange: Double, val unit: String)
}

class MeasurementRowAdapter(private val rows: List<MeasurementsActivity.MeasurementRow>) : RecyclerView.Adapter<MeasurementRowAdapter.VH>() {
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tvMeasLabel)
        val tvCurrent: TextView = view.findViewById(R.id.tvMeasCurrent)
        val tvPrev: TextView = view.findViewById(R.id.tvMeasPrev)
        val tvChange: TextView = view.findViewById(R.id.tvMeasChange)
    }
    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_measurement_row, parent, false)
        return VH(v)
    }
    override fun getItemCount() = rows.size
    override fun onBindViewHolder(holder: VH, pos: Int) {
        val row = rows[pos]
        holder.tvLabel.text = row.label
        holder.tvCurrent.text = String.format("%.1f%s", row.current, row.unit)
        holder.tvPrev.text = if (row.previous > 0) String.format("%.1f%s", row.previous, row.unit) else "—"
        if (row.diff == 0.0) {
            holder.tvChange.text = "—"; holder.tvChange.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
        } else {
            val sign = if (row.diff > 0) "+" else ""
            holder.tvChange.text = "$sign${String.format("%.1f", row.diff)}${row.unit}"
            holder.tvChange.setTextColor(holder.itemView.context.getColor(if (row.diff < 0) R.color.accent_red else R.color.accent_gold))
        }
    }
}
