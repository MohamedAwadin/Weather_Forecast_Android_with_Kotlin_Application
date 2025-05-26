package com.example.climo.favorites.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.climo.data.model.FavoriteLocation
import com.example.climo.databinding.ActivityMainBinding
import com.example.climo.databinding.ItemFavoriteLocationBinding

class FavoriteLocationsAdapter(
    private val onItemClick: (FavoriteLocation) -> Unit,
    private val onDeleteClick: (FavoriteLocation) -> Unit
) : ListAdapter<FavoriteLocation, FavoriteLocationsAdapter.ViewHolder>(FavoriteLocationDiffCallBack()) {

    inner class ViewHolder(private val binding: ItemFavoriteLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(location: FavoriteLocation){
            binding.locationName.text = location.cityName
            binding.root.setOnClickListener { onItemClick(location) }
            binding.deleteButton.setOnClickListener { onDeleteClick(location) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemFavoriteLocationBinding.inflate(LayoutInflater.from(parent.context) ,parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FavoriteLocationsAdapter.ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class FavoriteLocationDiffCallBack : DiffUtil.ItemCallback<FavoriteLocation>(){
        override fun areItemsTheSame(
            oldItem: FavoriteLocation,
            newItem: FavoriteLocation
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: FavoriteLocation,
            newItem: FavoriteLocation
        ): Boolean {
            return oldItem.id == newItem.id
        }

    }


}