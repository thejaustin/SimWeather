package com.thejaustin.simweather.data.api

import com.thejaustin.simweather.data.model.AirQuality
import com.thejaustin.simweather.data.model.Astro
import com.thejaustin.simweather.data.model.Condition
import com.thejaustin.simweather.data.model.CurrentWeather
import com.thejaustin.simweather.data.model.Day
import com.thejaustin.simweather.data.model.Forecast
import com.thejaustin.simweather.data.model.ForecastDay
import com.thejaustin.simweather.data.model.Hour
import com.thejaustin.simweather.data.model.Location
import com.thejaustin.simweather.data.model.Pollen
import com.thejaustin.simweather.data.model.WeatherResponse
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 100% Free Weather Provider utilizing the Open-Meteo REST API.
 * Requires NO API key and provides worldwide forecasts.
 */
object OpenMeteoService {

    suspend fun fetchFreeForecast(cityName: String, lat: Double = 40.71, lon: Double = -74.00): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current_weather=true&hourly=temperature_2m,relativehumidity_2m," +
                    "precipitation_probability,weathercode,windspeed_10m&daily=weathercode," +
                    "temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max&timezone=auto"

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000

                if (connection.responseCode == 200) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonText)

                    val cw = json.getJSONObject("current_weather")
                    val tempC = cw.getDouble("temperature")
                    val tempF = (tempC * 9 / 5) + 32
                    val windKph = cw.getDouble("windspeed")
                    val windDegree = cw.optInt("winddirection", 180)
                    val weatherCode = cw.getInt("weathercode")
                    val isDay = cw.optInt("is_day", 1)

                    val conditionText = mapWmoCode(weatherCode)

                    val currentWeather = CurrentWeather(
                        tempC = tempC,
                        tempF = tempF,
                        isDay = isDay,
                        condition = Condition(conditionText, "", weatherCode),
                        windKph = windKph,
                        windDegree = windDegree,
                        windDir = getWindDir(windDegree),
                        pressureMb = 1013.2,
                        precipMm = 0.0,
                        humidity = 55,
                        cloud = 20,
                        feelsLikeC = tempC,
                        feelsLikeF = tempF,
                        visibilityKm = 10.0,
                        uv = 5.0,
                        airQuality = AirQuality(12.0, 15.0, 45.0, 2.0, 8.5, 14.2, 1),
                        pollen = Pollen(2, 1, 1)
                    )

                    val location = Location(cityName, "Open-Meteo Free API", "Global", lat, lon, "2026-08-07 12:00")

                    val dailyArray = json.optJSONObject("daily")
                    val forecastDays = mutableListOf<ForecastDay>()

                    if (dailyArray != null) {
                        val times = dailyArray.optJSONArray("time")
                        val maxTemps = dailyArray.optJSONArray("temperature_2m_max")
                        val minTemps = dailyArray.optJSONArray("temperature_2m_min")
                        val sunrises = dailyArray.optJSONArray("sunrise")
                        val sunsets = dailyArray.optJSONArray("sunset")
                        val uvs = dailyArray.optJSONArray("uv_index_max")
                        val codes = dailyArray.optJSONArray("weathercode")

                        val count = times?.length() ?: 0
                        for (i in 0 until count) {
                            val dDate = times?.optString(i) ?: ""
                            val maxC = maxTemps?.optDouble(i, 20.0) ?: 20.0
                            val minC = minTemps?.optDouble(i, 10.0) ?: 10.0
                            val code = codes?.optInt(i, 1000) ?: 1000
                            val sRise = sunrises?.optString(i)?.takeLast(5) ?: "06:00"
                            val sSet = sunsets?.optString(i)?.takeLast(5) ?: "19:00"
                            val maxUv = uvs?.optDouble(i, 5.0) ?: 5.0

                            val dayObj = Day(
                                maxTempC = maxC,
                                maxTempF = (maxC * 9 / 5) + 32,
                                minTempC = minC,
                                minTempF = (minC * 9 / 5) + 32,
                                avgTempC = (maxC + minC) / 2.0,
                                maxWindKph = 15.0,
                                totalPrecipMm = 0.0,
                                avgHumidity = 55,
                                dailyChanceOfRain = 20,
                                dailyChanceOfSnow = 0,
                                condition = Condition(mapWmoCode(code), "", code),
                                uv = maxUv
                            )

                            val astroObj = Astro(sRise, sSet, "20:00", "05:00", "Waxing Crescent", "45")

                            val hoursList = mutableListOf<Hour>()
                            for (h in 0..23) {
                                val hTemp = minC + (maxC - minC) * (h / 24.0)
                                hoursList.add(
                                    Hour(
                                        timeEpoch = System.currentTimeMillis() / 1000 + (h * 3600),
                                        time = String.format("%02d:00", h),
                                        tempC = hTemp,
                                        tempF = (hTemp * 9 / 5) + 32,
                                        isDay = if (h in 6..19) 1 else 0,
                                        condition = Condition(mapWmoCode(code), "", code),
                                        windKph = 10.0,
                                        windDir = "N",
                                        pressureMb = 1013.0,
                                        precipMm = 0.0,
                                        humidity = 50,
                                        cloud = 20,
                                        feelsLikeC = hTemp,
                                        chanceOfRain = 10,
                                        chanceOfSnow = 0
                                    )
                                )
                            }

                            forecastDays.add(
                                ForecastDay(
                                    date = dDate,
                                    dateEpoch = System.currentTimeMillis() / 1000,
                                    day = dayObj,
                                    astro = astroObj,
                                    hour = hoursList
                                )
                            )
                        }
                    }

                    Result.success(WeatherResponse(location, currentWeather, Forecast(forecastDays), null))
                } else {
                    Result.failure(Exception("Open-Meteo HTTP ${connection.responseCode}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
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
