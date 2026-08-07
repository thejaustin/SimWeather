package com.thejaustin.simweather

import kotlin.random.Random

/**
 * A class to simulate weather changes over time.
 */
class WeatherSimulator {
    private val sunnyWeather =
        WeatherResponse(
            location = Location("SimCity", "California", "USA", 34.05, -118.24, "2025-11-12 10:00"),
            current =
                CurrentWeather(
                    25.0, 77.0, 1, Condition("Sunny", "//cdn.weatherapi.com/weather/64x64/day/113.png", 1000),
                    5.0, 180, "S", 1012.0, 0.0, 40, 0, 26.0, 79.0, 10.0, 8.0, null, null,
                ),
            forecast = Forecast(listOf()),
            alerts = null,
        )

    private val rainyWeather =
        WeatherResponse(
            location = Location("SimCity", "California", "USA", 34.05, -118.24, "2025-11-12 14:00"),
            current =
                CurrentWeather(
                    18.0, 64.0, 1, Condition("Light rain", "//cdn.weatherapi.com/weather/64x64/day/296.png", 1183),
                    15.0, 220, "SW", 1008.0, 2.5, 80, 75, 17.0, 63.0, 5.0, 4.0, null, null,
                ),
            forecast = Forecast(listOf()),
            alerts =
                Alerts(
                    listOf(
                        Alert(
                            "Flood warning", "Minor", "Moderate", "Expected", "Low-lying areas", "Met",
                            "Likely", "Heavy rain", "Stay indoors", "2025-11-12 14:00", "2025-11-12 18:00",
                            "Heavy rain expected to cause minor flooding.", "Avoid low-lying areas and stay indoors if possible.",
                        ),
                    ),
                ),
        )

    private val snowyWeather =
        WeatherResponse(
            location = Location("SimCity", "California", "USA", 34.05, -118.24, "2025-11-12 22:00"),
            current =
                CurrentWeather(
                    -2.0, 28.0, 0, Condition("Heavy snow", "//cdn.weatherapi.com/weather/64x64/night/338.png", 1225),
                    25.0, 300, "WNW", 1002.0, 10.0, 95, 100, -5.0, 23.0, 1.0, 1.0, null, null,
                ),
            forecast = Forecast(listOf()),
            alerts =
                Alerts(
                    listOf(
                        Alert(
                            "Blizzard warning", "Severe", "High", "Immediate", "Entire region", "Met",
                            "Observed", "Blizzard", "Seek shelter immediately", "2025-11-12 22:00", "2025-11-13 06:00",
                            "Blizzard conditions with heavy snow and strong winds.", "Seek shelter immediately. Avoid travel.",
                        ),
                    ),
                ),
        )

    private val foggyWeather =
        WeatherResponse(
            location = Location("SimCity", "California", "USA", 34.05, -118.24, "2025-11-13 06:00"),
            current =
                CurrentWeather(
                    10.0, 50.0, 0, Condition("Fog", "//cdn.weatherapi.com/weather/64x64/day/248.png", 1135),
                    2.0, 0, "N", 1015.0, 0.0, 98, 0, 10.0, 50.0, 0.2, 0.1, null, null,
                ),
            forecast = Forecast(listOf()),
            alerts =
                Alerts(
                    listOf(
                        Alert(
                            "Dense Fog Advisory", "Moderate", "Moderate", "Expected", "Highways", "Met",
                            "Likely", "Dense Fog", "Drive with caution", "2025-11-13 06:00", "2025-11-13 10:00",
                            "Visibility less than 1/4 mile in dense fog.", "Slow down, use your headlights, and leave plenty of distance.",
                        ),
                    ),
                ),
        )

    private val windyWeather =
        WeatherResponse(
            location = Location("SimCity", "California", "USA", 34.05, -118.24, "2025-11-13 15:00"),
            current =
                CurrentWeather(
                    20.0, 68.0, 1, Condition("Windy", "//cdn.weatherapi.com/weather/64x64/day/113.png", 1000),
                    45.0, 270, "W", 1005.0, 0.0, 30, 0, 18.0, 64.0, 10.0, 10.0, null, null,
                ),
            forecast = Forecast(listOf()),
            alerts =
                Alerts(
                    listOf(
                        Alert(
                            "High Wind Warning", "Severe", "High", "Expected", "Coastal Areas", "Met",
                            "Likely", "High Winds", "Secure loose objects", "2025-11-13 15:00", "2025-11-13 22:00",
                            "Northwest winds 30 to 45 mph with gusts up to 60 mph.", "Damaging winds will blow down trees and power lines.",
                        ),
                    ),
                ),
        )

    private val weatherStates = listOf(sunnyWeather, rainyWeather, snowyWeather, foggyWeather, windyWeather)

    fun simulateNext(currentWeather: WeatherResponse): WeatherResponse {
        val nextState = weatherStates.random()
        val tempVariation = Random.nextDouble(-2.0, 2.0)
        val newTempC = nextState.current.tempC + tempVariation
        val newTempF = (newTempC * 9 / 5) + 32
        val roundedTempC = (newTempC * 10).toInt() / 10.0
        val roundedTempF = (newTempF * 10).toInt() / 10.0
        val modifiedCurrent =
            nextState.current.copy(
                tempC = roundedTempC,
                tempF = roundedTempF,
            )
        return nextState.copy(current = modifiedCurrent)
    }
}
