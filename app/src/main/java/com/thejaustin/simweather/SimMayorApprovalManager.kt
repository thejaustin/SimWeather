package com.thejaustin.simweather

import kotlin.math.abs

/**
 * Calculates the Mayor's Civic Approval Rating (0-100%) in real-time
 * based on weather severity, tax rate, treasury balance, and air pollution.
 */
object SimMayorApprovalManager {
    data class ApprovalStatus(
        val ratingPercent: Int,
        val title: String,
        val emoji: String,
        val summary: String,
    )

    fun calculateApproval(
        current: CurrentWeather,
        taxRate: Int,
        treasuryBalance: Int,
    ): ApprovalStatus {
        var baseScore = 75.0

        // Tax rate impact (7% is ideal)
        val taxDiff = taxRate - 7
        if (taxDiff > 0) {
            baseScore -= taxDiff * 4.0
        } else if (taxDiff < 0) {
            baseScore += abs(taxDiff) * 1.5
        }

        // Treasury health impact
        if (treasuryBalance < 5000) {
            baseScore -= 15.0
        } else if (treasuryBalance > 30000) {
            baseScore += 10.0
        }

        // Weather comfort impact (ideal 18C - 26C)
        if (current.tempC < 5.0 || current.tempC > 32.0) {
            baseScore -= 10.0
        }

        // Air Quality impact
        val aqiIndex = current.airQuality?.usEpaIndex ?: 1
        if (aqiIndex >= 4) {
            baseScore -= 20.0
        } else if (aqiIndex == 1) {
            baseScore += 5.0
        }

        val rating = baseScore.toInt().coerceIn(10, 100)

        val (title, emoji, summary) =
            when {
                rating >= 85 -> Triple("BELOVED MAYOR", "😊", "Citizens are thriving! High satisfaction across all zones.")
                rating >= 70 -> Triple("STABLE LEADERSHIP", "🙂", "Public approval is solid. Municipal services running smoothly.")
                rating >= 50 -> Triple("DIVIDED CITY", "😐", "Civic complaints are rising due to weather and economic factors.")
                rating >= 30 -> Triple("PUBLIC DISCONTENT", "😟", "Citizens demand lower taxes and better weather mitigation!")
                else -> Triple("IMPEACHMENT THREAT", "😡", "Severe public anger! Immediate emergency budget action required!")
            }

        return ApprovalStatus(rating, title, emoji, summary)
    }
}
