package com.thejaustin.simweather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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
import com.thejaustin.simweather.ui.dialog.SettingsDialog
import com.thejaustin.simweather.ui.util.ClothingAdvisor
import com.thejaustin.simweather.ui.util.UnitConverter
import com.thejaustin.simweather.ui.util.ViewAnimations
import com.thejaustin.simweather.ui.viewmodel.WeatherUiState
import com.thejaustin.simweather.ui.viewmodel.WeatherViewModel
import com.thejaustin.simweather.data.preferences.SettingsPreferences
import com.thejaustin.simweather.ui.weather_events.FogEffect
import com.thejaustin.simweather.ui.weather_events.RainEffect
import com.thejaustin.simweather.ui.weather_events.SnowEffect
import com.thejaustin.simweather.ui.weather_events.WindEffect
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
    private lateinit var loadingOverlay: android.widget.FrameLayout
    private lateinit var tvLoadingStatus: TextView
    private lateinit var tvError: TextView
    private lateinit var rainEffect: RainEffect
    private lateinit var snowEffect: SnowEffect
    private lateinit var fogEffect: FogEffect
    private lateinit var windEffect: WindEffect
    private lateinit var weatherCardContainer: LinearLayout

    private var loadingJob: kotlinx.coroutines.Job? = null
    private val loadingMessages = listOf(
        "Reticulating Splines...",
        "Adjusting Ozone Levels...",
        "Calibrating Wind Sensors...",
        "Downloading Cloud Patterns...",
        "Simulating Traffic...",
        "Generating Terrain...",
        "Calculating Humidity...",
        "Triangulating Satellites..."
    )

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
        setupCityPlanningButton()
        setupSimulateButton()
        setupSettingsButton()
        observeWeatherData()
        requestLocationPermission()
    }

    private fun setupWeatherCards() {
        val sharedPreferences = getSharedPreferences("SimWeather", MODE_PRIVATE)
        val layout = sharedPreferences.getString("layout", "Current Weather,Hourly Forecast,Daily Forecast,Weather Alerts,Clothing Advisor,Pollen")
        val weatherCards = layout?.split(",") ?: listOf("Current Weather", "Hourly Forecast", "Daily Forecast", "Weather Alerts", "Clothing Advisor", "Pollen")

        weatherCardContainer.removeAllViews()

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
                val view = layoutInflater.inflate(layoutId, weatherCardContainer, false)
                weatherCardContainer.addView(view)
            }
        }
        // Re-initialize views that are now part of the dynamic cards
        initCardViews()
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
        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvLoadingStatus = findViewById(R.id.tvLoadingStatus)
        tvError = findViewById(R.id.tvError)
        rainEffect = findViewById(R.id.rainEffect)
        snowEffect = findViewById(R.id.snowEffect)
        fogEffect = findViewById(R.id.fogEffect)
        windEffect = findViewById(R.id.windEffect)
        weatherCardContainer = findViewById(R.id.weatherCardContainer)
    }

    private fun initCardViews() {
        // This function is called after the cards are inflated and added to the container
        val rvHourly = weatherCardContainer.findViewById<RecyclerView>(R.id.rvHourlyForecast)
        val rvDaily = weatherCardContainer.findViewById<RecyclerView>(R.id.rvDailyForecast)
        val rvAlerts = weatherCardContainer.findViewById<RecyclerView>(R.id.rvAlerts)

        rvHourly?.adapter = hourlyAdapter
        rvDaily?.adapter = dailyAdapter
        rvAlerts?.adapter = alertAdapter
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
                // Re-setup cards and adapters
                setupWeatherCards()
                // Refresh the weather display
                viewModel.uiState.value.let { state ->
                    if (state is WeatherUiState.Success) {
                        updateUI(state.weatherData)
                    }
                }
            }.show()
        }
    }

    private fun observeWeatherData() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WeatherUiState.Loading -> {
                        loadingOverlay.visibility = View.VISIBLE
                        tvError.visibility = View.GONE
                        
                        loadingJob?.cancel()
                        loadingJob = lifecycleScope.launch {
                            while (true) {
                                tvLoadingStatus.text = loadingMessages.random()
                                kotlinx.coroutines.delay(800)
                            }
                        }
                    }
                    is WeatherUiState.Success -> {
                        loadingJob?.cancel()
                        loadingOverlay.visibility = View.GONE
                        tvError.visibility = View.GONE
                        updateUI(state.weatherData)
                    }
                    is WeatherUiState.Error -> {
                        loadingJob?.cancel()
                        loadingOverlay.visibility = View.GONE
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

        val units = settings.units

        // Update current weather card
        val currentCard = weatherCardContainer.findViewById<View>(R.id.currentWeatherCard)
        currentCard?.let { ViewAnimations.animateWeatherCard(it) }

        val tvTemp = weatherCardContainer.findViewById<TextView>(R.id.tvTemperature)
        tvTemp?.text = UnitConverter.temperature(weather.current.tempC, units)
        tvTemp?.let { ViewAnimations.popUp(it, 200) }

        weatherCardContainer.findViewById<TextView>(R.id.tvCondition)?.text = weather.current.condition.text

        // Weather stats with animations and unit conversion
        weatherCardContainer.findViewById<View>(R.id.statFeelsLike)?.let { setStat(it, getString(R.string.feels_like), UnitConverter.temperature(weather.current.feelsLikeC, units)) }
        weatherCardContainer.findViewById<View>(R.id.statWind)?.let { setStat(it, getString(R.string.wind), "${UnitConverter.speed(weather.current.windKph, units)} ${weather.current.windDir}") }
        weatherCardContainer.findViewById<View>(R.id.statPressure)?.let { setStat(it, getString(R.string.pressure), UnitConverter.pressure(weather.current.pressureMb, units)) }
        weatherCardContainer.findViewById<View>(R.id.statHumidity)?.let { setStat(it, getString(R.string.humidity), "${weather.current.humidity}%") }
        weatherCardContainer.findViewById<View>(R.id.statVisibility)?.let { setStat(it, getString(R.string.visibility), UnitConverter.distance(weather.current.visibilityKm, units)) }
        weatherCardContainer.findViewById<View>(R.id.statUV)?.let { setStat(it, getString(R.string.uv_index), weather.current.uv.toInt().toString()) }

        // Hourly forecast (next 24 hours)
        val next24Hours = weather.forecast.forecastDays.flatMap { it.hour }.take(24)
        hourlyAdapter.submitList(next24Hours)

        // Daily forecast
        dailyAdapter.submitList(weather.forecast.forecastDays)

        // Alerts with animation
        val rvAlerts = weatherCardContainer.findViewById<RecyclerView>(R.id.rvAlerts)
        if (weather.alerts != null && weather.alerts.alert.isNotEmpty()) {
            rvAlerts?.visibility = View.VISIBLE
            rvAlerts?.let { ViewAnimations.slideUp(it, 450) }
            alertAdapter.submitList(weather.alerts.alert)
        } else {
            rvAlerts?.visibility = View.GONE
        }

        // Show/hide rain effect
        if (settings.disastersEnabled && weather.current.condition.text.contains("rain", ignoreCase = true)) {
            rainEffect.visibility = View.VISIBLE
        } else {
            rainEffect.visibility = View.GONE
        }

        // Show/hide snow effect
        if (settings.disastersEnabled && weather.current.condition.text.contains("snow", ignoreCase = true)) {
            snowEffect.visibility = View.VISIBLE
        } else {
            snowEffect.visibility = View.GONE
        }

        // Show/hide fog effect
        if (settings.disastersEnabled && weather.current.condition.text.contains("fog", ignoreCase = true)) {
            fogEffect.visibility = View.VISIBLE
        } else {
            fogEffect.visibility = View.GONE
        }

        // Show/hide wind effect
        if (settings.disastersEnabled && weather.current.condition.text.contains("wind", ignoreCase = true)) {
            windEffect.visibility = View.VISIBLE
        } else {
            windEffect.visibility = View.GONE
        }

        // Air Quality
        weather.current.airQuality?.let {
            weatherCardContainer.findViewById<TextView>(R.id.tvAqiValue)?.text = it.usEpaIndex.toString()
        }

        // Clothing Advisor
        weatherCardContainer.findViewById<TextView>(R.id.tvClothingAdvice)?.text = ClothingAdvisor.getClothingAdvice(weather.current)

        // Pollen
        weather.current.pollen?.let {
            weatherCardContainer.findViewById<TextView>(R.id.tvGrassPollen)?.text = it.grassPollen.toString()
            weatherCardContainer.findViewById<TextView>(R.id.tvTreePollen)?.text = it.treePollen.toString()
            weatherCardContainer.findViewById<TextView>(R.id.tvWeedPollen)?.text = it.weedPollen.toString()
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
