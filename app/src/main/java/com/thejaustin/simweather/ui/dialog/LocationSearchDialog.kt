package com.thejaustin.simweather.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import com.thejaustin.simweather.R
import com.thejaustin.simweather.ui.util.SoundManager

class LocationSearchDialog(
    context: Context,
    private val onLocationSelected: (String) -> Unit,
) : Dialog(context) {
    private lateinit var etCitySearch: EditText
    private lateinit var btnCancel: Button
    private lateinit var btnSubmit: Button
    private val soundManager = SoundManager.getInstance(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_location_search)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        etCitySearch = findViewById(R.id.etCitySearch)
        btnCancel = findViewById(R.id.btnSearchCancel)
        btnSubmit = findViewById(R.id.btnSearchSubmit)

        btnCancel.setOnClickListener {
            soundManager.playClick()
            dismiss()
        }

        btnSubmit.setOnClickListener {
            soundManager.playClick()
            val query = etCitySearch.text.toString().trim()
            if (query.isNotEmpty()) {
                onLocationSelected(query)
                dismiss()
            }
        }

        // Preset button listeners
        val presets =
            mapOf(
                R.id.btnCityTokyo to "Tokyo",
                R.id.btnCityLondon to "London",
                R.id.btnCityNY to "New York",
                R.id.btnCityParis to "Paris",
                R.id.btnCitySydney to "Sydney",
                R.id.btnCitySimCity to "SimCity",
            )

        for ((id, cityName) in presets) {
            findViewById<Button>(id)?.setOnClickListener {
                soundManager.playClick()
                onLocationSelected(cityName)
                dismiss()
            }
        }
    }
}
