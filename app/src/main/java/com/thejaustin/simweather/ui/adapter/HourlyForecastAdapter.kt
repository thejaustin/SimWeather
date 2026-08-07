package com.thejaustin.simweather.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thejaustin.simweather.R
import com.thejaustin.simweather.data.model.Hour
import com.thejaustin.simweather.data.preferences.SettingsPreferences
import com.thejaustin.simweather.ui.util.UnitConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HourlyForecastAdapter(
    private val settings: SettingsPreferences
) : RecyclerView.Adapter<HourlyForecastAdapter.HourViewHolder>() {

    private var hourlyData: List<Hour> = emptyList()

    fun submitList(hours: List<Hour>) {
        hourlyData = hours
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        holder.bind(hourlyData[position], settings.units)
    }

    override fun getItemCount() = hourlyData.size

    class HourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvHour)
        private val tvCondition: TextView = itemView.findViewById(R.id.tvConditionIcon)
        private val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        private val tvRain: TextView = itemView.findViewById(R.id.tvPrecipChance)

        fun bind(hour: Hour, units: SettingsPreferences.Units) {
            // Format time
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(hour.timeEpoch * 1000)
            tvTime.text = timeFormat.format(date)

            // Weather icon based on condition
            tvCondition.text = getWeatherEmoji(hour.condition.code, hour.isDay)

            // Temperature with unit conversion
            tvTemp.text = UnitConverter.temperature(hour.tempC, units)

            // Rain chance
            tvRain.text = "💧 ${hour.chanceOfRain}%"
        }

        private fun getWeatherEmoji(code: Int, isDay: Int): String {
            return when {
                code == 1000 -> if (isDay == 1) "☀" else "🌙"
                code in 1003..1009 -> "☁"
                code in 1063..1072 || code in 1150..1201 -> "🌧"
                code in 1210..1225 || code in 1237..1264 -> "❄"
                code in 1273..1282 -> "⛈"
                code in 1135..1147 -> "🌫"
                else -> if (isDay == 1) "☀" else "🌙"
            }
        }
    }
}
