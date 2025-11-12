package com.thejaustin.simweather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thejaustin.simweather.ui.adapter.CityPlanningAdapter

class CityPlanningActivity : AppCompatActivity() {

    private lateinit var rvCityPlanning: RecyclerView
    private lateinit var adapter: CityPlanningAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_planning)

        rvCityPlanning = findViewById(R.id.rvCityPlanning)
        rvCityPlanning.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("SimWeather", MODE_PRIVATE)
        val layout = sharedPreferences.getString("layout", "Current Weather,Hourly Forecast,Daily Forecast,Weather Alerts,Clothing Advisor,Pollen")
        val weatherCards = layout?.split(",")?.toMutableList() ?: mutableListOf("Current Weather", "Hourly Forecast", "Daily Forecast", "Weather Alerts", "Clothing Advisor", "Pollen")

        adapter = CityPlanningAdapter(weatherCards)
        rvCityPlanning.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.onItemMove(fromPosition, toPosition)
                return true
            }

            override fun onPause() {
        super.onPause()
        val sharedPreferences = getSharedPreferences("SimWeather", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("layout", adapter.getWeatherCards().joinToString(","))
        editor.apply()
    }

