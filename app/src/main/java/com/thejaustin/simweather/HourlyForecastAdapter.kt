package com.thejaustin.simweather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HourlyForecastAdapter(
    private val settings: SettingsPreferences,
) : RecyclerView.Adapter<HourlyForecastAdapter.HourViewHolder>() {
    private var hourlyData: List<Hour> = emptyList()

    fun submitList(hours: List<Hour>) {
        hourlyData = hours
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HourViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HourViewHolder,
        position: Int,
    ) {
        holder.bind(hourlyData[position], settings.units)
    }

    override fun getItemCount() = hourlyData.size

    class HourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvHour)
        private val tvCondition: TextView = itemView.findViewById(R.id.tvConditionIcon)
        private val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        private val tvRain: TextView = itemView.findViewById(R.id.tvPrecipChance)

        fun bind(
            hour: Hour,
            units: SettingsPreferences.Units,
        ) {
            // Format time
            date.time = hour.timeEpoch * 1000
            tvTime.text = timeFormat.format(date)

            // Weather icon based on condition
            tvCondition.text = getWeatherEmoji(hour.condition.code, hour.isDay)

            // Temperature with unit conversion
            tvTemp.text = UnitConverter.temperature(hour.tempC, units)

            // Rain chance
            tvRain.text = "💧 ${hour.chanceOfRain}%"
        }

        private fun getWeatherEmoji(
            code: Int,
            isDay: Int,
        ): String {
            val dayIcon = if (isDay == 1) "☀️" else "🌙"
            return when (code) {
                0, 1000 -> dayIcon
                1, 2, 3, 1003, 1006, 1009 -> if (isDay == 1) "⛅" else "☁️"
                45, 48, 1135, 1147 -> "🌫️"
                51, 53, 55, 1063, 1150, 1153 -> "🌧️"
                61, 63, 65, 80, 81, 82, 1180, 1183, 1186, 1189, 1192, 1195, 1240, 1243 -> "🌧️"
                71, 73, 75, 85, 86, 1066, 1114, 1210, 1213, 1216, 1219, 1222, 1225 -> "❄️"
                95, 96, 99, 1087, 1273, 1276, 1279, 1282 -> "⛈️"
                else -> dayIcon
            }
        }
    }

    companion object {
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val date = Date()
    }
}
