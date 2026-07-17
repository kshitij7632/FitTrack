package com.fittrack.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.io.File

class WorkoutAdapter(
    private var workouts: MutableList<Workout>,
    private val onEditClick: (Workout) -> Unit,
    private val onDeleteClick: (Workout) -> Unit,
    private val onItemClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivWorkoutImage: ImageView = itemView.findViewById(R.id.ivWorkoutImage)
        val tvExerciseName: TextView = itemView.findViewById(R.id.tvExerciseName)
        val tvMuscleGroup: TextView = itemView.findViewById(R.id.tvMuscleGroup)
        val tvWeight: TextView = itemView.findViewById(R.id.tvWeight)
        val tvSetsReps: TextView = itemView.findViewById(R.id.tvSetsReps)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(workouts[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]

        holder.tvExerciseName.text = workout.exerciseName
        holder.tvMuscleGroup.text = "  ${workout.muscleGroup}  "
        holder.tvWeight.text = "${workout.weight}"
        holder.tvSetsReps.text = "${workout.sets} × ${workout.reps}"
        holder.tvDuration.text = "${workout.duration}"
        holder.tvDate.text = workout.date

        if (workout.notes.isNotEmpty()) {
            holder.tvNotes.visibility = View.VISIBLE
            holder.tvNotes.text = workout.notes
        } else {
            holder.tvNotes.visibility = View.GONE
        }

        // Handle Image
        if (workout.imagePath.isNotEmpty()) {
            val file = File(workout.imagePath)
            if (file.exists()) {
                holder.ivWorkoutImage.setImageURI(Uri.fromFile(file))
            } else {
                holder.ivWorkoutImage.setImageResource(R.drawable.ic_image)
            }
        } else {
            holder.ivWorkoutImage.setImageResource(R.drawable.ic_image)
        }

        holder.btnEdit.setOnClickListener { onEditClick(workout) }
        holder.btnDelete.setOnClickListener { onDeleteClick(workout) }
    }

    override fun getItemCount(): Int = workouts.size

    fun updateList(newWorkouts: List<Workout>) {
        workouts.clear()
        workouts.addAll(newWorkouts)
        notifyDataSetChanged()
    }
}
