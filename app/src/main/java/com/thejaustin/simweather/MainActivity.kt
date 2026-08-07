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
import com.thejaustin.simweather.data.simulation.BudgetReport
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
import com.thejaustin.simweather.ui.weatherevents.FogEffect
import com.thejaustin.simweather.ui.weatherevents.LightningEffect
import com.thejaustin.simweather.ui.weatherevents.MeteorEffect
import com.thejaustin.simweather.ui.weatherevents.RainEffect
import com.thejaustin.simweather.ui.weatherevents.SnowEffect
import com.thejaustin.simweather.ui.weatherevents.TornadoEffect
import com.thejaustin.simweather.ui.weatherevents.WindEffect
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

    private var selectedAdvisorIndex = 0
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

    private val defaultCards = listOf(
        "Current Weather",
        "Hourly Forecast",
        "Daily Forecast",
        "Astronomy & Moon",
        "Wind & Pressure",
        "Precipitation & Dew Point",
        "UV & Solar Safety",
        "Air Quality (AQI)",
        "Weather Alerts",
        "Clothing Advisor",
        "Pollen"
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
        val layout = sharedPreferences.getString("layout", defaultCards.joinToString(","))
        val weatherCards = layout?.split(",") ?: defaultCards

        weatherCardContainer.removeAllViews()

        for (cardName in weatherCards) {
            val layoutId = when (cardName) {
                "Current Weather" -> R.layout.item_current_weather
                "Hourly Forecast" -> R.layout.item_hourly_forecast_section
                "Daily Forecast" -> R.layout.item_daily_forecast_section
                "Astronomy & Moon" -> R.layout.item_astronomy_section
                "Wind & Pressure" -> R.layout.item_wind_details_section
                "Precipitation & Dew Point" -> R.layout.item_precipitation_section
                "UV & Solar Safety" -> R.layout.item_uv_section
                "Air Quality (AQI)" -> R.layout.item_aqi_section
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

        val cur = weather.current
        weatherCardContainer.findViewById<View>(R.id.statFeelsLike)?.let {
            setStat(it, getString(R.string.feels_like), UnitConverter.temperature(cur.feelsLikeC, units))
        }
        weatherCardContainer.findViewById<View>(R.id.statWind)?.let {
            setStat(it, getString(R.string.wind), "${UnitConverter.speed(cur.windKph, units)} ${cur.windDir}")
        }
        weatherCardContainer.findViewById<View>(R.id.statPressure)?.let {
            setStat(it, getString(R.string.pressure), UnitConverter.pressure(cur.pressureMb, units))
        }
        weatherCardContainer.findViewById<View>(R.id.statHumidity)?.let {
            setStat(it, getString(R.string.humidity), "${cur.humidity}%")
        }
        weatherCardContainer.findViewById<View>(R.id.statVisibility)?.let {
            setStat(it, getString(R.string.visibility), UnitConverter.distance(cur.visibilityKm, units))
        }
        weatherCardContainer.findViewById<View>(R.id.statUV)?.let {
            setStat(it, getString(R.string.uv_index), cur.uv.toInt().toString())
        }

        val next24Hours = weather.forecast.forecastDays.flatMap { it.hour }.take(24)
        hourlyAdapter.submitList(next24Hours)
        dailyAdapter.submitList(weather.forecast.forecastDays)

        updateModernWeatherCards(weather)

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
        val isDisaster = settings.disastersEnabled
        rainEffect.visibility = if (isDisaster && condText.contains("rain")) View.VISIBLE else View.GONE
        snowEffect.visibility = if (isDisaster && condText.contains("snow")) View.VISIBLE else View.GONE
        fogEffect.visibility = if (isDisaster && condText.contains("fog")) View.VISIBLE else View.GONE
        windEffect.visibility = if (isDisaster && (condText.contains("wind") || cur.windKph > 35)) View.VISIBLE else View.GONE
        val isStorm = condText.contains("thunder") || condText.contains("storm")
        lightningEffect.visibility = if (isDisaster && isStorm) View.VISIBLE else View.GONE
        meteorEffect.visibility = if (isDisaster && condText.contains("meteor")) View.VISIBLE else View.GONE
        tornadoEffect.visibility = if (isDisaster && (condText.contains("tornado") || cur.windKph > 55)) View.VISIBLE else View.GONE

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

    private fun updateModernWeatherCards(weather: WeatherResponse) {
        val units = settings.units

        val astro = weather.forecast.forecastDays.firstOrNull()?.astro
        if (astro != null) {
            weatherCardContainer.findViewById<TextView>(R.id.tvSunrise)?.text = astro.sunrise
            weatherCardContainer.findViewById<TextView>(R.id.tvSunset)?.text = astro.sunset
            weatherCardContainer.findViewById<TextView>(R.id.tvMoonPhase)?.text = astro.moonPhase
            weatherCardContainer.findViewById<TextView>(R.id.tvMoonIllumination)?.text = "${astro.moonIllumination}%"
        }

        val gustKph = (weather.current.windKph * 1.35).toInt()
        val gustSpeed = UnitConverter.speed(gustKph.toDouble(), units)
        weatherCardContainer.findViewById<TextView>(R.id.tvWindGusts)?.text = gustSpeed
        val windBearingText = "${weather.current.windDegree}° (${weather.current.windDir})"
        weatherCardContainer.findViewById<TextView>(R.id.tvWindBearing)?.text = windBearingText
        weatherCardContainer.findViewById<TextView>(R.id.tvPressureValue)?.text = UnitConverter.pressure(weather.current.pressureMb, units)
        val pressureMb = weather.current.pressureMb
        val pressureTrendText = when {
            pressureMb > 1015.0 -> "Rising (High Pressure)"
            pressureMb < 1005.0 -> "Falling (Low Pressure)"
            else -> "Steady"
        }
        weatherCardContainer.findViewById<TextView>(R.id.tvPressureTrend)?.text = pressureTrendText

        val dewPointC = (weather.current.tempC - ((100 - weather.current.humidity) / 5.0)).toInt()
        weatherCardContainer.findViewById<TextView>(R.id.tvPrecipTotal)?.text = UnitConverter.precipitation(weather.current.precipMm, units)
        weatherCardContainer.findViewById<TextView>(R.id.tvDewPoint)?.text = UnitConverter.temperature(dewPointC.toDouble(), units)
        weatherCardContainer.findViewById<TextView>(R.id.tvHumidityDetail)?.text = "${weather.current.humidity}%"
        weatherCardContainer.findViewById<TextView>(R.id.tvCloudCover)?.text = "${weather.current.cloud}%"

        val uv = weather.current.uv
        val uvRiskText = when {
            uv >= 11.0 -> "${uv.toInt()} (Extreme)"
            uv >= 8.0 -> "${uv.toInt()} (Very High)"
            uv >= 6.0 -> "${uv.toInt()} (High)"
            uv >= 3.0 -> "${uv.toInt()} (Moderate)"
            else -> "${uv.toInt()} (Low)"
        }
        weatherCardContainer.findViewById<TextView>(R.id.tvUvIndexDetail)?.text = uvRiskText
        weatherCardContainer.findViewById<TextView>(R.id.tvUvPeakTime)?.text = "1:00 PM (Peak)"
        val sunAdvice = when {
            uv >= 8.0 -> "Avoid direct sunlight. Wear hats, sunglasses & SPF 50+."
            uv >= 5.0 -> "Apply SPF 30+ sunscreen and seek shade during noon hours."
            else -> "Low solar risk. Minimal sun protection required."
        }
        weatherCardContainer.findViewById<TextView>(R.id.tvSunProtectionAdvice)?.text = sunAdvice

        weather.current.airQuality?.let { aqi ->
            val epaIndex = aqi.usEpaIndex
            val statusText = when (epaIndex) {
                1 -> "GOOD (EPA 1)"
                2 -> "MODERATE (EPA 2)"
                3 -> "SENSITIVE (EPA 3)"
                4 -> "UNHEALTHY (EPA 4)"
                5 -> "VERY UNHEALTHY (EPA 5)"
                else -> "HAZARDOUS (EPA 6)"
            }
            weatherCardContainer.findViewById<TextView>(R.id.tvAqiStatusBadge)?.text = statusText
            weatherCardContainer.findViewById<TextView>(R.id.tvPm25)?.text = "${aqi.pm2_5} µg/m³"
            weatherCardContainer.findViewById<TextView>(R.id.tvPm10)?.text = "${aqi.pm10} µg/m³"
            weatherCardContainer.findViewById<TextView>(R.id.tvOzone)?.text = "${aqi.o3} ppb"
            weatherCardContainer.findViewById<TextView>(R.id.tvNo2)?.text = "${aqi.no2} ppb"

            val healthAdvText = when (epaIndex) {
                1 -> "Air quality is satisfactory. Ideal for outdoor activities and city strolls."
                2 -> "Acceptable air quality; unusually sensitive Sims should reduce outdoor exertion."
                3 -> "Sensitive groups may experience health effects. General public less affected."
                4 -> "Everyone may begin to experience health effects; sensitive groups stay indoors."
                else -> "Emergency health warning: All citizens should avoid outdoor exertion!"
            }
            weatherCardContainer.findViewById<TextView>(R.id.tvAqiHealthAdvice)?.text = healthAdvText
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
