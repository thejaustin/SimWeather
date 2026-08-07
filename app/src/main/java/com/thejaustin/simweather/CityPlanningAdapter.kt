package com.thejaustin.simweather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class CityPlanningAdapter(
    private val weatherCards: MutableList<String>,
    private val disabledCards: MutableSet<String>,
) : RecyclerView.Adapter<CityPlanningAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_city_planning, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val cardName = weatherCards[position]
        holder.tvCardName.text = cardName

        holder.switchVisibility.setOnCheckedChangeListener(null)
        holder.switchVisibility.isChecked = !disabledCards.contains(cardName)

        holder.switchVisibility.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                disabledCards.remove(cardName)
            } else {
                disabledCards.add(cardName)
            }
        }
    }

    override fun getItemCount(): Int = weatherCards.size

    fun onItemMove(
        fromPosition: Int,
        toPosition: Int,
    ) {
        Collections.swap(weatherCards, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getWeatherCards(): List<String> = weatherCards

    fun getDisabledCards(): Set<String> = disabledCards

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCardName: TextView = itemView.findViewById(R.id.tvCardName)
        val switchVisibility: SwitchCompat = itemView.findViewById(R.id.switchCardVisibility)
    }
}
