package com.example.climo.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.climo.data.local.WeatherAlertDao
import com.example.climo.data.model.WeatherAlert
import com.example.climo.data.remote.RetrofitClient
import com.example.climo.data.remote.WeatherApi
import com.example.climo.data.remote.WeatherResponse
import com.example.climo.data.remote.Weather_Desc
import com.example.climo.data.remote.WeatherCondition
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherAlertRepositoryTest {

    private lateinit var repository: WeatherAlertRepository
    private lateinit var weatherAlertDao: WeatherAlertDao
    private lateinit var weatherApi: WeatherApi
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        weatherAlertDao = mockk()
        weatherApi = mockk()
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        // Mock RetrofitClient.api
        mockkObject(RetrofitClient)
        every { RetrofitClient.api } returns weatherApi

        // Mock android.util.Log
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        repository = WeatherAlertRepository(weatherAlertDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getActiveAlertsLiveData_returnsDaoLiveData() {
        // Given
        val currentDateTime = "2025-05-28T10:00:00"
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
        val liveData = MutableLiveData<List<WeatherAlert>>(alerts)
        every { weatherAlertDao.getActiveAlertsLiveData(currentDateTime) } returns liveData

        // When
        val result = repository.getActiveAlertsLiveData(currentDateTime)

        // Then
        assertThat(result).isEqualTo(liveData)
        assertThat(result.value).isEqualTo(alerts)
    }





    @Test
    fun getWeatherDescription_success_returnsDescription() = runTest {
        // Given
        val latitude = 40.0
        val longitude = -74.0
        val weather = Weather_Desc(description = "sunny")
        val response = WeatherResponse(
            weather = listOf(weather),
            main = WeatherCondition(temp = 20.0)
        )
        val call = mockk<Call<WeatherResponse>>()
        every { call.execute() } returns Response.success(response)
        coEvery { weatherApi.getCurrentWeather(latitude, longitude, any()) } returns call

        // When
        val result = repository.getWeatherDescription(latitude, longitude)

        // Then
        assertThat(result).isEqualTo("Sunny")
    }


}