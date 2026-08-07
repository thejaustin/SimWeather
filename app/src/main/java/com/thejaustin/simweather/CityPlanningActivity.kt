package com.thejaustin.simweather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CityPlanningActivity : AppCompatActivity() {
    private lateinit var rvCityPlanning: RecyclerView
    private lateinit var adapter: CityPlanningAdapter

    private val defaultCards =
        listOf(
            "Current Weather",
            "Hourly Forecast",
            "Daily Forecast",
            "Astronomy & Moon",
            "Wind & Pressure",
            "Precipitation & Dew Point",
            "UV & Solar Safety",
            "Air Quality (AQI)",
            "Weather Alerts",
            "Clothing Advisor",
            "Pollen",
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_planning)

        rvCityPlanning = findViewById(R.id.rvCityPlanning)
        rvCityPlanning.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("SimWeather", MODE_PRIVATE)
        val layout = sharedPreferences.getString("layout", defaultCards.joinToString(","))
        val weatherCards = layout?.split(",")?.toMutableList() ?: defaultCards.toMutableList()

        adapter = CityPlanningAdapter(weatherCards)
        rvCityPlanning.adapter = adapter

        val itemTouchHelper =
            ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                    0,
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder,
                    ): Boolean {
                        val fromPosition = viewHolder.adapterPosition
                        val toPosition = target.adapterPosition
                        adapter.onItemMove(fromPosition, toPosition)
                        return true
                    }

                    override fun onSwiped(
                        viewHolder: RecyclerView.ViewHolder,
                        direction: Int,
                    ) {
                    }
                },
            )

        itemTouchHelper.attachToRecyclerView(rvCityPlanning)
    }

    override fun onPause() {
        super.onPause()
        val sharedPreferences = getSharedPreferences("SimWeather", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("layout", adapter.getWeatherCards().joinToString(","))
        editor.apply()
    }
}
