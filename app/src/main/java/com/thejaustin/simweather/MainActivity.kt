package com.thejaustin.simweather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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
import com.thejaustin.simweather.data.preferences.SettingsPreferences
import com.thejaustin.simweather.data.simulation.CityBudgetManager
import com.thejaustin.simweather.ui.adapter.AlertAdapter
import com.thejaustin.simweather.ui.adapter.DailyForecastAdapter
import com.thejaustin.simweather.ui.adapter.HourlyForecastAdapter
import com.thejaustin.simweather.ui.dialog.BudgetDialog
import com.thejaustin.simweather.ui.dialog.LocationSearchDialog
import com.thejaustin.simweather.ui.dialog.SettingsDialog
import com.thejaustin.simweather.ui.util.SimAdvisorManager
import com.thejaustin.simweather.ui.util.SoundManager
import com.thejaustin.simweather.ui.util.UnitConverter
import com.thejaustin.simweather.ui.util.ViewAnimations
import com.thejaustin.simweather.ui.view.RciDemandView
import com.thejaustin.simweather.ui.viewmodel.WeatherUiState
import com.thejaustin.simweather.ui.viewmodel.WeatherViewModel
import com.thejaustin.simweather.ui.weather_events.FogEffect
import com.thejaustin.simweather.ui.weather_events.LightningEffect
import com.thejaustin.simweather.ui.weather_events.MeteorEffect
import com.thejaustin.simweather.ui.weather_events.RainEffect
import com.thejaustin.simweather.ui.weather_events.SnowEffect
import com.thejaustin.simweather.ui.weather_events.TornadoEffect
import com.thejaustin.simweather.ui.weather_events.WindEffect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settings: SettingsPreferences
    private lateinit var soundManager: SoundManager
    private lateinit var budgetManager: CityBudgetManager

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
    private lateinit var lightningEffect: LightningEffect
    private lateinit var meteorEffect: MeteorEffect
    private lateinit var tornadoEffect: TornadoEffect
    private lateinit var weatherCardContainer: LinearLayout

    private var selectedAdvisorIndex = 0 // 0: Fin, 1: Env, 2: Safe, 3: Tra, 4: Hlth
    private var cachedWeather: WeatherResponse? = null

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

    private val apiKey by lazy { getString(R.string.weather_api_key) }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> fetchCurrentLocation()
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> fetchCurrentLocation()
            else -> viewModel.fetchWeather(apiKey, "New York")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settings = SettingsPreferences.getInstance(this)
        soundManager = SoundManager.getInstance(this)
        budgetManager = CityBudgetManager(settings)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initAdapters()
        initViews()
        setupWeatherCards()
        setupCityPlanningButton()
        setupSimulateButton()
        setupSettingsButton()
        setupLocationSearch()
        setupBudgetClick()
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
        lightningEffect = findViewById(R.id.lightningEffect)
        meteorEffect = findViewById(R.id.meteorEffect)
        tornadoEffect = findViewById(R.id.tornadoEffect)
        weatherCardContainer = findViewById(R.id.weatherCardContainer)
    }

    private fun initCardViews() {
        val rvHourly = weatherCardContainer.findViewById<RecyclerView>(R.id.rvHourlyForecast)
        val rvDaily = weatherCardContainer.findViewById<RecyclerView>(R.id.rvDailyForecast)
        val rvAlerts = weatherCardContainer.findViewById<RecyclerView>(R.id.rvAlerts)

        rvHourly?.adapter = hourlyAdapter
        rvDaily?.adapter = dailyAdapter
        rvAlerts?.adapter = alertAdapter

        setupAdvisorTabs()
    }

    private fun setupLocationSearch() {
        tvLocation.setOnClickListener {
            soundManager.playClick()
            LocationSearchDialog(this) { selectedCity ->
                soundManager.playSplineReticulate()
                viewModel.fetchWeather(apiKey, selectedCity)
            }.show()
        }
    }

    private fun setupBudgetClick() {
        tvFunds.setOnClickListener {
            soundManager.playCashRegister()
            BudgetDialog(this) {
                cachedWeather?.let { updateUI(it) }
            }.show()
        }
    }

    private fun setupCityPlanningButton() {
        btnCityPlanning.setOnClickListener {
            soundManager.playClick()
            val intent = Intent(this, CityPlanningActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSimulateButton() {
        btnSimulate.setOnClickListener {
            soundManager.playSplineReticulate()
            viewModel.fetchSimulatedWeather()
        }
    }

    private fun setupSettingsButton() {
        btnSettings.setOnClickListener {
            soundManager.playClick()
            SettingsDialog(this) {
                initAdapters()
                setupWeatherCards()
                viewModel.uiState.value.let { state ->
                    if (state is WeatherUiState.Success) {
                        updateUI(state.weatherData)
                    }
                }
            }.show()
        }
    }

    private fun setupAdvisorTabs() {
        val btnFin = weatherCardContainer.findViewById<Button>(R.id.btnAdvFin) ?: return
        val btnEnv = weatherCardContainer.findViewById<Button>(R.id.btnAdvEnv)
        val btnSafe = weatherCardContainer.findViewById<Button>(R.id.btnAdvSafe)
        val btnTra = weatherCardContainer.findViewById<Button>(R.id.btnAdvTra)
        val btnHlth = weatherCardContainer.findViewById<Button>(R.id.btnAdvHlth)

        val buttons = listOf(btnFin, btnEnv, btnSafe, btnTra, btnHlth)
        buttons.forEachIndexed { index, button ->
            button?.setOnClickListener {
                soundManager.playClick()
                selectedAdvisorIndex = index
                cachedWeather?.let { updateAdvisorDisplay(it) }
            }
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
                        cachedWeather = state.weatherData
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
        ViewAnimations.fadeIn(tvLocation, 100)
        tvLocation.text = "${weather.location.name}, ${weather.location.region} (Tap to change)"

        val budgetReport = budgetManager.calculateMonthlyBudget(weather.current)

        if (settings.gameHudEnabled) {
            tvFunds.visibility = View.VISIBLE
            tickerBar.visibility = View.VISIBLE
            tvFunds.text = "§ ${budgetReport.currentTreasury}"
            updateTicker(weather, budgetReport)
        } else {
            tvFunds.visibility = View.GONE
            tickerBar.visibility = View.GONE
        }

        try {
            if (settings.showRealTime) {
                tvDate.text = weather.location.localTime
            } else {
                val dateParts = weather.location.localTime.split(" ")[0].split("-")
                val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                if (dateParts.size == 3) {
                    val monthIndex = dateParts[1].toInt()
                    val year = dateParts[0]
                    tvDate.text = "${months[monthIndex]} $year"
                }
            }
        } catch (_: Exception) {
            tvDate.text = "Jan 2000"
        }

        val units = settings.units
        val currentCard = weatherCardContainer.findViewById<View>(R.id.currentWeatherCard)
        currentCard?.let { ViewAnimations.animateWeatherCard(it) }

        val tvTemp = weatherCardContainer.findViewById<TextView>(R.id.tvTemperature)
        tvTemp?.text = UnitConverter.temperature(weather.current.tempC, units)
        tvTemp?.let { ViewAnimations.popUp(it, 200) }

        weatherCardContainer.findViewById<TextView>(R.id.tvCondition)?.text = weather.current.condition.text

        weatherCardContainer.findViewById<View>(R.id.statFeelsLike)?.let { setStat(it, getString(R.string.feels_like), UnitConverter.temperature(weather.current.feelsLikeC, units)) }
        weatherCardContainer.findViewById<View>(R.id.statWind)?.let { setStat(it, getString(R.string.wind), "${UnitConverter.speed(weather.current.windKph, units)} ${weather.current.windDir}") }
        weatherCardContainer.findViewById<View>(R.id.statPressure)?.let { setStat(it, getString(R.string.pressure), UnitConverter.pressure(weather.current.pressureMb, units)) }
        weatherCardContainer.findViewById<View>(R.id.statHumidity)?.let { setStat(it, getString(R.string.humidity), "${weather.current.humidity}%") }
        weatherCardContainer.findViewById<View>(R.id.statVisibility)?.let { setStat(it, getString(R.string.visibility), UnitConverter.distance(weather.current.visibilityKm, units)) }
        weatherCardContainer.findViewById<View>(R.id.statUV)?.let { setStat(it, getString(R.string.uv_index), weather.current.uv.toInt().toString()) }

        val next24Hours = weather.forecast.forecastDays.flatMap { it.hour }.take(24)
        hourlyAdapter.submitList(next24Hours)
        dailyAdapter.submitList(weather.forecast.forecastDays)

        val rvAlerts = weatherCardContainer.findViewById<RecyclerView>(R.id.rvAlerts)
        if (weather.alerts != null && weather.alerts.alert.isNotEmpty()) {
            rvAlerts?.visibility = View.VISIBLE
            rvAlerts?.let { ViewAnimations.slideUp(it, 450) }
            alertAdapter.submitList(weather.alerts.alert)
            soundManager.playAlert()
        } else {
            rvAlerts?.visibility = View.GONE
        }

        val condText = weather.current.condition.text.lowercase()
        rainEffect.visibility = if (settings.disastersEnabled && condText.contains("rain")) View.VISIBLE else View.GONE
        snowEffect.visibility = if (settings.disastersEnabled && condText.contains("snow")) View.VISIBLE else View.GONE
        fogEffect.visibility = if (settings.disastersEnabled && condText.contains("fog")) View.VISIBLE else View.GONE
        windEffect.visibility = if (settings.disastersEnabled && (condText.contains("wind") || weather.current.windKph > 35)) View.VISIBLE else View.GONE
        lightningEffect.visibility = if (settings.disastersEnabled && (condText.contains("thunder") || condText.contains("storm"))) View.VISIBLE else View.GONE
        meteorEffect.visibility = if (settings.disastersEnabled && condText.contains("meteor")) View.VISIBLE else View.GONE
        tornadoEffect.visibility = if (settings.disastersEnabled && (condText.contains("tornado") || weather.current.windKph > 55)) View.VISIBLE else View.GONE

        weather.current.airQuality?.let {
            weatherCardContainer.findViewById<TextView>(R.id.tvAqiValue)?.text = it.usEpaIndex.toString()
        }

        updateAdvisorDisplay(weather)

        weather.current.pollen?.let {
            weatherCardContainer.findViewById<TextView>(R.id.tvGrassPollen)?.text = it.grassPollen.toString()
            weatherCardContainer.findViewById<TextView>(R.id.tvTreePollen)?.text = it.treePollen.toString()
            weatherCardContainer.findViewById<TextView>(R.id.tvWeedPollen)?.text = it.weedPollen.toString()
        }
    }

    private fun updateAdvisorDisplay(weather: WeatherResponse) {
        val tvAdvisorTitle = weatherCardContainer.findViewById<TextView>(R.id.tvAdvisorTitle) ?: return
        val tvAdviceText = weatherCardContainer.findViewById<TextView>(R.id.tvClothingAdvice) ?: return
        val rciGauge = weatherCardContainer.findViewById<RciDemandView>(R.id.rciDemandGauge)

        val opinion = when (selectedAdvisorIndex) {
            0 -> SimAdvisorManager.getFinancialAdvice(weather.current)
            1 -> SimAdvisorManager.getEnvironmentalAdvice(weather.current)
            2 -> SimAdvisorManager.getSafetyAdvice(weather)
            3 -> SimAdvisorManager.getTransportationAdvice(weather.current)
            else -> SimAdvisorManager.getHealthAdvice(weather.current)
        }

        tvAdvisorTitle.text = opinion.advisorName
        tvAdviceText.text = "${opinion.title}: ${opinion.advice}"

        val temp = weather.current.tempC
        val rDemand = if (temp in 18.0..26.0) 0.9f else 0.3f
        val cDemand = if (weather.current.condition.text.lowercase().contains("rain")) 0.2f else 0.7f
        val iDemand = if (temp < 10 || temp > 30) 0.8f else 0.4f

        rciGauge?.updateDemand(rDemand, cDemand, iDemand)
    }

    private fun updateTicker(weather: WeatherResponse, budgetReport: BudgetReport) {
        val sb = StringBuilder()
        sb.append(" *** CURRENT WEATHER: ${weather.current.condition.text}, ${weather.current.tempC}°C *** ")
        sb.append(" [TOWN BUDGET: Tax Rate ${settings.taxRate}%, Monthly Cashflow: §${budgetReport.netMonthlyCashflow}] ")

        if (weather.alerts != null && weather.alerts.alert.isNotEmpty()) {
            weather.alerts.alert.forEach {
                sb.append(" !!! ALERT: ${it.headline} !!! ")
            }
        }
        simNews.shuffled().take(5).forEach {
            sb.append(" ... $it ... ")
        }
        tickerBar.text = sb.toString()
        tickerBar.isSelected = true
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
            ) == PackageManager.PERMISSION_GRANTED -> fetchCurrentLocation()
            else -> locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.fetchWeather(apiKey, "New York")
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
                    viewModel.fetchWeather(apiKey, locationString)
                } else {
                    viewModel.fetchWeather(apiKey, "New York")
                }
            }.addOnFailureListener {
                viewModel.fetchWeather(apiKey, "New York")
            }
        } catch (_: Exception) {
            viewModel.fetchWeather(apiKey, "New York")
        }
    }
}
