package com.example.climo.alerts.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.room.Query
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.climo.R
import com.example.climo.alerts.viewmodel.WeatherAlertsViewModel
import com.example.climo.alerts.viewmodel.WeatherAlertsViewModelFactory
import com.example.climo.alerts.worker.AlertWorker
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.data.model.WeatherAlert
import com.example.climo.data.remote.RetrofitClient
import com.example.climo.databinding.FragmentAddAlertDialogBinding
import com.google.android.material.datepicker.DateSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class AddAlertDialogFragment : DialogFragment() {

    private var _binding: FragmentAddAlertDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WeatherAlertsViewModel by viewModels{
        WeatherAlertsViewModelFactory(ClimoDatabase.getDatabase(requireContext()))
    }
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private var fromDateTime: LocalDateTime? = null
    private var toDateTime: LocalDateTime? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddAlertDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.citySearchAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position) as String
            fetchCityCoordinates(selectedCity)
        }

        binding.citySearchAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 >= 3) {
                    fetchCitySuggestions(s.toString())
                }
            }
        })

        binding.fromDateTimeButton.setOnClickListener {
            showDateTimePicker{ dateTime ->
                fromDateTime = dateTime
                binding.fromDateTimeButton.text = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        }

        binding.toDateTimeButton.setOnClickListener {
            showDateTimePicker { dateTime ->
                toDateTime = dateTime
                binding.toDateTimeButton.text = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        }

        binding.saveButton.setOnClickListener {
            val cityName = binding.citySearchAutoComplete.text.toString()
            val alertType = if (binding.notificationRadioButton.isChecked) "NOTIFICATION" else "HEADS_UP"
            if (cityName.isNotEmpty() && fromDateTime != null && toDateTime != null && selectedLatitude != 0.0) {
                val alert= WeatherAlert(
                    cityName= cityName,
                    latitude = selectedLatitude,
                    longitude = selectedLongitude,
                    fromDateTime = fromDateTime!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    toDateTime =  toDateTime!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    alertType = alertType
                )
//                viewModel.addAlert(alert)
//                scheduleAlert(alert)
                saveAndScheduleAlert(alert)
                //dismiss()
            }else{
                context?.let {
                    Toast.makeText(it, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()

                }
            }
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun fetchCitySuggestions(query: String){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getGeocoding(query,5, "ecfe2681690524ece36e0e4818523e5f")
                }
                val suggestions = response.map { it.name }
                binding.citySearchAutoComplete.setAdapter(
                    ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,suggestions)
                )
                binding.citySearchAutoComplete.showDropDown()
            }catch (e: Exception){

                context?.let {
                    Toast.makeText(it, getString(R.string.error_fetching_suggestions, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchCityCoordinates(city: String){
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getGeocoding(city, 1, "ecfe2681690524ece36e0e4818523e5f")
                }
                if (response.isNotEmpty()){
                    selectedLatitude = response[0].lat
                    selectedLongitude= response[0].lon
                }
            } catch (e : Exception){
                context?.let{
                    Toast.makeText(it, getString(R.string.error_fetching_city, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDateTimePicker(onDateSelected: (LocalDateTime) -> Unit){
        val now = LocalDateTime.now()
        DatePickerDialog(
            requireContext(),
            {_, year, month, day ->
                TimePickerDialog(
                    requireContext(),
                    {_, hour, minute ->
                        val dateTime = LocalDateTime.of(year, month + 1 , day , hour , minute)
                        onDateSelected(dateTime)
                    },
                    now.hour, now.minute, true
                ).show()
            },
            now.year, now.monthValue -1 , now.dayOfMonth
        ).show()
    }


    private fun saveAndScheduleAlert(alert: WeatherAlert) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val insertedId = withContext(Dispatchers.IO) {
                    ClimoDatabase.getDatabase(requireContext()).weatherAlertDao().insert(alert)
                }
                Log.d("AddAlertDialogFragment", "Inserted alert ID: $insertedId")

                if (insertedId > 0) {
                    val data = Data.Builder()
                        .putInt("alertId", insertedId.toInt())
                        .putString("cityName", alert.cityName)
                        .putString("fromDateTime", alert.fromDateTime)
                        .putString("toDateTime", alert.toDateTime)
                        .putString("alertType", alert.alertType)
                        .putDouble("latitude", alert.latitude)
                        .putDouble("longitude", alert.longitude)
                        .build()

                    val fromTime = LocalDateTime.parse(alert.fromDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    val delay = Duration.between(LocalDateTime.now(), fromTime).toMillis()
                    Log.d("AddAlertDialogFragment", "Scheduling alert with delay: $delay ms")

                    if (delay > 0) {
                        val workRequest = OneTimeWorkRequestBuilder<com.example.climo.alerts.worker.AlertWorker>()
                            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .addTag("alert_$insertedId")
                            .build()
                        activity?.let {
                            WorkManager.getInstance(it).enqueue(workRequest)
                            Log.d("AddAlertDialogFragment", "Work request enqueued for alert ID: $insertedId")
                        } ?: Log.w("AddAlertDialogFragment", "Activity is null, cannot enqueue work")
                    } else {
                        Log.w("AddAlertDialogFragment", "Delay is not positive: $delay, skipping scheduling")
                        activity?.let {
                            Toast.makeText(it, "Please select a future start time", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("AddAlertDialogFragment", "Failed to insert alert into database")
                    activity?.let {
                        Toast.makeText(it, "Failed to save alert", Toast.LENGTH_SHORT).show()
                    }
                }
                dismiss() // Moved dismiss after coroutine completion
            } catch (e: Exception) {
                Log.e("AddAlertDialogFragment", "Error scheduling alert: ${e.message}")
                activity?.let {
                    Toast.makeText(it, "Error saving alert: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}