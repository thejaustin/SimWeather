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

    var gameHudEnabled: Boolean
        get() = prefs.getBoolean(KEY_GAME_HUD, true)
        set(value) = prefs.edit().putBoolean(KEY_GAME_HUD, value).apply()

    var showRealTime: Boolean
        get() = prefs.getBoolean(KEY_REAL_TIME, false)
        set(value) = prefs.edit().putBoolean(KEY_REAL_TIME, value).apply()

    var funds: Int
        get() = prefs.getInt(KEY_FUNDS, 25000)
        set(value) = prefs.edit().putInt(KEY_FUNDS, value.coerceAtLeast(0)).apply()

    var taxRate: Int
        get() = prefs.getInt(KEY_TAX_RATE, 7)
        set(value) = prefs.edit().putInt(KEY_TAX_RATE, value.coerceIn(1, 20)).apply()

    var ordinanceSmogScrubbers: Boolean
        get() = prefs.getBoolean(KEY_ORD_SMOG, false)
        set(value) = prefs.edit().putBoolean(KEY_ORD_SMOG, value).apply()

    var ordinanceSnowPlows: Boolean
        get() = prefs.getBoolean(KEY_ORD_SNOW, true)
        set(value) = prefs.edit().putBoolean(KEY_ORD_SNOW, value).apply()

    var ordinanceCoolingShelters: Boolean
        get() = prefs.getBoolean(KEY_ORD_COOLING, false)
        set(value) = prefs.edit().putBoolean(KEY_ORD_COOLING, value).apply()

    var ordinanceSunscreen: Boolean
        get() = prefs.getBoolean(KEY_ORD_SUNSCREEN, false)
        set(value) = prefs.edit().putBoolean(KEY_ORD_SUNSCREEN, value).apply()

    companion object {
        private const val PREFS_NAME = "simweather_prefs"
        private const val KEY_UNITS = "units"
        private const val KEY_DISASTERS = "disasters"
        private const val KEY_SOUND = "sound"
        private const val KEY_SIM_SPEED = "sim_speed"
        private const val KEY_GAME_HUD = "game_hud"
        private const val KEY_REAL_TIME = "real_time"
        private const val KEY_FUNDS = "city_funds"
        private const val KEY_TAX_RATE = "tax_rate"
        private const val KEY_ORD_SMOG = "ord_smog"
        private const val KEY_ORD_SNOW = "ord_snow"
        private const val KEY_ORD_COOLING = "ord_cooling"
        private const val KEY_ORD_SUNSCREEN = "ord_sunscreen"

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
