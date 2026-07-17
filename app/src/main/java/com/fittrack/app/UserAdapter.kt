package com.fittrack.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val users: List<Triple<String, String, Int>>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserWorkouts: TextView = itemView.findViewById(R.id.tvUserWorkouts)
        val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val (username, role, _) = users[position]

        holder.tvUserName.text = username.replaceFirstChar { it.uppercase() }
        holder.tvUserWorkouts.text = "Registered user"

        if (role == "admin") {
            holder.tvUserRole.text = "  👑 Admin  "
            holder.tvUserRole.setBackgroundResource(R.drawable.bg_admin_chip)
            holder.tvUserRole.setTextColor(
                holder.itemView.context.getColor(R.color.admin_badge)
            )
        } else {
            holder.tvUserRole.text = "  🏋️ User  "
            holder.tvUserRole.setBackgroundResource(R.drawable.bg_user_chip)
            holder.tvUserRole.setTextColor(
                holder.itemView.context.getColor(R.color.user_badge)
            )
        }
    }

    override fun getItemCount(): Int = users.size
}
