package com.thejaustin.simweather.data.simulation

import com.thejaustin.simweather.data.model.CurrentWeather
import com.thejaustin.simweather.data.preferences.SettingsPreferences

data class BudgetReport(
    val grossTaxIncome: Int,
    val totalOrdinanceCost: Int,
    val weatherEmergencyCost: Int,
    val netMonthlyCashflow: Int,
    val currentTreasury: Int,
)

class CityBudgetManager(private val settings: SettingsPreferences) {
    fun calculateMonthlyBudget(weather: CurrentWeather): BudgetReport {
        return calculate(
            taxRate = settings.taxRate,
            currentFunds = settings.funds,
            ordSmog = settings.ordinanceSmogScrubbers,
            ordSnow = settings.ordinanceSnowPlows,
            ordCooling = settings.ordinanceCoolingShelters,
            ordSunscreen = settings.ordinanceSunscreen,
            weather = weather,
        ).also { report ->
            settings.funds = report.currentTreasury
        }
    }

    companion object {
        fun calculate(
            taxRate: Int,
            currentFunds: Int,
            ordSmog: Boolean,
            ordSnow: Boolean,
            ordCooling: Boolean,
            ordSunscreen: Boolean,
            weather: CurrentWeather,
        ): BudgetReport {
            val baseMultiplier = 2500
            var weatherFactor = 1.0

            val temp = weather.tempC
            if (temp in 18.0..26.0) {
                weatherFactor += 0.25
            } else if (temp < 0.0 || temp > 35.0) {
                weatherFactor -= 0.30
            }

            val condText = weather.condition.text.lowercase()
            if (condText.contains("rain") || condText.contains("snow")) {
                weatherFactor -= 0.15
            }

            val grossTax = (taxRate * baseMultiplier * weatherFactor).toInt().coerceAtLeast(1000)

            var ordinanceCost = 0
            if (ordSmog) ordinanceCost += 500
            if (ordSnow) ordinanceCost += 600
            if (ordCooling) ordinanceCost += 400
            if (ordSunscreen) ordinanceCost += 300

            var emergencyCost = 0
            if (weather.windKph > 50.0) emergencyCost += 800
            if (condText.contains("thunder") || condText.contains("storm")) emergencyCost += 1200

            val netCashflow = grossTax - ordinanceCost - emergencyCost
            val updatedFunds = (currentFunds + netCashflow).coerceAtLeast(0)

            return BudgetReport(
                grossTaxIncome = grossTax,
                totalOrdinanceCost = ordinanceCost,
                weatherEmergencyCost = emergencyCost,
                netMonthlyCashflow = netCashflow,
                currentTreasury = updatedFunds,
            )
        }
    }
}
