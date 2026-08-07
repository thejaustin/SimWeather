package com.thejaustin.simweather

data class AdvisorOpinion(
    val advisorName: String,
    val title: String,
    val advice: String,
    val mood: AdvisorMood,
)

enum class AdvisorMood {
    HAPPY,
    NEUTRAL,
    CONCERNED,
    URGENT,
}

object SimAdvisorManager {
    fun getFinancialAdvice(weather: CurrentWeather): AdvisorOpinion {
        val temp = weather.tempC
        return when {
            temp > 30 ->
                AdvisorOpinion(
                    advisorName = "Mortimer Green (Financial)",
                    title = "High Energy Consumption",
                    advice = "Power grid usage is spiking due to air conditioning! Municipal electricity bills may increase by 18%.",
                    mood = AdvisorMood.CONCERNED,
                )
            temp < 0 ->
                AdvisorOpinion(
                    advisorName = "Mortimer Green (Financial)",
                    title = "Heating Fuel Expenditure",
                    advice = "Freezing temperatures spike heating fuel demand. Consider subsidizing municipal heating plants.",
                    mood = AdvisorMood.CONCERNED,
                )
            else ->
                AdvisorOpinion(
                    advisorName = "Mortimer Green (Financial)",
                    title = "Stable Municipal Budget",
                    advice = "Mild weather conditions are reducing public utility strains. Treasury projections look healthy!",
                    mood = AdvisorMood.HAPPY,
                )
        }
    }

    fun getEnvironmentalAdvice(weather: CurrentWeather): AdvisorOpinion {
        val aqi = weather.airQuality?.usEpaIndex ?: 1
        val pollenCount = ((weather.pollen?.grassPollen ?: 0) + (weather.pollen?.treePollen ?: 0) + (weather.pollen?.weedPollen ?: 0))
        return when {
            aqi >= 4 ->
                AdvisorOpinion(
                    advisorName = "Karen Landers (Environment)",
                    title = "Hazardous Air Pollution",
                    advice = "Air quality index is elevated (Level $aqi). Recommend industrial emission caps until winds clear.",
                    mood = AdvisorMood.URGENT,
                )
            pollenCount > 5 ->
                AdvisorOpinion(
                    advisorName = "Karen Landers (Environment)",
                    title = "High Pollen Concentration",
                    advice = "Pollen levels are high. Plant urban tree barriers to filter airborne allergens.",
                    mood = AdvisorMood.CONCERNED,
                )
            else ->
                AdvisorOpinion(
                    advisorName = "Karen Landers (Environment)",
                    title = "Pristine Air Quality",
                    advice = "Ozone and particulate levels are low. Citizens are enjoying clean, crisp atmospheric conditions!",
                    mood = AdvisorMood.HAPPY,
                )
        }
    }

    fun getSafetyAdvice(response: WeatherResponse): AdvisorOpinion {
        val hasAlerts = response.alerts?.alert?.isNotEmpty() == true
        val windKph = response.current.windKph
        return when {
            hasAlerts -> {
                val topAlert = response.alerts!!.alert.first()
                AdvisorOpinion(
                    advisorName = "Maria Luna (Public Safety)",
                    title = topAlert.headline,
                    advice = topAlert.instruction.ifBlank { topAlert.desc },
                    mood = AdvisorMood.URGENT,
                )
            }
            windKph > 40 ->
                AdvisorOpinion(
                    advisorName = "Maria Luna (Public Safety)",
                    title = "High Wind Warning",
                    advice = "Gale forces recorded (${windKph.toInt()} kph). Secure loose structures and activate emergency crews.",
                    mood = AdvisorMood.CONCERNED,
                )
            else ->
                AdvisorOpinion(
                    advisorName = "Maria Luna (Public Safety)",
                    title = "All Systems Normal",
                    advice = "No imminent natural disasters detected in current weather radar sweeps.",
                    mood = AdvisorMood.HAPPY,
                )
        }
    }

    fun getTransportationAdvice(weather: CurrentWeather): AdvisorOpinion {
        val condition = weather.condition.text.lowercase()
        val precip = weather.precipMm
        return when {
            condition.contains("snow") || condition.contains("ice") ->
                AdvisorOpinion(
                    advisorName = "Moe Sillan (Transportation)",
                    title = "Road Icing & Delays",
                    advice = "Snow accumulation on highways! Deploy snowplows and salt spreaders to prevent traffic gridlock.",
                    mood = AdvisorMood.URGENT,
                )
            condition.contains("rain") && precip > 10.0 ->
                AdvisorOpinion(
                    advisorName = "Moe Sillan (Transportation)",
                    title = "Heavy Hydroplaning Hazard",
                    advice = "Rainfall exceeding $precip mm. Commuters experiencing delays; lower speed limits on expressways.",
                    mood = AdvisorMood.CONCERNED,
                )
            else ->
                AdvisorOpinion(
                    advisorName = "Moe Sillan (Transportation)",
                    title = "Optimal Road Conditions",
                    advice = "Traffic flowing smoothly across all city sectors. No weather-related transit delays reported.",
                    mood = AdvisorMood.HAPPY,
                )
        }
    }

    fun getHealthAdvice(weather: CurrentWeather): AdvisorOpinion {
        val uv = weather.uv
        val temp = weather.tempC
        return when {
            uv >= 8.0 ->
                AdvisorOpinion(
                    advisorName = "Dr. Constable (Public Health)",
                    title = "Extreme UV Warning",
                    advice = "UV Index is dangerous ($uv). Recommend high-factor sunblock and shade structures for public parks.",
                    mood = AdvisorMood.URGENT,
                )
            temp > 32 ->
                AdvisorOpinion(
                    advisorName = "Dr. Constable (Public Health)",
                    title = "Heat Stroke Advisory",
                    advice = "Temperatures exceeding $temp°C. Open public cooling centers and urge hydrational care.",
                    mood = AdvisorMood.CONCERNED,
                )
            else ->
                AdvisorOpinion(
                    advisorName = "Dr. Constable (Public Health)",
                    title = "Healthy Living Index",
                    advice = "Comfortable temperatures and moderate UV levels favor outdoor recreational activities.",
                    mood = AdvisorMood.HAPPY,
                )
        }
    }
}
