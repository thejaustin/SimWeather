package com.thejaustin.simweather


object ClothingAdvisor {
    fun getClothingAdvice(weather: CurrentWeather): String {
        val temp = weather.tempC
        val condition = weather.condition.text.lowercase()

        return when {
            temp > 25 -> "It's hot! Wear light clothes."
            temp > 15 -> "It's a pleasant day. A t-shirt and jeans should be fine."
            temp > 5 -> "It's a bit chilly. A jacket or sweater is recommended."
            else -> "It's cold! Bundle up with a warm coat, scarf, and gloves."
        }
    }
}
