package com.example.climo.settings.view

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.room.util.query
import com.example.climo.Activity.MapSelectionActivity

import com.example.climo.R
import com.example.climo.databinding.FragmentSettingsBinding
import com.example.climo.settings.viewmodel.SettingsViewModel
import com.example.climo.settings.viewmodel.SettingsViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val viewModel: SettingsViewModel by viewModels{
        SettingsViewModelFactory(PreferenceManager.getDefaultSharedPreferences(requireContext())
        , LocationServices.getFusedLocationProviderClient(requireContext()))
    }
    companion object {
        val languageChangeTrigger = MutableLiveData<Unit>()
    }

    private val mapSelectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK){
            val date = result.data
            val latitude = date?.getFloatExtra("selected_latitude", 0f)?.toDouble() ?: 0.0
            val longitude = date?.getFloatExtra("selected_longitude", 0f)?.toDouble() ?: 0.0
            val locationName = sharedPreferences.getString("selected_location_name", "Selected Location") ?: "Selected Location"

            viewModel.saveLocation(latitude , longitude , locationName)
            Toast.makeText(requireContext(),getString(R.string.location_set_to, locationName), Toast.LENGTH_SHORT).show()
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted){
                sharedPreferences.edit().putString("initial_notification_choice", "enable").apply()
                Toast.makeText(requireContext(),  getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show()
            } else {
                sharedPreferences.edit().putString("initial_notification_choice", "disable").apply()
                binding.notificationSwitch.isChecked = false
                Toast.makeText(requireContext(), getString(R.string.notifications_disabled), Toast.LENGTH_SHORT).show()
            }
        }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupLocationSelection()
        setupLanguageSpinner()
        setupTempUnitSpinner()
        setupWindSpeedUnitSpinner()
        setupNotificationSwitch()
    }

    private fun setupLocationSelection(){
        val locationChoice = sharedPreferences.getString("initial_location_choice", "gps")
        binding.locationRadioGroup.check(
            if (locationChoice == "gps"){
                R.id.radioGps
            } else {
                R.id.radioMap
            }
        )
        binding.selectLocationButton.visibility =
            if (locationChoice == "map"){
                View.VISIBLE
            }else{
                View.GONE
            }
        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.radioGps ->{
                    viewModel.saveLocationChoice("gps")
                    binding.selectLocationButton.visibility = View.GONE
                    viewModel.fetchGpsLocation(fusedLocationProviderClient)
                }
                R.id.radioMap ->{
                    viewModel.saveLocationChoice("map")
                    binding.selectLocationButton.visibility = View.VISIBLE
                }
            }
        }
        binding.selectLocationButton.setOnClickListener {
            val intent = Intent(requireContext() , MapSelectionActivity::class.java)
            mapSelectionLauncher.launch(intent)
        }
    }

    private fun setupLanguageSpinner(){
        val languages = arrayOf(getString(R.string.language_english), getString(R.string.language_arabic))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter

        val savedLanguage = viewModel.getLanguage()
        binding.languageSpinner.setSelection(if (savedLanguage=="ar") 1 else 0)

        binding.languageSpinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val languageCode = if (position == 1) "ar" else "en"
                viewModel.saveLanguage(languageCode)
                updateLocale(languageCode)
                languageChangeTrigger.postValue(Unit)
                Toast.makeText(requireContext(), getString(R.string.language_changed, languages[position]), Toast.LENGTH_LONG).show()

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTempUnitSpinner(){
        val units = arrayOf(
            getString(R.string.unit_celsius),
            getString(R.string.unit_kelvin),
            getString(R.string.unit_fahrenheit)
        )
        val adapter = ArrayAdapter(requireContext() , android.R.layout.simple_spinner_item , units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.tempUnitSpinner.adapter = adapter

        val savedUnit = viewModel.getTempUnit()
        binding.tempUnitSpinner.setSelection(
            when(savedUnit){
                "standard" -> 1
                "imperial" -> 2
                else -> 0
            }
        )

        binding.tempUnitSpinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val unit = when (position) {
                    1 -> "standard"
                    2 -> "imperial"
                    else -> "metric"
                }
                viewModel.saveTempUnit(unit)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }
    }

    private fun setupWindSpeedUnitSpinner() {
        val units = arrayOf(getString(R.string.unit_mps), getString(R.string.unit_mph))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.windSpeedUnitSpinner.adapter = adapter

        val savedUnit = viewModel.getWindSpeedUnit()
        binding.windSpeedUnitSpinner.setSelection(if (savedUnit == "mph") 1 else 0)

        binding.windSpeedUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val unit = if (position == 1) "mph" else "mps"
                viewModel.saveWindSpeedUnit(unit)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}


        }
    }

    private fun setupNotificationSwitch() {
        val notificationChoice = sharedPreferences.getString("initial_notification_choice", "disable")
        binding.notificationSwitch.isChecked = notificationChoice == "enable"

        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    sharedPreferences.edit().putString("initial_notification_choice", "enable").apply()
                }
            } else {
                sharedPreferences.edit().putString("initial_notification_choice", "disable").apply()
            }
        }
    }
    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        //requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}










