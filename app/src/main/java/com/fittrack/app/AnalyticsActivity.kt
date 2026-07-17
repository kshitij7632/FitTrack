package com.fittrack.app

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var scoreCalculator: FitnessScoreCalculator

    private lateinit var progressScore: ProgressBar
    private lateinit var tvScore: TextView
    private lateinit var tvWeeklyWorkouts: TextView
    private lateinit var tvAvgDuration: TextView
    private lateinit var tvLeastTrained: TextView
    private lateinit var tvMostImproved: TextView

    private lateinit var chartFrequency: BarChart
    private lateinit var chartMuscle: PieChart
    private lateinit var chartVolume: LineChart
    private lateinit var chartDuration: LineChart
    
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        prefs = getSharedPreferences("FitTrackPrefs", MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        scoreCalculator = FitnessScoreCalculator(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAnalytics)
        setSupportActionBar(toolbar)

        initViews()
        setupBottomNav()
        loadData()
    }

    private fun initViews() {
        progressScore = findViewById(R.id.progressScore)
        tvScore = findViewById(R.id.tvScore)
        tvWeeklyWorkouts = findViewById(R.id.tvWeeklyWorkouts)
        tvAvgDuration = findViewById(R.id.tvAvgDuration)
        tvLeastTrained = findViewById(R.id.tvLeastTrained)
        tvMostImproved = findViewById(R.id.tvMostImproved)
        
        chartFrequency = findViewById(R.id.chartFrequency)
        chartMuscle = findViewById(R.id.chartMuscle)
        chartVolume = findViewById(R.id.chartVolume)
        chartDuration = findViewById(R.id.chartDuration)
        
        bottomNav = findViewById(R.id.bottomNavigation)
        
        findViewById<FloatingActionButton>(R.id.fabAddWorkoutPlaceholder).setOnClickListener {
            startActivity(Intent(this, AddWorkoutActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun setupBottomNav() {
        bottomNav.background = null
        bottomNav.menu.getItem(2).isEnabled = false
        bottomNav.selectedItemId = R.id.nav_analytics
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TOP })
                    finish()
                    false
                }
                R.id.nav_analytics -> true
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
                    finish()
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NO_ANIMATION })
                    finish()
                    false
                }
                else -> false
            }
        }
    }

    private fun loadData() {
        val username = prefs.getString("loggedInUser", "User") ?: "User"
        
        // Summary Stats
        val score = scoreCalculator.calculateScore(username)
        tvScore.text = score.toString()
        progressScore.progress = score
        
        tvWeeklyWorkouts.text = dbHelper.getWeeklyWorkoutCount(username).toString()
        tvAvgDuration.text = dbHelper.getAverageWorkoutDuration(username).toString()
        
        val leastTrained = dbHelper.getLeastTrainedMuscleGroup(username)
        tvLeastTrained.text = if (leastTrained == "N/A" || leastTrained.isEmpty()) "—" else leastTrained
        
        val mostImproved = dbHelper.getMostImprovedExercise(username)
        tvMostImproved.text = if (mostImproved == "N/A" || mostImproved.isEmpty()) "—" else mostImproved

        // Setup Charts
        setupFrequencyChart(username)
        setupMuscleChart(username)
        setupVolumeChart(username)
        setupDurationChart(username)
    }

    private fun setupFrequencyChart(username: String) {
        val map = dbHelper.getWorkoutsByWeekday(username)
        val entries = ArrayList<BarEntry>()
        // 1=Sun, 7=Sat -> display as Mon to Sun (2,3,4,5,6,7,1)
        val orderedKeys = listOf(2, 3, 4, 5, 6, 7, 1)
        for (i in orderedKeys.indices) {
            entries.add(BarEntry(i.toFloat(), map[orderedKeys[i]]?.toFloat() ?: 0f))
        }

        val dataSet = BarDataSet(entries, "Workouts")
        dataSet.color = Color.parseColor("#D32F2F") // accent_red
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.5f

        chartFrequency.data = data
        chartFrequency.description.isEnabled = false
        chartFrequency.legend.isEnabled = false
        chartFrequency.setFitBars(true)

        val xAxis = chartFrequency.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)

        chartFrequency.axisLeft.textColor = Color.WHITE
        chartFrequency.axisLeft.axisMinimum = 0f
        chartFrequency.axisRight.isEnabled = false
        
        chartFrequency.animateY(1000)
        chartFrequency.invalidate()
    }

    private fun setupMuscleChart(username: String) {
        val list = dbHelper.getMuscleGroupDistribution(username)
        val entries = ArrayList<PieEntry>()
        for (pair in list) {
            entries.add(PieEntry(pair.second.toFloat(), pair.first))
        }

        val dataSet = PieDataSet(entries, "")
        val colors = listOf(
            Color.parseColor("#D32F2F"), // Red
            Color.parseColor("#D4AF37"), // Gold
            Color.parseColor("#181818"), // Secondary
            Color.parseColor("#2E7D32"), // Green
            Color.parseColor("#EF5350")  // Light Red
        )
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())

        chartMuscle.data = data
        chartMuscle.description.isEnabled = false
        chartMuscle.legend.textColor = Color.WHITE
        chartMuscle.setUsePercentValues(true)
        chartMuscle.setEntryLabelColor(Color.WHITE)
        chartMuscle.setHoleColor(Color.parseColor("#0D0D0D"))
        
        chartMuscle.animateY(1000)
        chartMuscle.invalidate()
    }

    private fun setupVolumeChart(username: String) {
        val list = dbHelper.getVolumeTrend(username)
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        
        for (i in list.indices) {
            entries.add(Entry(i.toFloat(), list[i].second.toFloat()))
            labels.add(list[i].first.substring(0, 5)) // just dd/MM
        }

        val dataSet = LineDataSet(entries, "Volume (kg)")
        dataSet.color = Color.parseColor("#D4AF37") // Gold
        dataSet.setCircleColor(Color.parseColor("#D4AF37"))
        dataSet.valueTextColor = Color.WHITE
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        val data = LineData(dataSet)
        
        chartVolume.data = data
        chartVolume.description.isEnabled = false
        chartVolume.legend.textColor = Color.WHITE

        val xAxis = chartVolume.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)

        chartVolume.axisLeft.textColor = Color.WHITE
        chartVolume.axisRight.isEnabled = false
        
        chartVolume.animateX(1000)
        chartVolume.invalidate()
    }

    private fun setupDurationChart(username: String) {
        val list = dbHelper.getDurationTrend(username)
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        
        for (i in list.indices) {
            entries.add(Entry(i.toFloat(), list[i].second.toFloat()))
            labels.add(list[i].first.substring(0, 5)) // just dd/MM
        }

        val dataSet = LineDataSet(entries, "Duration (min)")
        dataSet.color = Color.parseColor("#D32F2F") // Red
        dataSet.setCircleColor(Color.parseColor("#D32F2F"))
        dataSet.valueTextColor = Color.WHITE
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val data = LineData(dataSet)
        
        chartDuration.data = data
        chartDuration.description.isEnabled = false
        chartDuration.legend.textColor = Color.WHITE

        val xAxis = chartDuration.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)

        chartDuration.axisLeft.textColor = Color.WHITE
        chartDuration.axisRight.isEnabled = false
        
        chartDuration.animateX(1000)
        chartDuration.invalidate()
    }
}
