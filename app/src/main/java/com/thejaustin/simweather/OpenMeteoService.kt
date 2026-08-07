package com.thejaustin.simweather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 100% Free Weather Provider utilizing the Open-Meteo REST API.
 * Requires NO API key and provides worldwide forecasts and live Air Quality (AQI) data.
 */
object OpenMeteoService {
    suspend fun fetchFreeForecast(
        cityName: String,
        lat: Double = 40.71,
        lon: Double = -74.00,
    ): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Fetch live weather forecast
                val weatherUrlString =
                    "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,relative_humidity_2m,is_day,precipitation,weather_code," +
                        "surface_pressure,wind_speed_10m,wind_direction_10m,cloud_cover,uv_index" +
                        "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code," +
                        "wind_speed_10m,surface_pressure,cloud_cover&daily=weather_code,temperature_2m_max," +
                        "temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum&timezone=auto"

                val weatherJson = fetchJsonObject(weatherUrlString)

                // 2. Fetch live Air Quality (AQI) data
                val aqiUrlString =
                    "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$lat&longitude=$lon" +
                        "&current=us_aqi,pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone"
                val aqiJson =
                    try {
                        fetchJsonObject(aqiUrlString)
                    } catch (e: Exception) {
                        null
                    }

                val currentObj = weatherJson.optJSONObject("current") ?: weatherJson.optJSONObject("current_weather")
                if (currentObj == null) {
                    return@withContext Result.failure(Exception("Invalid weather response from Open-Meteo"))
                }

                val tempC = currentObj.optDouble("temperature_2m", currentObj.optDouble("temperature", 20.0))
                val tempF = (tempC * 9 / 5) + 32
                val windKph = currentObj.optDouble("wind_speed_10m", currentObj.optDouble("windspeed", 10.0))
                val windDegree = currentObj.optInt("wind_direction_10m", currentObj.optInt("winddirection", 180))
                val weatherCode = currentObj.optInt("weather_code", currentObj.optInt("weathercode", 0))
                val isDay = currentObj.optInt("is_day", 1)
                val humidity = currentObj.optInt("relative_humidity_2m", 55)
                val pressureMb = currentObj.optDouble("surface_pressure", 1013.25)
                val precipMm = currentObj.optDouble("precipitation", 0.0)
                val cloudCover = currentObj.optInt("cloud_cover", 20)
                val uvIndex = currentObj.optDouble("uv_index", 3.0)

                val conditionText = mapWmoCode(weatherCode)

                // Parse AQI details if available
                val airQuality =
                    if (aqiJson != null && aqiJson.has("current")) {
                        val aqiCurrent = aqiJson.getJSONObject("current")
                        val usEpaIndex =
                            when (val usAqi = aqiCurrent.optInt("us_aqi", 50)) {
                                in 0..50 -> 1
                                in 51..100 -> 2
                                in 101..150 -> 3
                                in 151..200 -> 4
                                in 201..300 -> 5
                                else -> 6
                            }
                        AirQuality(
                            co = aqiCurrent.optDouble("carbon_monoxide", 200.0),
                            no2 = aqiCurrent.optDouble("nitrogen_dioxide", 10.0),
                            o3 = aqiCurrent.optDouble("ozone", 40.0),
                            so2 = aqiCurrent.optDouble("sulphur_dioxide", 5.0),
                            pm2_5 = aqiCurrent.optDouble("pm2_5", 12.0),
                            pm10 = aqiCurrent.optDouble("pm10", 20.0),
                            usEpaIndex = usEpaIndex,
                        )
                    } else {
                        AirQuality(200.0, 10.0, 40.0, 5.0, 12.0, 20.0, 1)
                    }

                val currentWeather =
                    CurrentWeather(
                        tempC = tempC,
                        tempF = tempF,
                        isDay = isDay,
                        condition = Condition(conditionText, "", weatherCode),
                        windKph = windKph,
                        windDegree = windDegree,
                        windDir = getWindDir(windDegree),
                        pressureMb = pressureMb,
                        precipMm = precipMm,
                        humidity = humidity,
                        cloud = cloudCover,
                        feelsLikeC = tempC,
                        feelsLikeF = tempF,
                        visibilityKm = 10.0,
                        uv = uvIndex,
                        airQuality = airQuality,
                        pollen = Pollen(2, 1, 1),
                    )

                val location = Location(cityName, "Open-Meteo Live API", "Global", lat, lon, "2026-08-07 12:00")

                val dailyArray = weatherJson.optJSONObject("daily")
                val hourlyArray = weatherJson.optJSONObject("hourly")
                val forecastDays = mutableListOf<ForecastDay>()

                if (dailyArray != null) {
                    val times = dailyArray.optJSONArray("time")
                    val maxTemps = dailyArray.optJSONArray("temperature_2m_max")
                    val minTemps = dailyArray.optJSONArray("temperature_2m_min")
                    val sunrises = dailyArray.optJSONArray("sunrise")
                    val sunsets = dailyArray.optJSONArray("sunset")
                    val uvs = dailyArray.optJSONArray("uv_index_max")
                    val codes = dailyArray.optJSONArray("weather_code") ?: dailyArray.optJSONArray("weathercode")
                    val precipSums = dailyArray.optJSONArray("precipitation_sum")

                    val count = times?.length() ?: 0
                    for (i in 0 until count) {
                        val dDate = times?.optString(i) ?: ""
                        val maxC = maxTemps?.optDouble(i, 20.0) ?: 20.0
                        val minC = minTemps?.optDouble(i, 10.0) ?: 10.0
                        val code = codes?.optInt(i, 0) ?: 0
                        val sRise = sunrises?.optString(i)?.takeLast(5) ?: "06:00"
                        val sSet = sunsets?.optString(i)?.takeLast(5) ?: "19:00"
                        val maxUv = uvs?.optDouble(i, 5.0) ?: 5.0
                        val dayPrecip = precipSums?.optDouble(i, 0.0) ?: 0.0

                        val dayObj =
                            Day(
                                maxTempC = maxC,
                                maxTempF = (maxC * 9 / 5) + 32,
                                minTempC = minC,
                                minTempF = (minC * 9 / 5) + 32,
                                avgTempC = (maxC + minC) / 2.0,
                                maxWindKph = 15.0,
                                totalPrecipMm = dayPrecip,
                                avgHumidity = 55,
                                dailyChanceOfRain = if (dayPrecip > 0) 70 else 10,
                                dailyChanceOfSnow = if (code in listOf(71, 73, 75)) 80 else 0,
                                condition = Condition(mapWmoCode(code), "", code),
                                uv = maxUv,
                            )

                        val astroObj = Astro(sRise, sSet, "20:00", "05:00", "Waxing Crescent", "45")

                        val hoursList = mutableListOf<Hour>()
                        if (hourlyArray != null) {
                            val hTimes = hourlyArray.optJSONArray("time")
                            val hTemps = hourlyArray.optJSONArray("temperature_2m")
                            val hCodes = hourlyArray.optJSONArray("weather_code")
                            val hWinds = hourlyArray.optJSONArray("wind_speed_10m")
                            val hPrecipProbs = hourlyArray.optJSONArray("precipitation_probability")

                            val startIndex = i * 24
                            for (h in 0..23) {
                                val idx = startIndex + h
                                if (idx < (hTimes?.length() ?: 0)) {
                                    val hTempC = hTemps?.optDouble(idx, minC) ?: minC
                                    val hCode = hCodes?.optInt(idx, code) ?: code
                                    val hWind = hWinds?.optDouble(idx, 10.0) ?: 10.0
                                    val hRainChance = hPrecipProbs?.optInt(idx, 10) ?: 10

                                    hoursList.add(
                                        Hour(
                                            timeEpoch = (System.currentTimeMillis() / 1000) + (h * 3600),
                                            time = String.format("%02d:00", h),
                                            tempC = hTempC,
                                            tempF = (hTempC * 9 / 5) + 32,
                                            isDay = if (h in 6..19) 1 else 0,
                                            condition = Condition(mapWmoCode(hCode), "", hCode),
                                            windKph = hWind,
                                            windDir = "N",
                                            pressureMb = 1013.0,
                                            precipMm = 0.0,
                                            humidity = 50,
                                            cloud = 20,
                                            feelsLikeC = hTempC,
                                            chanceOfRain = hRainChance,
                                            chanceOfSnow = 0,
                                        ),
                                    )
                                }
                            }
                        }

                        forecastDays.add(
                            ForecastDay(
                                date = dDate,
                                dateEpoch = System.currentTimeMillis() / 1000,
                                day = dayObj,
                                astro = astroObj,
                                hour = hoursList,
                            ),
                        )
                    }
                }

                Result.success(WeatherResponse(location, currentWeather, Forecast(forecastDays), null))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun fetchJsonObject(urlString: String): JSONObject {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        return try {
            if (connection.responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                JSONObject(jsonText)
            } else {
                throw Exception("HTTP ${connection.responseCode}")
            }
        } finally {
            connection.disconnect()
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
