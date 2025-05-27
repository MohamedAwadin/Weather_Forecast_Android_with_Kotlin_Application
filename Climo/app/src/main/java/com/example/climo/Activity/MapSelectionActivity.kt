package com.example.climo.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.climo.R
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.FavoriteLocation
import com.example.climo.data.remote.RetrofitClient
import com.example.climo.databinding.ActivityMapSelectionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

class MapSelectionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapSelectionBinding
    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap
    private var selectedLatLng: LatLng? = null
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private var currentMarkerFeature: Feature? = null
    private var searchJob: Job? = null
    private var selectedLocationName: String? = null
    public lateinit var msg : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        binding = ActivityMapSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val copy_msg = intent.getStringExtra("from_where") ?: "home"
        msg = copy_msg.toString()

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        setupCitySearch()


        binding.saveButton.setOnClickListener {
            Log.d("MapSelectionActivity", "Save button clicked: $selectedLatLng, $selectedLocationName")
            selectedLatLng?.let { latLng ->
                val locationName = selectedLocationName ?: "Unknown Location"
                val intent = Intent().apply {
                    if (msg == "set" || msg == "home") {
                        putExtra("current_latitude", latLng.latitude.toFloat())
                        putExtra("current_longitude", latLng.longitude.toFloat())
                        putExtra("current_location_name", locationName)
                    } else if (msg == "fav") {
                        putExtra("favorite_latitude", latLng.latitude.toFloat())
                        putExtra("favorite_longitude", latLng.longitude.toFloat())
                        putExtra("favorite_location_name", locationName)
                    }
                }
                setResult(RESULT_OK, intent)
                finish()
            } ?: run {
                Toast.makeText(this, getString(R.string.please_select_location), Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map
        mapLibreMap.setStyle("https://api.maptiler.com/maps/streets/style.json?key=D3vSzuWOTthtSYqQ2hfH") { style ->
            if (style.getImage("marker-15") == null) {
                val drawable = resources.getDrawable(R.drawable.marker_15, null)
                style.addImage("marker-15", drawable)
            }

            style.addSource(
                GeoJsonSource(
                    "marker-source",
                    FeatureCollection.fromFeatures(emptyList())
                )
            )

            style.addLayer(
                SymbolLayer("marker-layer", "marker-source")
                    .withProperties(
                        PropertyFactory.iconImage("marker-15"),
                        PropertyFactory.iconSize(1.0f),
                        PropertyFactory.iconAllowOverlap(true)
                    )
            )

            mapLibreMap.addOnMapClickListener { point ->
                selectedLatLng = point
                updateMarker(point)
                fetchLocationName(point)
                true
            }

            mapLibreMap.cameraPosition = CameraPosition.Builder()
                .target(LatLng(0.0, 0.0))
                .zoom(1.0)
                .build()
        }
    }

    private fun updateMarker(latLng: LatLng) {
        mapLibreMap.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>("marker-source")
            source?.setGeoJson(
                FeatureCollection.fromFeature(
                    Feature.fromGeometry(Point.fromLngLat(latLng.longitude, latLng.latitude)).also {
                        currentMarkerFeature = it
                    }
                )
            )
        }
    }

    private fun clearMarkers() {
        mapLibreMap.getStyle { style ->
            val source = style.getSourceAs<GeoJsonSource>("marker-source")
            source?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
        }
    }

    private fun setupCitySearch() {
        binding.citySearchAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position) as String
            fetchCityCoordinates(selectedCity)
        }

        binding.citySearchAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length >= 3) {
                    searchJob?.cancel()
                    searchJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        fetchCitySuggestions(query)
                    }
                }
            }
        })
    }

    private fun fetchCitySuggestions(cityName: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getGeocoding(cityName, 5, "ecfe2681690524ece36e0e4818523e5f")
                }
                Log.d("MapSelectionActivity", "Geocoding response: $response")
                if (response.isNotEmpty()) {
                    val suggestions = response.map { it.name }
                    val adapter = ArrayAdapter( this@MapSelectionActivity,  android.R.layout.simple_dropdown_item_1line, suggestions)
                    binding.citySearchAutoComplete.setAdapter(adapter)
                    binding.citySearchAutoComplete.showDropDown()
                } else {
                    Toast.makeText(this@MapSelectionActivity, getString(R.string.no_cities_found, cityName), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MapSelectionActivity, getString(R.string.error_fetching_suggestions, e.message), Toast.LENGTH_SHORT).show()
                Log.e("MapSelectionActivity", "Error fetching suggestions", e)
            }
        }
    }

    private fun fetchCityCoordinates(cityName: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getGeocoding(cityName, 5, "ecfe2681690524ece36e0e4818523e5f")
                }
                Log.d("MapSelectionActivity", "Geocoding response: $response")
                if (response.isNotEmpty()) {
                    val city = response[0]
                    val latLng = LatLng(city.lat, city.lon)
                    selectedLatLng = latLng
                    selectedLocationName = city.name
                    clearMarkers()
                    updateMarker(latLng)
                    mapLibreMap.cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(10.0)
                        .build()



                    with(sharedPreferences.edit()) {
                        if (msg == "set" || msg == "home") {
                            putString("current_location_name", city.name)
                        } else if (msg == "fav") {
                            putString("favorite_location_name", city.name)
                        }
                        apply()


                    }

                    val suggestions = response.map { it.name }
                    val adapter = ArrayAdapter( this@MapSelectionActivity,  android.R.layout.simple_dropdown_item_1line, suggestions
                    )
                    binding.citySearchAutoComplete.setAdapter(adapter)
                    binding.citySearchAutoComplete.showDropDown()
                } else {
                    Toast.makeText(this@MapSelectionActivity, getString(R.string.no_cities_found, cityName), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MapSelectionActivity, getString(R.string.error_fetching_city, e.message), Toast.LENGTH_SHORT).show()
                Log.e("MapSelectionActivity", "Error fetching city", e)
            }
        }
    }

    private fun fetchLocationName(latLng: LatLng) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getReverseGeocoding(latLng.latitude, latLng.longitude, 1, "ecfe2681690524ece36e0e4818523e5f")
                }
                Log.d("MapSelectionActivity", "Reverse geocoding response: $response")
                selectedLocationName = if (response.isNotEmpty()) response[0].name else "Unknown Location"
                Toast.makeText(this@MapSelectionActivity, getString(R.string.selected_location, selectedLocationName), Toast.LENGTH_SHORT).show()

                with(sharedPreferences.edit()) {
                    if (msg == "set" || msg == "home") {
                        putString("current_location_name", selectedLocationName)
                    } else if (msg == "fav") {
                        putString("favorite_location_name", selectedLocationName)
                    }
                    apply()
                }
            } catch (e: Exception) {

                selectedLocationName = "Unknown Location"
                Toast.makeText(this@MapSelectionActivity, getString(R.string.error_fetching_location_name, e.message), Toast.LENGTH_SHORT).show()
                Log.e("MapSelectionActivity", "Error fetching location name", e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}