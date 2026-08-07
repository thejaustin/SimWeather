package com.thejaustin.simweather.ui.util

import com.thejaustin.simweather.data.model.AirQuality
import com.thejaustin.simweather.data.model.Alert
import com.thejaustin.simweather.data.model.Alerts
import com.thejaustin.simweather.data.model.Condition
import com.thejaustin.simweather.data.model.CurrentWeather
import com.thejaustin.simweather.data.model.Forecast
import com.thejaustin.simweather.data.model.Location
import com.thejaustin.simweather.data.model.Pollen
import com.thejaustin.simweather.data.model.WeatherResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimAdvisorManagerTest {
    private fun createBaseWeather(
        tempC: Double = 20.0,
        windKph: Double = 10.0,
        conditionText: String = "Clear",
        precipMm: Double = 0.0,
        uv: Double = 3.0,
        aqi: Int = 1,
        pollen: Int = 1,
    ): CurrentWeather {
        return CurrentWeather(
            tempC = tempC,
            tempF = (tempC * 9 / 5) + 32,
            isDay = 1,
            condition = Condition(conditionText, "", 1000),
            windKph = windKph,
            windDegree = 180,
            windDir = "S",
            pressureMb = 1013.0,
            precipMm = precipMm,
            humidity = 50,
            cloud = 0,
            feelsLikeC = tempC,
            feelsLikeF = (tempC * 9 / 5) + 32,
            visibilityKm = 10.0,
            uv = uv,
            airQuality = AirQuality(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, aqi),
            pollen = Pollen(pollen, pollen, pollen),
        )
    }

    @Test
    fun testFinancialAdviceHighTemp() {
        val weather = createBaseWeather(tempC = 35.0)
        val advice = SimAdvisorManager.getFinancialAdvice(weather)
        assertEquals("Mortimer Green (Financial)", advice.advisorName)
        assertEquals(AdvisorMood.CONCERNED, advice.mood)
        assertTrue(advice.advice.contains("air conditioning"))
    }

    @Test
    fun testFinancialAdviceFreezingTemp() {
        val weather = createBaseWeather(tempC = -5.0)
        val advice = SimAdvisorManager.getFinancialAdvice(weather)
        assertEquals(AdvisorMood.CONCERNED, advice.mood)
        assertTrue(advice.advice.contains("heating fuel"))
    }

    @Test
    fun testEnvironmentalAdviceBadAqi() {
        val weather = createBaseWeather(aqi = 5)
        val advice = SimAdvisorManager.getEnvironmentalAdvice(weather)
        assertEquals("Karen Landers (Environment)", advice.advisorName)
        assertEquals(AdvisorMood.URGENT, advice.mood)
        assertTrue(advice.advice.contains("Level 5"))
    }

    @Test
    fun testSafetyAdviceAlerts() {
        val weather = createBaseWeather()
        val response =
            WeatherResponse(
                location = Location("SimCity", "State", "Country", 0.0, 0.0, ""),
                current = weather,
                forecast = Forecast(emptyList()),
                alerts =
                    Alerts(
                        listOf(
                            Alert(
                                "Tornado Warning", "Severe", "Immediate", "Expected", "City", "Met", "Observed",
                                "Tornado", "Take shelter", "", "", "Tornado spotted", "Take shelter immediately",
                            ),
                        ),
                    ),
            )
        val advice = SimAdvisorManager.getSafetyAdvice(response)
        assertEquals("Maria Luna (Public Safety)", advice.advisorName)
        assertEquals(AdvisorMood.URGENT, advice.mood)
        assertEquals("Tornado Warning", advice.title)
    }

    @Test
    fun testTransportationAdviceSnow() {
        val weather = createBaseWeather(conditionText = "Heavy Snow")
        val advice = SimAdvisorManager.getTransportationAdvice(weather)
        assertEquals("Moe Sillan (Transportation)", advice.advisorName)
        assertEquals(AdvisorMood.URGENT, advice.mood)
        assertTrue(advice.advice.contains("snowplows"))
    }

    @Test
    fun testHealthAdviceHighUv() {
        val weather = createBaseWeather(uv = 9.0)
        val advice = SimAdvisorManager.getHealthAdvice(weather)
        assertEquals("Dr. Constable (Public Health)", advice.advisorName)
        assertEquals(AdvisorMood.URGENT, advice.mood)
        assertTrue(advice.advice.contains("sunblock"))
    }
}
