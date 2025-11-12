package com.thejaustin.simweather.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thejaustin.simweather.R
import com.thejaustin.simweather.data.model.ForecastDay
import java.text.SimpleDateFormat
import java.util.*

class DailyForecastAdapter : RecyclerView.Adapter<DailyForecastAdapter.DayViewHolder>() {

    private var dailyData: List<ForecastDay> = emptyList()

    fun submitList(days: List<ForecastDay>) {
        dailyData = days
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_forecast, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(dailyData[position], position)
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

        fun bind(forecastDay: ForecastDay, position: Int) {
            val day = forecastDay.day

            // Day name
            val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val date = Date(forecastDay.dateEpoch * 1000)
            tvDayName.text = if (position == 0) {
                "TODAY"
            } else {
                dateFormat.format(date).uppercase()
            }

            // Condition
            tvCondition.text = day.condition.text

            // Weather icon
            tvIcon.text = getWeatherEmoji(day.condition.code)

            // Temperatures
            tvHigh.text = "↑ ${day.maxTempC.toInt()}°"
            tvLow.text = "↓ ${day.minTempC.toInt()}°"

            // Rain chance
            tvRain.text = "💧 ${day.dailyChanceOfRain}%"

            // Wind
            tvWind.text = "💨 ${day.maxWindKph.toInt()}kph"
        }

        private fun getWeatherEmoji(code: Int): String {
            return when {
                code == 1000 -> "☀" // Clear
                code in 1003..1009 -> "☁" // Cloudy
                code in 1063..1072 || code in 1150..1201 -> "🌧" // Rain
                code in 1210..1225 || code in 1237..1264 -> "❄" // Snow
                code in 1273..1282 -> "⛈" // Thunderstorm
                code in 1135..1147 -> "🌫" // Fog
                else -> "☀"
            }
        }
    }
}
