package com.thejaustin.simweather.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thejaustin.simweather.R
import java.util.Collections

class CityPlanningAdapter(private val weatherCards: MutableList<String>) :
    RecyclerView.Adapter<CityPlanningAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city_planning, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCardName.text = weatherCards[position]
    }

    override fun getItemCount(): Int {
        return weatherCards.size
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(weatherCards, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getWeatherCards(): List<String> {
        return weatherCards
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCardName: TextView = itemView.findViewById(R.id.tvCardName)
    }
}
