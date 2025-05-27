package com.example.climo.alerts.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.example.climo.R
import com.example.climo.alerts.viewmodel.WeatherAlertsViewModel
import com.example.climo.alerts.viewmodel.WeatherAlertsViewModelFactory
import com.example.climo.data.local.ClimoDatabase
import com.example.climo.databinding.FragmentWeatherAlertsBinding
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class WeatherAlertsFragment : Fragment() {

    private var _binding : FragmentWeatherAlertsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WeatherAlertsViewModel by viewModels{
        WeatherAlertsViewModelFactory(ClimoDatabase.getDatabase(requireContext()))
    }
    private lateinit var adapter: WeatherAlertsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentWeatherAlertsBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WeatherAlertsAdapter(
            onDeleteClick = { alert ->
                viewModel.deleteAlert(alert)
                Snackbar.make(
                    binding.root,
                    getString(R.string.alert_removed, alert.cityName),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.undo)) {
                    viewModel.addAlert(alert)
                }.show()
            }
        )
        binding.alertsRecyclerView.adapter = adapter

        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        viewModel.activeAlerts.observe(viewLifecycleOwner) { alerts ->
            Log.d("WeatherAlertsFragment", "Active alerts updated: $alerts")
            binding.emptyStateText.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
            binding.alertsRecyclerView.visibility = if (alerts.isEmpty()) View.GONE else View.VISIBLE
            adapter.submitList(alerts)
        }

        viewModel.cleanupExpiredAlerts(currentDateTime)

        binding.addAlertFab.setOnClickListener {
            Log.d("WeatherAlertsFragment", "FAB clicked")
            AddAlertDialogFragment().show(childFragmentManager, "AddAlertDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}