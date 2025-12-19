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
    private lateinit var tvFunds: TextView
    private lateinit var tvDate: TextView
    private lateinit var tickerBar: TextView
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
    
    private val simNews = listOf(
        "Traffic reporting heavy delays on Main St.",
        "Llama spotted near City Hall.",
        "Citizens demand more parks!",
        "Power plant output stable.",
        "RCI demand for Residential is soaring.",
        "Mayor approval rating at 95%.",
        "Alien spaceship sighting unconfirmed.",
        "New weather satellite deployed successfully.",
        "Sims enjoying the nice weather.",
        "Construction complete on new stadium."
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
        tvFunds = findViewById(R.id.tvFunds)
        tvDate = findViewById(R.id.tvDate)
        tickerBar = findViewById(R.id.tickerBar)
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
    
    // ... (rest of methods)

    private fun updateUI(weather: WeatherResponse) {
        // Animate location
        ViewAnimations.fadeIn(tvLocation, 100)
        tvLocation.text = "${weather.location.name}, ${weather.location.region}"

        // Update Header Info based on settings
        if (settings.gameHudEnabled) {
            tvFunds.visibility = View.VISIBLE
            tickerBar.visibility = View.VISIBLE
            
            val randomFunds = (10000..50000).random()
            tvFunds.text = "§ $randomFunds"
            updateTicker(weather)
        } else {
            tvFunds.visibility = View.GONE
            tickerBar.visibility = View.GONE
        }
        
        // Date Display
        try {
            if (settings.showRealTime) {
                // Parse and reformat if possible, or just use raw string for now if format matches "yyyy-MM-dd HH:mm"
                // Ideally use SimpleDateFormat, but for simplicity here:
                tvDate.text = weather.location.localTime
            } else {
                val dateParts = weather.location.localTime.split(" ")[0].split("-")
                // YYYY-MM-DD -> Dec 2025
                val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                if (dateParts.size == 3) {
                    val monthIndex = dateParts[1].toInt()
                    val year = dateParts[0]
                    tvDate.text = "${months[monthIndex]} $year"
                }
            }
        } catch (e: Exception) {
            tvDate.text = "Jan 2000"
        }

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

    private fun updateTicker(weather: WeatherResponse) {
        val sb = StringBuilder()
        
        // Add current weather to ticker
        sb.append(" *** CURRENT WEATHER: ${weather.current.condition.text}, ${weather.current.tempC}°C *** ")
        
        // Add alerts if any
        if (weather.alerts != null && weather.alerts.alert.isNotEmpty()) {
             weather.alerts.alert.forEach {
                 sb.append(" !!! ALERT: ${it.headline} !!! ")
             }
        }
        
        // Add random news
        simNews.shuffled().take(5).forEach {
            sb.append(" ... $it ... ")
        }
        
        tickerBar.text = sb.toString()
        tickerBar.isSelected = true // Start marquee
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
