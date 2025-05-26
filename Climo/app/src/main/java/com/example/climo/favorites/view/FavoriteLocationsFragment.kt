package com.example.climo.favorites.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.climo.Activity.MapSelectionActivity
import com.example.climo.R
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.FavoriteLocation
import com.example.climo.databinding.FragmentFavoriteLocationsBinding
import com.example.climo.favorites.viewmodel.FavoriteLocationsViewModel
import com.example.climo.favorites.viewmodel.FavoriteLocationsViewModelFactory
import com.google.android.material.snackbar.Snackbar


class   FavoriteLocationsFragment : Fragment() {

    private var _binding : FragmentFavoriteLocationsBinding?= null
    private val binding get() = _binding!!
    private val viewModel : FavoriteLocationsViewModel by viewModels {
        FavoriteLocationsViewModelFactory(ClimoDatabase.getDatabase(requireContext()))
    }
    private lateinit var adapter: FavoriteLocationsAdapter

//    private val mapSelectionLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            Log.d("FavoriteLocationsFragment", "mapSelectionLauncher result: ${result.resultCode}, ${result.data}")
//            if (result.resultCode == android.app.Activity.RESULT_OK){
//                val data = result.data
//                val latitude = data?.getFloatExtra("favorite_latitude", 0f)?.toDouble() ?: 0.0
//                val longitude = data?.getFloatExtra("favorite_latitude", 0f)?.toDouble() ?: 0.0
//                val cityName = data?.getStringExtra("favorite_location_name") ?: "Unknown Location"
//                viewModel.addFavoriteLocation(latitude , longitude , cityName)
//            }
//        }
private val mapSelectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    Log.d("FavoriteLocationsFragment", "mapSelectionLauncher result: ${result.resultCode}, ${result.data}")
    if (result.resultCode == android.app.Activity.RESULT_OK) {
        result.data?.let { data ->
            val latitude = data.getFloatExtra("favorite_latitude", 0f).toDouble()
            val longitude = data.getFloatExtra("favorite_longitude", 0f).toDouble()
            val locationName = data.getStringExtra("favorite_location_name") ?: getString(R.string.unknown)
            if (latitude != 0.0 && longitude != 0.0) {
                viewModel.addFavoriteLocation(latitude, longitude, locationName)
                Toast.makeText(context, getString(R.string.location_added, locationName), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, getString(R.string.please_select_location), Toast.LENGTH_SHORT).show()
            }
        }
    }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteLocationsBinding.inflate(inflater, container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavoriteLocationsAdapter(
            onItemClick = {location ->
                val action = FavoriteLocationsFragmentDirections.actionFavoriteLocationsFragmentToFavoriteDetailsFragment(
                    latitude = location.latitude.toFloat(),
                    longitude = location.longitude.toFloat(),
                    locationName = location.cityName
                )
                findNavController().navigate(action)
            },
            onDeleteClick = {location ->
                viewModel.deleteFavoriteLocation(location)
                Snackbar.make(
                    binding.root,
                    getString(R.string.location_removed , location.cityName),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.undo)) {
                    viewModel.addFavoriteLocation(location.latitude , location.longitude , location.cityName)
                }.show()

            }
        )
        binding.favoriteLocationsRecyclerView.adapter = adapter
//
//        viewModel.favoriteLocations.observe(viewLifecycleOwner) { locations ->
//            adapter.submitList(locations)
//            binding.emptyStateText.visibility = if (locations.isEmpty()) View.VISIBLE else View.GONE
//        }
//
//        binding.addLocationFab.setOnClickListener {
//            val intent = Intent(requireContext() , MapSelectionActivity::class.java)
//            intent.putExtra("from_where" , "fav")
//            mapSelectionLauncher.launch(intent)
//        }

        viewModel.favoriteLocations.observe(viewLifecycleOwner) { locations ->
            Log.d("FavoriteLocationsFragment", "Favorite locations updated: $locations")
            binding.emptyStateText.visibility = if (locations.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(locations)
        }

        binding.addLocationFab.setOnClickListener {
            Log.d("FavoriteLocationsFragment", "FAB clicked")
            val intent = Intent(requireContext(), MapSelectionActivity::class.java)
            intent.putExtra("from_where", "fav")
            mapSelectionLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}