package com.example.youart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.youart.R
import com.example.youart.Notification

/**
 * RecyclerViewAdapter for Notifications in NotifactionFragment
 * with ViewHolder included
 */
class NotificationAdapter(
    private val context: Context,
    private val notifications: List<Notification>,
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        Glide.with(context)
            .load(notification.notificationImage)
            .circleCrop()
            .into(holder.notificationIv);
        holder.notificationMessageTxt.text = notification.notificationMessage
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val notificationIv: ImageView = itemView.findViewById(R.id.notificationIv)
        val notificationMessageTxt: TextView = itemView.findViewById(R.id.notificationMessageTxt)
    }
}