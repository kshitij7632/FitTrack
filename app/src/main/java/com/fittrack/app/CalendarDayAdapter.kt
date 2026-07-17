package com.fittrack.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarDayAdapter(
    private val days: List<String>,
    private val workoutDates: Set<String>,
    private val currentMonthStr: String, // e.g. "07/2026"
    private val todayStr: String, // e.g. "17/07/2026"
    private val onDayClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
        val dayContainer: FrameLayout = view.findViewById(R.id.dayContainer)
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
            
            // Format to dd/MM/yyyy to check against workoutDates and todayStr
            val dayFormatted = day.padStart(2, '0')
            val fullDateStr = "$dayFormatted/$currentMonthStr"
            
            if (fullDateStr == todayStr) {
                holder.dayContainer.setBackgroundResource(R.drawable.bg_calendar_today)
            } else if (workoutDates.contains(fullDateStr)) {
                holder.dayContainer.setBackgroundResource(R.drawable.bg_calendar_workout)
            } else {
                holder.dayContainer.setBackgroundResource(R.drawable.bg_calendar_rest)
            }
            
            holder.itemView.setOnClickListener {
                onDayClick(fullDateStr)
            }
        }
    }

    override fun getItemCount() = days.size
}
