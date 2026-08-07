package com.thejaustin.simweather.data.simulation

import com.thejaustin.simweather.data.model.Condition
import com.thejaustin.simweather.data.model.CurrentWeather
import com.thejaustin.simweather.data.model.Forecast
import com.thejaustin.simweather.data.model.Location
import com.thejaustin.simweather.data.model.WeatherResponse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherSimulatorTest {
    @Test
    fun testSimulateNextProducesValidWeather() {
        val simulator = WeatherSimulator()
        val initial =
            WeatherResponse(
                location = Location("SimCity", "State", "Country", 0.0, 0.0, "2025-11-12 10:00"),
                current =
                    CurrentWeather(
                        20.0, 68.0, 1, Condition("Sunny", "", 1000), 10.0, 180, "S",
                        1013.0, 0.0, 50, 0, 20.0, 68.0, 10.0, 5.0, null, null,
                    ),
                forecast = Forecast(emptyList()),
                alerts = null,
            )

        val nextState = simulator.simulateNext(initial)
        assertNotNull(nextState)
        assertNotNull(nextState.location)
        assertNotNull(nextState.current)
        assertTrue(nextState.current.tempC >= -10.0 && nextState.current.tempC <= 50.0)
    }
}
