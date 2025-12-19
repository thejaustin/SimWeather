package com.thejaustin.simweather.data.preferences

import android.content.Context
import android.content.SharedPreferences

class SettingsPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    enum class Units {
        METRIC, IMPERIAL
    }

    var units: Units
        get() {
            val value = prefs.getString(KEY_UNITS, Units.METRIC.name)
            return Units.valueOf(value ?: Units.METRIC.name)
        }
        set(value) {
            prefs.edit().putString(KEY_UNITS, value.name).apply()
        }

    var disastersEnabled: Boolean
        get() = prefs.getBoolean(KEY_DISASTERS, true)
        set(value) = prefs.edit().putBoolean(KEY_DISASTERS, value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var simulationSpeed: Int
        get() = prefs.getInt(KEY_SIM_SPEED, 50)
        set(value) = prefs.edit().putInt(KEY_SIM_SPEED, value).apply()

    companion object {
        private const val PREFS_NAME = "simweather_prefs"
        private const val KEY_UNITS = "units"
        private const val KEY_DISASTERS = "disasters"
        private const val KEY_SOUND = "sound"
        private const val KEY_SIM_SPEED = "sim_speed"

        @Volatile
        private var instance: SettingsPreferences? = null

        fun getInstance(context: Context): SettingsPreferences {
            return instance ?: synchronized(this) {
                instance ?: SettingsPreferences(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
