package com.thejaustin.simweather.data.repository

import com.thejaustin.simweather.data.api.RetrofitInstance
import com.thejaustin.simweather.data.model.WeatherResponse

class WeatherRepository {

    private val api = RetrofitInstance.weatherApi

    suspend fun getWeatherForecast(apiKey: String, location: String): Result<WeatherResponse> {
        return try {
            val response = api.getForecast(apiKey, location, days = 7, aqi = "yes", pollen = "yes", alerts = "yes")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
