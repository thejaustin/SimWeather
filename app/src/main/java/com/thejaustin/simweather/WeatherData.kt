package com.thejaustin.simweather

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val location: Location,
    val current: CurrentWeather,
    val forecast: Forecast,
    val alerts: Alerts?,
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    @SerializedName("localtime")
    val localTime: String,
)

data class CurrentWeather(
    @SerializedName("temp_c")
    val tempC: Double,
    @SerializedName("temp_f")
    val tempF: Double,
    @SerializedName("is_day")
    val isDay: Int,
    val condition: Condition,
    @SerializedName("wind_kph")
    val windKph: Double,
    @SerializedName("wind_degree")
    val windDegree: Int,
    @SerializedName("wind_dir")
    val windDir: String,
    @SerializedName("pressure_mb")
    val pressureMb: Double,
    @SerializedName("precip_mm")
    val precipMm: Double,
    val humidity: Int,
    val cloud: Int,
    @SerializedName("feelslike_c")
    val feelsLikeC: Double,
    @SerializedName("feelslike_f")
    val feelsLikeF: Double,
    @SerializedName("vis_km")
    val visibilityKm: Double,
    val uv: Double,
    @SerializedName("air_quality")
    val airQuality: AirQuality?,
    val pollen: Pollen?,
)

data class AirQuality(
    val co: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    @SerializedName("pm2_5")
    val pm2_5: Double,
    val pm10: Double,
    @SerializedName("us-epa-index")
    val usEpaIndex: Int,
)

data class Pollen(
    @SerializedName("grass_pollen")
    val grassPollen: Int,
    @SerializedName("tree_pollen")
    val treePollen: Int,
    @SerializedName("weed_pollen")
    val weedPollen: Int,
)

data class Condition(
    val text: String,
    val icon: String,
    val code: Int,
)

data class Forecast(
    @SerializedName("forecastday")
    val forecastDays: List<ForecastDay>,
)

data class ForecastDay(
    val date: String,
    @SerializedName("date_epoch")
    val dateEpoch: Long,
    val day: Day,
    val astro: Astro,
    val hour: List<Hour>,
)

data class Day(
    @SerializedName("maxtemp_c")
    val maxTempC: Double,
    @SerializedName("maxtemp_f")
    val maxTempF: Double,
    @SerializedName("mintemp_c")
    val minTempC: Double,
    @SerializedName("mintemp_f")
    val minTempF: Double,
    @SerializedName("avgtemp_c")
    val avgTempC: Double,
    @SerializedName("maxwind_kph")
    val maxWindKph: Double,
    @SerializedName("totalprecip_mm")
    val totalPrecipMm: Double,
    @SerializedName("avghumidity")
    val avgHumidity: Int,
    @SerializedName("daily_chance_of_rain")
    val dailyChanceOfRain: Int,
    @SerializedName("daily_chance_of_snow")
    val dailyChanceOfSnow: Int,
    val condition: Condition,
    val uv: Double,
)

data class Hour(
    @SerializedName("time_epoch")
    val timeEpoch: Long,
    val time: String,
    @SerializedName("temp_c")
    val tempC: Double,
    @SerializedName("temp_f")
    val tempF: Double,
    @SerializedName("is_day")
    val isDay: Int,
    val condition: Condition,
    @SerializedName("wind_kph")
    val windKph: Double,
    @SerializedName("wind_dir")
    val windDir: String,
    @SerializedName("pressure_mb")
    val pressureMb: Double,
    @SerializedName("precip_mm")
    val precipMm: Double,
    val humidity: Int,
    val cloud: Int,
    @SerializedName("feelslike_c")
    val feelsLikeC: Double,
    @SerializedName("chance_of_rain")
    val chanceOfRain: Int,
    @SerializedName("chance_of_snow")
    val chanceOfSnow: Int,
)

data class Astro(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    @SerializedName("moon_phase")
    val moonPhase: String,
    @SerializedName("moon_illumination")
    val moonIllumination: String,
)

data class Alerts(
    val alert: List<Alert>,
)

data class Alert(
    val headline: String,
    @SerializedName("msgtype")
    val msgType: String,
    val severity: String,
    val urgency: String,
    val areas: String,
    val category: String,
    val certainty: String,
    val event: String,
    val note: String,
    val effective: String,
    val expires: String,
    val desc: String,
    val instruction: String,
)
