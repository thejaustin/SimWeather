package com.thejaustin.simweather

import kotlin.random.Random

/**
 * Procedural offline weather simulation engine.
 * Generates realistic current conditions, 7-day daily forecasts, 24-hour hourly forecasts,
 * live AQI, pollen metrics, and sun/moon astronomy data when offline.
 */
class WeatherSimulator {
    fun generateSimulatedForecast(cityName: String): WeatherResponse {
        val name = cityName.ifBlank { "SimCity" }
        val location = Location(name, "Atmospheric Simulation", "Simulated Region", 34.05, -118.24, "2026-08-08 12:00")

        val baseTempC = Random.nextDouble(15.0, 28.0)
        val tempC = (baseTempC * 10).toInt() / 10.0
        val tempF = ((tempC * 9 / 5) + 32 * 10).toInt() / 10.0
        val humidity = Random.nextInt(40, 80)
        val windKph = Random.nextDouble(5.0, 35.0)
        val windDegree = Random.nextInt(0, 360)
        val pressureMb = Random.nextDouble(1005.0, 1022.0)
        val cloud = Random.nextInt(10, 85)
        val uv = Random.nextDouble(1.0, 9.0)
        val precipMm = if (humidity > 65) Random.nextDouble(0.5, 8.0) else 0.0

        val weatherCode =
            when {
                precipMm > 4.0 -> 63 // Rain
                precipMm > 0.0 -> 51 // Drizzle
                cloud > 60 -> 3 // Partly cloudy
                else -> 0 // Clear
            }

        val conditionText = mapWmoCode(weatherCode)

        val airQuality =
            AirQuality(
                co = 180.0 + Random.nextDouble(0.0, 50.0),
                no2 = 10.0 + Random.nextDouble(0.0, 15.0),
                o3 = 35.0 + Random.nextDouble(0.0, 25.0),
                so2 = 4.0 + Random.nextDouble(0.0, 5.0),
                pm2_5 = 8.0 + Random.nextDouble(0.0, 15.0),
                pm10 = 15.0 + Random.nextDouble(0.0, 20.0),
                usEpaIndex = Random.nextInt(1, 3),
            )

        val pollen = Pollen(Random.nextInt(1, 4), Random.nextInt(1, 3), Random.nextInt(1, 3))

        val currentWeather =
            CurrentWeather(
                tempC = tempC,
                tempF = tempF,
                isDay = 1,
                condition = Condition(conditionText, "", weatherCode),
                windKph = windKph,
                windDegree = windDegree,
                windDir = getWindDir(windDegree),
                pressureMb = pressureMb,
                precipMm = precipMm,
                humidity = humidity,
                cloud = cloud,
                feelsLikeC = tempC + 1.2,
                feelsLikeF = tempF + 2.0,
                visibilityKm = 10.0,
                uv = uv,
                airQuality = airQuality,
                pollen = pollen,
            )

        // 7-day forecast generation
        val forecastDays = mutableListOf<ForecastDay>()
        val currentEpoch = System.currentTimeMillis() / 1000

        for (i in 0..6) {
            val maxC = tempC + Random.nextDouble(-3.0, 5.0)
            val minC = tempC - Random.nextDouble(4.0, 10.0)
            val dayPrecip = if (Random.nextBoolean()) Random.nextDouble(0.0, 5.0) else 0.0
            val dayCode = if (dayPrecip > 2.0) 61 else 1

            val dayObj =
                Day(
                    maxTempC = maxC,
                    maxTempF = (maxC * 9 / 5) + 32,
                    minTempC = minC,
                    minTempF = (minC * 9 / 5) + 32,
                    avgTempC = (maxC + minC) / 2.0,
                    maxWindKph = Random.nextDouble(10.0, 25.0),
                    totalPrecipMm = dayPrecip,
                    avgHumidity = Random.nextInt(45, 75),
                    dailyChanceOfRain = if (dayPrecip > 0) 65 else 15,
                    dailyChanceOfSnow = 0,
                    condition = Condition(mapWmoCode(dayCode), "", dayCode),
                    uv = Random.nextDouble(3.0, 8.0),
                )

            val astroObj = Astro("06:15 AM", "07:45 PM", "09:30 PM", "05:15 AM", "Waxing Crescent", "62")

            val hoursList = mutableListOf<Hour>()
            for (h in 0..23) {
                val hTemp = minC + (maxC - minC) * (h / 24.0)
                hoursList.add(
                    Hour(
                        timeEpoch = currentEpoch + (i * 86400) + (h * 3600),
                        time = String.format("%02d:00", h),
                        tempC = hTemp,
                        tempF = (hTemp * 9 / 5) + 32,
                        isDay = if (h in 6..19) 1 else 0,
                        condition = Condition(mapWmoCode(dayCode), "", dayCode),
                        windKph = Random.nextDouble(8.0, 18.0),
                        windDir = "NW",
                        pressureMb = pressureMb,
                        precipMm = if (dayPrecip > 0 && h in 12..16) 1.0 else 0.0,
                        humidity = Random.nextInt(40, 70),
                        cloud = Random.nextInt(10, 50),
                        feelsLikeC = hTemp,
                        chanceOfRain = if (dayPrecip > 0) 60 else 10,
                        chanceOfSnow = 0,
                    ),
                )
            }

            forecastDays.add(
                ForecastDay(
                    date = "Day ${i + 1}",
                    dateEpoch = currentEpoch + (i * 86400),
                    day = dayObj,
                    astro = astroObj,
                    hour = hoursList,
                ),
            )
        }

        return WeatherResponse(location, currentWeather, Forecast(forecastDays), null)
    }

    fun simulateNext(currentWeather: WeatherResponse): WeatherResponse {
        return generateSimulatedForecast(currentWeather.location.name)
    }

    private fun mapWmoCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Sunny"
        }
    }

    private fun getWindDir(degree: Int): String {
        val dirs = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val idx = ((degree + 22.5) / 45).toInt() % 8
        return dirs[idx]
    }
}
