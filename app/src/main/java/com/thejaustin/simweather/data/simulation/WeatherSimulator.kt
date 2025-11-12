package com.thejaustin.simweather.data.simulation

import com.thejaustin.simweather.data.model.*

/**
 * A class to simulate weather changes over time.
 *
 * This is a placeholder for a more complex weather simulation model.
 * For now, it will provide a simple mechanism to transition between weather states.
 */
class WeatherSimulator {

    private val sunnyWeather = WeatherResponse(
        location = Location("SimCity", "California", "USA", 34.05, -118.24, "2025-11-12 10:00"),
        current = CurrentWeather(25.0, 77.0, 1, Condition("Sunny", "//cdn.weatherapi.com/weather/64x64/day/113.png", 1000), 5.0, 180, "S", 1012.0, 0.0, 40, 0, 26.0, 79.0, 10.0, 8.0, null, null),
        forecast = Forecast(listOf()),
        alerts = null
    )

    private val rainyWeather = WeatherResponse(
        location = Location("SimCity", "California", "USA", 34.05, -118.24, "2025-11-12 14:00"),
        current = CurrentWeather(18.0, 64.0, 1, Condition("Light rain", "//cdn.weatherapi.com/weather/64x64/day/296.png", 1183), 15.0, 220, "SW", 1008.0, 2.5, 80, 75, 17.0, 63.0, 5.0, 4.0, null, null),
        forecast = Forecast(listOf()),
        alerts = Alerts(listOf(Alert("Flood warning", "Minor", "Moderate", "Expected", "Low-lying areas", "Met", "Likely", "Heavy rain", "Stay indoors", "2025-11-12 14:00", "2025-11-12 18:00", "Heavy rain expected to cause minor flooding.", "Avoid low-lying areas and stay indoors if possible.")))
    )

    private val snowyWeather = WeatherResponse(
        location = Location("SimCity", "California", "USA", 34.05, -118.24, "2025-11-12 22:00"),
        current = CurrentWeather(-2.0, 28.0, 0, Condition("Heavy snow", "//cdn.weatherapi.com/weather/64x64/night/338.png", 1225), 25.0, 300, "WNW", 1002.0, 10.0, 95, 100, -5.0, 23.0, 1.0, 1.0, null, null),
        forecast = Forecast(listOf()),
        alerts = Alerts(listOf(Alert("Blizzard warning", "Severe", "High", "Immediate", "Entire region", "Met", "Observed", "Blizzard", "Seek shelter immediately", "2025-11-12 22:00", "2025-11-13 06:00", "Blizzard conditions with heavy snow and strong winds.", "Seek shelter immediately. Avoid travel.")))
    )

    private val weatherStates = listOf(sunnyWeather, rainyWeather, snowyWeather)

    /**
     * Simulates the next weather state based on the current weather.
     *
     * @param currentWeather The current weather data.
     * @return The simulated next weather data.
     */
    fun simulateNext(currentWeather: WeatherResponse): WeatherResponse {
        return weatherStates.random()
    }
}
