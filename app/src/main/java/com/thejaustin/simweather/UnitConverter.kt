package com.thejaustin.simweather

object UnitConverter {
    fun temperature(
        celsius: Double,
        units: SettingsPreferences.Units,
    ): String {
        return when (units) {
            SettingsPreferences.Units.METRIC -> "${celsius.toInt()}°C"
            SettingsPreferences.Units.IMPERIAL -> {
                val fahrenheit = (celsius * 9 / 5) + 32
                "${fahrenheit.toInt()}°F"
            }
        }
    }

    fun speed(
        kph: Double,
        units: SettingsPreferences.Units,
    ): String {
        return when (units) {
            SettingsPreferences.Units.METRIC -> "${kph.toInt()} kph"
            SettingsPreferences.Units.IMPERIAL -> {
                val mph = kph * 0.621371
                "${mph.toInt()} mph"
            }
        }
    }

    fun distance(
        km: Double,
        units: SettingsPreferences.Units,
    ): String {
        return when (units) {
            SettingsPreferences.Units.METRIC -> "${km.toInt()} km"
            SettingsPreferences.Units.IMPERIAL -> {
                val miles = km * 0.621371
                "${miles.toInt()} mi"
            }
        }
    }

    fun pressure(
        mb: Double,
        units: SettingsPreferences.Units,
    ): String {
        return when (units) {
            SettingsPreferences.Units.METRIC -> "${mb.toInt()} mb"
            SettingsPreferences.Units.IMPERIAL -> {
                val inHg = mb * 0.02953
                String.format("%.2f inHg", inHg)
            }
        }
    }

    fun precipitation(
        mm: Double,
        units: SettingsPreferences.Units,
    ): String {
        return when (units) {
            SettingsPreferences.Units.METRIC -> "$mm mm"
            SettingsPreferences.Units.IMPERIAL -> {
                val inches = mm * 0.0393701
                String.format("%.2f in", inches)
            }
        }
    }
}
