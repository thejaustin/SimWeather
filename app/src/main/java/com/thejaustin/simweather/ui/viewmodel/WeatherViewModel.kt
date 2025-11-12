package com.thejaustin.simweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thejaustin.simweather.data.model.WeatherResponse
import com.thejaustin.simweather.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weatherData: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchWeather(apiKey: String, location: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getWeatherForecast(apiKey, location).fold(
                onSuccess = { weatherData ->
                    _uiState.value = WeatherUiState.Success(weatherData)
                },
                onFailure = { error ->
                    _uiState.value = WeatherUiState.Error(
                        error.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }
}
