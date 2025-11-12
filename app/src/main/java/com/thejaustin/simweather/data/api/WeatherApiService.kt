package com.thejaustin.simweather.data.api

import com.thejaustin.simweather.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7,
        @Query("aqi") aqi: String = "no",
        @Query("alerts") alerts: String = "yes"
    ): WeatherResponse

    companion object {
        const val BASE_URL = "https://api.weatherapi.com/"
    }
}
