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

    companion object {
        private const val PREFS_NAME = "simweather_prefs"
        private const val KEY_UNITS = "units"

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
