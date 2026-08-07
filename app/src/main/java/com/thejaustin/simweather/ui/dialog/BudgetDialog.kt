package com.thejaustin.simweather.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.thejaustin.simweather.R
import com.thejaustin.simweather.data.preferences.SettingsPreferences
import com.thejaustin.simweather.ui.util.SoundManager

class BudgetDialog(
    context: Context,
    private val onBudgetUpdated: () -> Unit,
) : Dialog(context) {
    private lateinit var tvTreasuryHeader: TextView
    private lateinit var tvTaxLabel: TextView
    private lateinit var sbTaxRate: SeekBar
    private lateinit var swOrdSmog: Switch
    private lateinit var swOrdSnow: Switch
    private lateinit var swOrdCooling: Switch
    private lateinit var swOrdSunscreen: Switch
    private lateinit var btnClose: Button
    private lateinit var btnApply: Button

    private val settings = SettingsPreferences.getInstance(context)
    private val soundManager = SoundManager.getInstance(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_budget)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        tvTreasuryHeader = findViewById(R.id.tvTreasuryHeader)
        tvTaxLabel = findViewById(R.id.tvTaxLabel)
        sbTaxRate = findViewById(R.id.sbTaxRate)
        swOrdSmog = findViewById(R.id.swOrdSmog)
        swOrdSnow = findViewById(R.id.swOrdSnow)
        swOrdCooling = findViewById(R.id.swOrdCooling)
        swOrdSunscreen = findViewById(R.id.swOrdSunscreen)
        btnClose = findViewById(R.id.btnBudgetClose)
        btnApply = findViewById(R.id.btnBudgetApply)

        loadCurrentValues()
        setupListeners()
    }

    private fun loadCurrentValues() {
        tvTreasuryHeader.text = "Treasury Balance: §${settings.funds}"
        sbTaxRate.progress = settings.taxRate
        tvTaxLabel.text = "Municipal Tax Rate: ${settings.taxRate}%"

        swOrdSmog.isChecked = settings.ordinanceSmogScrubbers
        swOrdSnow.isChecked = settings.ordinanceSnowPlows
        swOrdCooling.isChecked = settings.ordinanceCoolingShelters
        swOrdSunscreen.isChecked = settings.ordinanceSunscreen
    }

    private fun setupListeners() {
        sbTaxRate.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    val rate = progress.coerceAtLeast(1)
                    tvTaxLabel.text = "Municipal Tax Rate: $rate%"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            },
        )

        btnClose.setOnClickListener {
            soundManager.playClick()
            dismiss()
        }

        btnApply.setOnClickListener {
            soundManager.playCashRegister()
            settings.taxRate = sbTaxRate.progress.coerceAtLeast(1)
            settings.ordinanceSmogScrubbers = swOrdSmog.isChecked
            settings.ordinanceSnowPlows = swOrdSnow.isChecked
            settings.ordinanceCoolingShelters = swOrdCooling.isChecked
            settings.ordinanceSunscreen = swOrdSunscreen.isChecked

            onBudgetUpdated()
            dismiss()
        }
    }
}
