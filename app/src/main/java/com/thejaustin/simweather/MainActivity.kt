package com.thejaustin.simweather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.thejaustin.simweather.data.model.WeatherResponse
import com.thejaustin.simweather.ui.adapter.AlertAdapter
import com.thejaustin.simweather.ui.adapter.DailyForecastAdapter
import com.thejaustin.simweather.ui.adapter.HourlyForecastAdapter
import com.thejaustin.simweather.ui.viewmodel.WeatherUiState
import com.thejaustin.simweather.ui.viewmodel.WeatherViewModel
import com.thejaustin.simweather.ui.util.ViewAnimations
import com.thejaustin.simweather.ui.util.UnitConverter
import com.thejaustin.simweather.ui.dialog.SettingsDialog
import com.thejaustin.simweather.data.preferences.SettingsPreferences
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settings: SettingsPreferences

    // Adapters
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyAdapter: DailyForecastAdapter
    private val alertAdapter = AlertAdapter()

    // Views
    private lateinit var btnCityPlanning: Button
    private lateinit var btnSimulate: Button
    private lateinit var btnSettings: Button
    private lateinit var tvLocation: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvCondition: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var rvHourly: RecyclerView
    private lateinit var rvDaily: RecyclerView
    private lateinit var rvAlerts: RecyclerView
    private lateinit var rainEffect: RainEffect
    private lateinit var snowEffect: SnowEffect
    private lateinit var fogEffect: FogEffect
    private lateinit var windEffect: WindEffect

    // Weather stats
    private lateinit var statFeelsLike: View
    private lateinit var statWind: View
    private lateinit var statPressure: View
    private lateinit var statHumidity: View
    private lateinit var statVisibility: View
    private lateinit var statUV: View

    // Weather API Key - Get yours from https://www.weatherapi.com/
    private val API_KEY by lazy { getString(R.string.weather_api_key) }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                fetchCurrentLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                fetchCurrentLocation()
            }
            else -> {
                // Use default location
                viewModel.fetchWeather(API_KEY, "New York")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = SettingsPreferences.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initAdapters()
        initViews()
        setupWeatherCards()
        setupRecyclerViews()
        setupCityPlanningButton()
        setupSimulateButton()
        setupSettingsButton()
        observeWeatherData()
        requestLocationPermission()
    }

    private fun setupWeatherCards() {
        val sharedPreferences = getSharedPreferences("SimWeather", MODE_PRIVATE)
        val layout = sharedPreferences.getString("layout", "Current Weather,Hourly Forecast,Daily Forecast,Weather Alerts")
        val weatherCards = layout?.split(",") ?: listOf("Current Weather", "Hourly Forecast", "Daily Forecast", "Weather Alerts")

        val container = findViewById<LinearLayout>(R.id.weatherCardContainer)
        container.removeAllViews()

        for (cardName in weatherCards) {
            val layoutId = when (cardName) {
                "Current Weather" -> R.layout.item_current_weather
                "Hourly Forecast" -> R.layout.item_hourly_forecast_section
                "Daily Forecast" -> R.layout.item_daily_forecast_section
                "Weather Alerts" -> R.layout.item_alerts_section
                "Clothing Advisor" -> R.layout.item_clothing_advisor_section
                "Pollen" -> R.layout.item_pollen_section
                else -> 0
            }
            if (layoutId != 0) {
                val view = layoutInflater.inflate(layoutId, container, false)
                container.addView(view)
            }
        }
    }

    private fun initAdapters() {
        hourlyAdapter = HourlyForecastAdapter(settings)
        dailyAdapter = DailyForecastAdapter(settings)
    }

    private fun initViews() {
        btnCityPlanning = findViewById(R.id.btnCityPlanning)
        btnSimulate = findViewById(R.id.btnSimulate)
        btnSettings = findViewById(R.id.btnSettings)
        tvLocation = findViewById(R.id.tvLocation)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvCondition = findViewById(R.id.tvCondition)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        rvHourly = findViewById(R.id.rvHourlyForecast)
        rvDaily = findViewById(R.id.rvDailyForecast)
        rvAlerts = findViewById(R.id.rvAlerts)
        rainEffect = findViewById(R.id.rainEffect)
        snowEffect = findViewById(R.id.snowEffect)
        fogEffect = findViewById(R.id.fogEffect)
        windEffect = findViewById(R.id.windEffect)

        statFeelsLike = findViewById(R.id.statFeelsLike)
        statWind = findViewById(R.id.statWind)
        statPressure = findViewById(R.id.statPressure)
        statHumidity = findViewById(R.id.statHumidity)
        statVisibility = findViewById(R.id.statVisibility)
        statUV = findViewById(R.id.statUV)
    }

    private fun setupCityPlanningButton() {
        btnCityPlanning.setOnClickListener {
            val intent = Intent(this, CityPlanningActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSimulateButton() {
        btnSimulate.setOnClickListener {
            viewModel.fetchSimulatedWeather()
        }
    }

    private fun setupSettingsButton() {
        btnSettings.setOnClickListener {
            SettingsDialog(this) {
                // Refresh adapters with new units
                initAdapters()
                rvHourly.adapter = hourlyAdapter
                rvDaily.adapter = dailyAdapter
                // Refresh the weather display
                viewModel.uiState.value.let { state ->
                    if (state is WeatherUiState.Success) {
                        updateUI(state.weatherData)
                    }
                }
            }.show()
        }
    }

    private fun setupRecyclerViews() {
        rvHourly.adapter = hourlyAdapter
        rvDaily.adapter = dailyAdapter
        rvAlerts.adapter = alertAdapter
    }

    private fun observeWeatherData() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WeatherUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        tvError.visibility = View.GONE
                    }
                    is WeatherUiState.Success -> {
                        progressBar.visibility = View.GONE
                        tvError.visibility = View.GONE
                        updateUI(state.weatherData)
                    }
                    is WeatherUiState.Error -> {
                        progressBar.visibility = View.GONE
                        tvError.visibility = View.VISIBLE
                        tvError.text = "Error: ${state.message}"
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun updateUI(weather: WeatherResponse) {
        // Animate location
        ViewAnimations.fadeIn(tvLocation, 100)
        tvLocation.text = "${weather.location.name}, ${weather.location.region}"

        // Animate current weather with staggered delays
        val currentWeatherCard = findViewById<View>(R.id.currentWeatherCard)
        ViewAnimations.slideUp(currentWeatherCard, 150)

        val units = settings.units

        tvTemperature.text = UnitConverter.temperature(weather.current.tempC, units)
        tvCondition.text = weather.current.condition.text

        // Weather stats with animations and unit conversion
        setStat(statFeelsLike, getString(R.string.feels_like),
            UnitConverter.temperature(weather.current.feelsLikeC, units))
        setStat(statWind, getString(R.string.wind),
            "${UnitConverter.speed(weather.current.windKph, units)} ${weather.current.windDir}")
        setStat(statPressure, getString(R.string.pressure),
            UnitConverter.pressure(weather.current.pressureMb, units))
        setStat(statHumidity, getString(R.string.humidity), "${weather.current.humidity}%")
        setStat(statVisibility, getString(R.string.visibility),
            UnitConverter.distance(weather.current.visibilityKm, units))
        setStat(statUV, getString(R.string.uv_index), weather.current.uv.toInt().toString())

        // Animate forecast sections
        findViewById<View>(R.id.tvHourlyTitle)?.let { ViewAnimations.fadeIn(it, 250) }
        rvHourly.let { ViewAnimations.slideInRight(it, 300) }

        // Hourly forecast (next 24 hours)
        val next24Hours = weather.forecast.forecastDays
            .flatMap { it.hour }
            .take(24)
        hourlyAdapter.submitList(next24Hours)

        // Animate daily forecast
        findViewById<View>(R.id.tvDailyTitle)?.let { ViewAnimations.fadeIn(it, 350) }
        rvDaily.let { ViewAnimations.slideInRight(it, 400) }

        // Daily forecast
        dailyAdapter.submitList(weather.forecast.forecastDays)

        // Alerts with animation
        if (weather.alerts != null && weather.alerts.alert.isNotEmpty()) {
            rvAlerts.visibility = View.VISIBLE
            ViewAnimations.slideUp(rvAlerts, 450)
            alertAdapter.submitList(weather.alerts.alert)
        } else {
            rvAlerts.visibility = View.GONE
        }

        // Show/hide rain effect
        if (weather.current.condition.text.contains("rain", ignoreCase = true)) {
            rainEffect.visibility = View.VISIBLE
        } else {
            rainEffect.visibility = View.GONE
        }

        // Show/hide snow effect
        if (weather.current.condition.text.contains("snow", ignoreCase = true)) {
            snowEffect.visibility = View.VISIBLE
        } else {
            snowEffect.visibility = View.GONE
        }

        // Show/hide fog effect
        if (weather.current.condition.text.contains("fog", ignoreCase = true)) {
            fogEffect.visibility = View.VISIBLE
        } else {
            fogEffect.visibility = View.GONE
        }

        // Show/hide wind effect
        if (weather.current.condition.text.contains("wind", ignoreCase = true)) {
            windEffect.visibility = View.VISIBLE
        } else {
            windEffect.visibility = View.GONE
        }

        // Air Quality
        weather.current.airQuality?.let {
            findViewById<TextView>(R.id.tvAqiValue).text = it.usEpaIndex.toString()
        }

        // Clothing Advisor
        findViewById<TextView>(R.id.tvClothingAdvice)?.text = ClothingAdvisor.getClothingAdvice(weather.current)

        // Pollen
        weather.current.pollen?.let {
            findViewById<TextView>(R.id.tvGrassPollen).text = it.grassPollen.toString()
            findViewById<TextView>(R.id.tvTreePollen).text = it.treePollen.toString()
            findViewById<TextView>(R.id.tvWeedPollen).text = it.weedPollen.toString()
        }
    }

    private fun setStat(view: View, label: String, value: String) {
        view.findViewById<TextView>(R.id.tvStatLabel).text = label
        view.findViewById<TextView>(R.id.tvStatValue).text = value
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.fetchWeather(API_KEY, "New York")
            return
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val locationString = "${location.latitude},${location.longitude}"
                    viewModel.fetchWeather(API_KEY, locationString)
                } else {
                    viewModel.fetchWeather(API_KEY, "New York")
                }
            }.addOnFailureListener {
                viewModel.fetchWeather(API_KEY, "New York")
            }
        } catch (e: Exception) {
            viewModel.fetchWeather(API_KEY, "New York")
        }
    }
}