package com.thejaustin.simweather.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thejaustin.simweather.R
import com.thejaustin.simweather.data.model.Alert
import java.text.SimpleDateFormat
import java.util.*

class AlertAdapter : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    private var alerts: List<Alert> = emptyList()

    fun submitList(alertList: List<Alert>) {
        alerts = alertList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount() = alerts.size

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSeverity: TextView = itemView.findViewById(R.id.tvAlertSeverity)
        private val tvHeadline: TextView = itemView.findViewById(R.id.tvAlertHeadline)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvAlertDesc)
        private val tvTime: TextView = itemView.findViewById(R.id.tvAlertTime)

        fun bind(alert: Alert) {
            tvSeverity.text = alert.severity.uppercase()
            tvHeadline.text = alert.headline
            tvDesc.text = alert.desc

            // Format times
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

                val effectiveDate = inputFormat.parse(alert.effective)
                val expiresDate = inputFormat.parse(alert.expires)

                val effectiveStr = effectiveDate?.let { outputFormat.format(it) } ?: alert.effective
                val expiresStr = expiresDate?.let { outputFormat.format(it) } ?: alert.expires

                tvTime.text = "Effective: $effectiveStr - Expires: $expiresStr"
            } catch (e: Exception) {
                tvTime.text = "Effective: ${alert.effective} - Expires: ${alert.expires}"
            }
        }
    }
}
