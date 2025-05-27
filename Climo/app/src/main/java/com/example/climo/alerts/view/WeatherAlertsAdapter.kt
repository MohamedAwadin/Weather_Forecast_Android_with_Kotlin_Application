package com.example.climo.alerts.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.climo.data.model.WeatherAlert
import com.example.climo.databinding.ItemWeatherAlertBinding

class WeatherAlertsAdapter(
    private val onDeleteClick: (WeatherAlert) -> Unit
) : ListAdapter<WeatherAlert, WeatherAlertsAdapter.WeatherAlertViewHolder>(WeatherAlertDiffCallback()) {
    inner class WeatherAlertViewHolder(private val binding: ItemWeatherAlertBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alert: WeatherAlert){
            binding.cityName.text = alert.cityName
            binding.dateTimeRange.text = "${alert.fromDateTime} to ${alert.toDateTime}"
            binding.alertType.text = alert.alertType
            binding.deleteButton.setOnClickListener { onDeleteClick(alert) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WeatherAlertViewHolder {
        val binding = ItemWeatherAlertBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return WeatherAlertViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: WeatherAlertsAdapter.WeatherAlertViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class WeatherAlertDiffCallback : DiffUtil.ItemCallback<WeatherAlert>(){
        override fun areItemsTheSame(
            oldItem: WeatherAlert,
            newItem: WeatherAlert
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: WeatherAlert,
            newItem: WeatherAlert
        ): Boolean {
            return oldItem.id == newItem.id
        }

    }
}