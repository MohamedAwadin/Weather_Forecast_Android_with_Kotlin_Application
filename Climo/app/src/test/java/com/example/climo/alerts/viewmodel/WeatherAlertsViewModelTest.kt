package com.example.climo.alerts.viewmodel

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.climo.data.model.WeatherAlert
import com.example.climo.data.repository.WeatherAlertRepository
import com.example.climo.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherAlertsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: WeatherAlertsViewModel
    private lateinit var repository: WeatherAlertRepository
    private val testTimestamp = "2025-05-28T10:00:00"
    private val alertsLiveData = MutableLiveData<List<WeatherAlert>>()

    @Before
    fun setup() {
        repository = mockk()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Mock android.util.Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        // Mock getActiveAlertsLiveData for any timestamp
        every { repository.getActiveAlertsLiveData(any()) } returns alertsLiveData

        viewModel = WeatherAlertsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun activeAlerts_initializedWithRepositoryLiveData() = runTest {
        // Given
        val alerts = listOf(
            WeatherAlert(
                id = 1,
                cityName = "Test City",
                latitude = 0.0,
                longitude = 0.0,
                fromDateTime = "2025-05-28T09:00:00",
                toDateTime = "2025-05-29T10:00:00",
                alertType = "Rain"
            )
        )
        alertsLiveData.postValue(alerts)

        // When
        val result = viewModel.activeAlerts.getOrAwaitValue(time = 5)

        // Then
        assertThat(result).isEqualTo(alerts)
    }

    @Test
    fun addAlert_callsRepositoryInsert() = runTest {
        // Given
        val alert = WeatherAlert(
            id = 1,
            cityName = "Test City",
            latitude = 0.0,
            longitude = 0.0,
            fromDateTime = "2025-05-28T09:00:00",
            toDateTime = "2025-05-29T10:00:00",
            alertType = "Rain"
        )
        coEvery { repository.insertAlert(alert) } returns 1L

        // When
        viewModel.addAlert(alert)

        // Then
        coVerify { repository.insertAlert(alert) }
    }

    @Test
    fun deleteAlert_callsRepositoryDelete() = runTest {
        // Given
        val alert = WeatherAlert(
            id = 1,
            cityName = "Test City",
            latitude = 0.0,
            longitude = 0.0,
            fromDateTime = "2025-05-28T09:00:00",
            toDateTime = "2025-05-29T10:00:00",
            alertType = "Rain"
        )
        coEvery { repository.deleteAlert(alert) } returns Unit

        // When
        viewModel.deleteAlert(alert)

        // Then
        coVerify { repository.deleteAlert(alert) }
    }

    
}