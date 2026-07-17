package com.fittrack.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ExerciseProgressAdapter(
    private val progressList: List<ExerciseProgress>
) : RecyclerView.Adapter<ExerciseProgressAdapter.ProgressViewHolder>() {

    class ProgressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvExerciseName: TextView = view.findViewById(R.id.tvExerciseName)
        val tvIncrease: TextView = view.findViewById(R.id.tvIncrease)
        val tvCurrentWeight: TextView = view.findViewById(R.id.tvCurrentWeight)
        val tvBestWeight: TextView = view.findViewById(R.id.tvBestWeight)
        val tv1RM: TextView = view.findViewById(R.id.tv1RM)
        val chart: LineChart = view.findViewById(R.id.chartExerciseProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_progress, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val progress = progressList[position]
        holder.tvExerciseName.text = progress.exerciseName
        
        holder.tvCurrentWeight.text = "${String.format("%.1f", progress.currentWeight)} kg"
        holder.tvBestWeight.text = "${String.format("%.1f", progress.bestWeight)} kg"
        holder.tv1RM.text = "${String.format("%.1f", progress.estimatedOneRepMax)} kg"

        // Improvement %
        val pct = progress.improvementPercent
        if (pct > 0) {
            holder.tvIncrease.text = "+${String.format("%.0f", pct)}%"
            holder.tvIncrease.setTextColor(Color.parseColor("#4CAF50"))
        } else if (pct < 0) {
            holder.tvIncrease.text = "${String.format("%.0f", pct)}%"
            holder.tvIncrease.setTextColor(Color.parseColor("#EF5350"))
        } else {
            holder.tvIncrease.text = "0%"
            holder.tvIncrease.setTextColor(Color.parseColor("#BDBDBD"))
        }

        // Chart
        setupChart(holder.chart, progress.history)
    }

    private fun setupChart(chart: LineChart, history: List<Pair<String, Double>>) {
        if (history.isEmpty()) {
            chart.clear()
            return
        }
        
        val entries = ArrayList<Entry>()
        for (i in history.indices) {
            entries.add(Entry(i.toFloat(), history[i].second.toFloat()))
        }

        val dataSet = LineDataSet(entries, "")
        dataSet.color = Color.parseColor("#D4AF37")
        dataSet.setCircleColor(Color.parseColor("#D4AF37"))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 3f
        dataSet.setDrawValues(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#D4AF37")
        dataSet.fillAlpha = 50

        val data = LineData(dataSet)
        chart.data = data
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(false)

        chart.axisLeft.textColor = Color.WHITE
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        
        chart.setTouchEnabled(false)
        
        chart.animateX(1000)
        chart.invalidate()
    }

    override fun getItemCount() = progressList.size
}
