package com.thejaustin.simweather

import java.util.concurrent.ConcurrentHashMap

class WeatherRepository {
    private val api = RetrofitInstance.weatherApi

    private data class CachedForecast(
        val response: WeatherResponse,
        val timestamp: Long,
    )

    private val cache = ConcurrentHashMap<String, CachedForecast>()
    private val cacheTtlMs = 10 * 60 * 1000L // 10 minutes

    suspend fun getWeatherForecast(
        apiKey: String,
        location: String,
        forceRefresh: Boolean = false,
    ): Result<WeatherResponse> {
        val cacheKey = "${apiKey.trim()}_${location.trim().lowercase()}"

        if (!forceRefresh) {
            val cached = cache[cacheKey]
            if (cached != null && (System.currentTimeMillis() - cached.timestamp) < cacheTtlMs) {
                return Result.success(cached.response)
            }
        }

        val result =
            if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE" || apiKey == "simweather_free") {
                OpenMeteoService.fetchFreeForecast(location)
            } else {
                try {
                    val response = api.getForecast(apiKey, location, days = 7, aqi = "yes", pollen = "yes", alerts = "yes")
                    Result.success(response)
                } catch (e: Exception) {
                    OpenMeteoService.fetchFreeForecast(location)
                }
            }

        result.getOrNull()?.let { response ->
            cache[cacheKey] = CachedForecast(response, System.currentTimeMillis())
        }

        return result
    }

    fun clearCache() {
        cache.clear()
    }
}
