package com.thejaustin.simweather


class WeatherRepository {
    private val api = RetrofitInstance.weatherApi

    suspend fun getWeatherForecast(
        apiKey: String,
        location: String,
    ): Result<WeatherResponse> {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE" || apiKey == "simweather_free") {
            return OpenMeteoService.fetchFreeForecast(location)
        }

        return try {
            val response = api.getForecast(apiKey, location, days = 7, aqi = "yes", pollen = "yes", alerts = "yes")
            Result.success(response)
        } catch (e: Exception) {
            OpenMeteoService.fetchFreeForecast(location)
        }
    }
}
