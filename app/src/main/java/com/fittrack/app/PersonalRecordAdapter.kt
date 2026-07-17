package com.fittrack.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PersonalRecordAdapter(
    private val records: List<DatabaseHelper.DetailedPR>
) : RecyclerView.Adapter<PersonalRecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRecordExercise: TextView = itemView.findViewById(R.id.tvRecordExercise)
        val tvRecordDate: TextView = itemView.findViewById(R.id.tvRecordDate)
        val tvRecordWeight: TextView = itemView.findViewById(R.id.tvRecordWeight)
        val tvEst1RM: TextView = itemView.findViewById(R.id.tvEst1RM)
        val tvMaxVolume: TextView = itemView.findViewById(R.id.tvMaxVolume)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personal_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.tvRecordExercise.text = record.exerciseName
        
        if (record.maxWeightDate.isNotEmpty()) {
            holder.tvRecordDate.text = "Achieved on ${record.maxWeightDate}"
        } else {
            holder.tvRecordDate.text = ""
        }
        
        holder.tvRecordWeight.text = "${String.format("%.1f", record.maxWeight)} kg"
        holder.tvEst1RM.text = "${String.format("%.1f", record.estimated1RM)} kg"
        holder.tvMaxVolume.text = "${String.format("%.0f", record.maxVolume)} kg"
    }

    override fun getItemCount(): Int = records.size
}
