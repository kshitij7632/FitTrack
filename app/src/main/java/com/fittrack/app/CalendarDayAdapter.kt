package com.fittrack.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date

class CalendarDayAdapter(
    private val days: List<String>,
    private val monthStatuses: Map<String, CalendarActivity.DayStatus>,
    private val currentMonthDate: Date,
    private val todayStr: String,
    private val onDayClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    private val sdfKey = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    private val sdfMonthYear = java.text.SimpleDateFormat("MM/yyyy", java.util.Locale.getDefault())

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
        val dayContainer: FrameLayout = view.findViewById(R.id.dayContainer)
        val tvIndicatorPR: TextView = view.findViewById(R.id.tvIndicatorPR)
        val tvIndicatorStreak: TextView = view.findViewById(R.id.tvIndicatorStreak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.tvDayNumber.text = day

        if (day.isEmpty()) {
            holder.dayContainer.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener(null)
        } else {
            holder.dayContainer.visibility = View.VISIBLE
            
            val dayFormatted = day.padStart(2, '0')
            val fullDateStr = "$dayFormatted/${sdfMonthYear.format(currentMonthDate)}"
            
            val status = monthStatuses[fullDateStr] ?: CalendarActivity.DayStatus(CalendarActivity.DayType.REST)
            
            holder.tvDayNumber.setTextColor(Color.WHITE)
            
            // Set background color according to requirement: 
            // 🟢 Completed Workout, 🔵 Planned Workout, 🟡 Missed Workout, ⚫ Rest Day
            when (status.type) {
                CalendarActivity.DayType.COMPLETED -> holder.dayContainer.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                CalendarActivity.DayType.PLANNED -> holder.dayContainer.setBackgroundColor(Color.parseColor("#2196F3")) // Blue
                CalendarActivity.DayType.MISSED -> holder.dayContainer.setBackgroundColor(Color.parseColor("#FFC107")) // Yellow
                CalendarActivity.DayType.REST -> holder.dayContainer.setBackgroundColor(Color.parseColor("#1E1E1E")) // Dark gray / Black
            }
            
            if (status.type == CalendarActivity.DayType.MISSED) {
                holder.tvDayNumber.setTextColor(Color.BLACK)
            }
            
            // Highlight today's border if we wanted to... or just keep it simple.
            if (fullDateStr == todayStr) {
                holder.dayContainer.setBackgroundResource(R.drawable.bg_calendar_today)
                // Need to re-apply color as tint if using drawable, or just override. Let's create a custom approach:
                // If it's today, we might want to just make the text gold.
                holder.tvDayNumber.setTextColor(Color.parseColor("#D4AF37")) // Gold text for today
            }

            // Indicators
            holder.tvIndicatorPR.visibility = if (status.hasPR) View.VISIBLE else View.GONE
            holder.tvIndicatorStreak.visibility = if (status.hasStreak) View.VISIBLE else View.GONE
            
            holder.itemView.setOnClickListener {
                onDayClick(fullDateStr)
            }
        }
    }

    override fun getItemCount() = days.size
}
