package com.thejaustin.simweather.data.simulation

import com.thejaustin.simweather.data.model.AirQuality
import com.thejaustin.simweather.data.model.Condition
import com.thejaustin.simweather.data.model.CurrentWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CityBudgetManagerTest {

    private fun createBaseWeather(
        tempC: Double = 22.0,
        windKph: Double = 10.0,
        conditionText: String = "Clear"
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
            precipMm = 0.0,
            humidity = 50,
            cloud = 0,
            feelsLikeC = tempC,
            feelsLikeF = (tempC * 9 / 5) + 32,
            visibilityKm = 10.0,
            uv = 3.0,
            airQuality = AirQuality(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1),
            pollen = null
        )
    }

    @Test
    fun testTaxCalculationInMildWeather() {
        val report = CityBudgetManager.calculate(
            taxRate = 10,
            currentFunds = 25000,
            ordSmog = false,
            ordSnow = false,
            ordCooling = false,
            ordSunscreen = false,
            weather = createBaseWeather(tempC = 22.0)
        )
        assertTrue(report.grossTaxIncome > 25000)
    }

    @Test
    fun testOrdinanceCosts() {
        val report = CityBudgetManager.calculate(
            taxRate = 7,
            currentFunds = 25000,
            ordSmog = true,
            ordSnow = true,
            ordCooling = false,
            ordSunscreen = false,
            weather = createBaseWeather()
        )
        assertEquals(1100, report.totalOrdinanceCost)
    }

    @Test
    fun testEmergencyCostsInStorm() {
        val report = CityBudgetManager.calculate(
            taxRate = 7,
            currentFunds = 25000,
            ordSmog = false,
            ordSnow = false,
            ordCooling = false,
            ordSunscreen = false,
            weather = createBaseWeather(windKph = 60.0, conditionText = "Thunderstorm")
        )
        assertEquals(2000, report.weatherEmergencyCost)
    }
}
