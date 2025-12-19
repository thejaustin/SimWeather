package com.thejaustin.simweather.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import com.thejaustin.simweather.R
import com.thejaustin.simweather.data.preferences.SettingsPreferences

class SettingsDialog(
    context: Context,
    private val onSettingsChanged: () -> Unit
) : Dialog(context) {

    private lateinit var rgUnits: RadioGroup
    private lateinit var rbMetric: RadioButton
    private lateinit var rbImperial: RadioButton
    private lateinit var swDisasters: Switch
    private lateinit var swSound: Switch
    private lateinit var sbSimSpeed: SeekBar
    private lateinit var btnCancel: Button
    private lateinit var btnApply: Button

    private val settings = SettingsPreferences.getInstance(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_settings)

        window?.setBackgroundDrawableResource(android.R.color.transparent)

        initViews()
        loadCurrentSettings()
        setupListeners()
    }

    private fun initViews() {
        rgUnits = findViewById(R.id.rgUnits)
        rbMetric = findViewById(R.id.rbMetric)
        rbImperial = findViewById(R.id.rbImperial)
        swDisasters = findViewById(R.id.swDisasters)
        swSound = findViewById(R.id.swSound)
        sbSimSpeed = findViewById(R.id.sbSimSpeed)
        btnCancel = findViewById(R.id.btnCancel)
        btnApply = findViewById(R.id.btnApply)
    }

    private fun loadCurrentSettings() {
        when (settings.units) {
            SettingsPreferences.Units.METRIC -> rbMetric.isChecked = true
            SettingsPreferences.Units.IMPERIAL -> rbImperial.isChecked = true
        }
        swDisasters.isChecked = settings.disastersEnabled
        swSound.isChecked = settings.soundEnabled
        sbSimSpeed.progress = settings.simulationSpeed
    }

    private fun setupListeners() {
        btnCancel.setOnClickListener {
            dismiss()
        }

        btnApply.setOnClickListener {
            saveSettings()
            onSettingsChanged()
            dismiss()
        }
    }

    private fun saveSettings() {
        settings.units = when (rgUnits.checkedRadioButtonId) {
            R.id.rbImperial -> SettingsPreferences.Units.IMPERIAL
            else -> SettingsPreferences.Units.METRIC
        }
        settings.disastersEnabled = swDisasters.isChecked
        settings.soundEnabled = swSound.isChecked
        settings.simulationSpeed = sbSimSpeed.progress
    }
}
