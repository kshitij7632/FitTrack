package com.fittrack.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AchievementAdapter(
    private val achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.achievementContainer)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val ivStatus: ImageView = view.findViewById(R.id.ivStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.tvName.text = achievement.name
        holder.tvDescription.text = achievement.description

        if (achievement.isUnlocked) {
            holder.container.setBackgroundResource(R.drawable.bg_achievement_unlocked)
            holder.ivIcon.setColorFilter(Color.parseColor("#D4AF37")) // Gold
            holder.ivStatus.setImageResource(R.drawable.ic_check)
            holder.ivStatus.setColorFilter(Color.parseColor("#D4AF37"))
            holder.tvDate.visibility = View.VISIBLE
            holder.tvDate.text = "Unlocked on ${achievement.unlockedDate}"
            holder.tvName.setTextColor(Color.WHITE)
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_achievement_locked)
            holder.ivIcon.setColorFilter(Color.parseColor("#333333")) // divider_light
            holder.ivStatus.setImageResource(R.drawable.ic_launcher_foreground) // Placeholder locked icon, maybe a lock
            holder.ivStatus.setColorFilter(Color.parseColor("#333333"))
            holder.tvDate.visibility = View.GONE
            holder.tvName.setTextColor(Color.parseColor("#757575")) // text_hint
        }
    }

    override fun getItemCount() = achievements.size
}
