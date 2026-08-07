package com.thejaustin.simweather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyForecastAdapter(
    private val settings: SettingsPreferences,
) : RecyclerView.Adapter<DailyForecastAdapter.DayViewHolder>() {
    private var dailyData: List<ForecastDay> = emptyList()

    fun submitList(days: List<ForecastDay>) {
        dailyData = days
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DayViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_daily_forecast, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DayViewHolder,
        position: Int,
    ) {
        holder.bind(dailyData[position], position, settings.units)
    }

    override fun getItemCount() = dailyData.size

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
        private val tvCondition: TextView = itemView.findViewById(R.id.tvDayCondition)
        private val tvIcon: TextView = itemView.findViewById(R.id.tvDayIcon)
        private val tvHigh: TextView = itemView.findViewById(R.id.tvDayHigh)
        private val tvLow: TextView = itemView.findViewById(R.id.tvDayLow)
        private val tvRain: TextView = itemView.findViewById(R.id.tvDayRain)
        private val tvWind: TextView = itemView.findViewById(R.id.tvDayWind)

        fun bind(
            forecastDay: ForecastDay,
            position: Int,
            units: SettingsPreferences.Units,
        ) {
            val day = forecastDay.day

            // Day name
            date.time = forecastDay.dateEpoch * 1000
            tvDayName.text =
                if (position == 0) {
                    "TODAY"
                } else {
                    dateFormat.format(date).uppercase()
                }

            // Condition
            tvCondition.text = day.condition.text

            // Weather icon
            tvIcon.text = getWeatherEmoji(day.condition.code)

            // Temperatures with unit conversion
            tvHigh.text = "↑ ${UnitConverter.temperature(day.maxTempC, units)}"
            tvLow.text = "↓ ${UnitConverter.temperature(day.minTempC, units)}"

            // Rain chance
            tvRain.text = "💧 ${day.dailyChanceOfRain}%"

            // Wind with unit conversion
            tvWind.text = "💨 ${UnitConverter.speed(day.maxWindKph, units)}"
        }

        private fun getWeatherEmoji(code: Int): String {
            return when (code) {
                0, 1000 -> "☀️"
                1, 2, 3, 1003, 1006, 1009 -> "⛅"
                45, 48, 1135, 1147 -> "🌫️"
                51, 53, 55, 1063, 1150, 1153 -> "🌧️"
                61, 63, 65, 80, 81, 82, 1180, 1183, 1186, 1189, 1192, 1195, 1240, 1243 -> "🌧️"
                71, 73, 75, 85, 86, 1066, 1114, 1210, 1213, 1216, 1219, 1222, 1225 -> "❄️"
                95, 96, 99, 1087, 1273, 1276, 1279, 1282 -> "⛈️"
                else -> "☀️"
            }
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        private val date = Date()
    }
}
