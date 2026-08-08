package com.thejaustin.simweather

import kotlin.math.abs

/**
 * Calculates city livability, civic comfort analytics, barometric pressure trends,
 * dew point comfort classifications, and energy demand metrics based on live weather data.
 */
object WeatherAnalyticsManager {
    data class CityAnalytics(
        val comfortScorePercent: Int,
        val comfortRating: String,
        val energyDemandStatus: String,
        val pressureTrendStatus: String,
        val dewPointStatus: String,
    )

    fun calculateAnalytics(current: CurrentWeather): CityAnalytics {
        // Temperature penalty (ideal 22C / 72.5F)
        val tempDiff = abs(current.tempC - 22.0)
        val tempScore = (100.0 - (tempDiff * 3.0)).coerceIn(0.0, 100.0)

        // Humidity penalty (ideal 45%)
        val humDiff = abs(current.humidity - 45)
        val humScore = (100.0 - (humDiff * 0.8)).coerceIn(0.0, 100.0)

        // AQI penalty
        val aqiPenalty =
            when (current.airQuality?.usEpaIndex ?: 1) {
                1 -> 0.0
                2 -> 10.0
                3 -> 25.0
                4 -> 45.0
                else -> 70.0
            }

        // UV penalty
        val uvPenalty = if (current.uv > 7.0) (current.uv - 7.0) * 5.0 else 0.0

        val totalScore = ((tempScore * 0.5) + (humScore * 0.3) - aqiPenalty - uvPenalty).toInt().coerceIn(5, 100)

        val rating =
            when {
                totalScore >= 85 -> "IDEAL CIVIC COMFORT (85%+)"
                totalScore >= 70 -> "GOOD CIVIC COMFORT"
                totalScore >= 50 -> "MODERATE WEATHER STRESS"
                totalScore >= 30 -> "POOR CIVIC COMFORT"
                else -> "SEVERE ATMOSPHERIC ALERT"
            }

        val energy =
            when {
                current.tempC > 28.0 -> "HIGH COOLING DEMAND (AIR CONDITIONING)"
                current.tempC < 10.0 -> "HIGH HEATING DEMAND (MUNICIPAL POWER)"
                else -> "OPTIMAL ENERGY CONSUMPTION (LOW HVAC)"
            }

        val pressureStatus =
            when {
                current.pressureMb >= 1020.0 -> "HIGH PRESSURE (STABLE FAIR ATMOSPHERE)"
                current.pressureMb >= 1008.0 -> "STEADY PRESSURE (BALANCED AIR MASS)"
                else -> "LOW PRESSURE (STORM / CYCLONIC SYSTEM)"
            }

        // Dew Point approximation: Tdp = T - ((100 - RH) / 5)
        val dewPointC = current.tempC - ((100 - current.humidity) / 5.0)
        val dewPointStatus =
            when {
                dewPointC < 10.0 -> "DRY & CRISP (DEW POINT ${dewPointC.toInt()}°C)"
                dewPointC <= 16.0 -> "COMFORTABLE (DEW POINT ${dewPointC.toInt()}°C)"
                dewPointC <= 20.0 -> "HUMID & MUGGY (DEW POINT ${dewPointC.toInt()}°C)"
                else -> "OPPRESSIVE TROPICAL MOISTURE (${dewPointC.toInt()}°C)"
            }

        return CityAnalytics(totalScore, rating, energy, pressureStatus, dewPointStatus)
    }
}
