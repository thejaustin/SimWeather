# SimWeather 3000

SimWeather is a unique weather application that reimagines your daily forecast through the lens of a classic city-building simulator.

## Features

### 🏙️ SimCity 3000 Aesthetic
Experience the nostalgia of 1999 with a fully themed user interface:
- **Reticulating Splines**: Authentic loading sequences and status messages.
- **Isometric UI**: Custom backgrounds, panels, and buttons inspired by SC3K.
- **Monospace Fonts**: Clean, retro typography.
- **Synthesized 8-Bit Sound Engine**: Real-time synthesized chiptune sound effects for buttons, reticulations, sirens, cash registers, and speed toggles.

### 🌤️ Real-Time & Simulated Weather
- **Live Forecasts**: Powered by WeatherAPI for accurate current, hourly, and daily weather.
- **Modern Weather Data Suite**:
  - **Astronomy & Moon**: Sunrise, Sunset, Moonrise, Moonset, Moon Phase, and Moon Illumination %.
  - **Wind & Pressure**: Wind gusts, bearing angle (0°-360°), and barometric pressure trends.
  - **Precipitation & Dew Point**: 24h precipitation total, calculated dew point, humidity index, and cloud cover %.
  - **UV & Solar Safety**: UV index scale, solar peak times, and sunscreen protection guidance.
- **Simulation Mode**: Test different weather scenarios with the "Simulate" button.
- **Natural Weather Scanner**: A high-tech satellite scanning visualizer for weather updates.
- **Metropolis Search**: Search any city worldwide or select from metropolis presets (Tokyo, London, New York, Paris, Sydney, SimCity).

### 🏗️ City Planning, Budget & Multi-Advisor System
- **Customizable Layout**: Drag and drop weather modules (Current, Hourly, Daily, Astronomy, Wind, Dew Point, UV Safety, Alerts, Advisors, Pollen) to design your perfect dashboard.
- **City Treasury & Tax Economy**: Accumulate § Simoleons in real-time. Adjust municipal tax rates (1% to 20%) and manage emergency expenditures.
- **Weather Ordinances**: Pass city-wide weather policies:
  - Industrial Smog Scrubbers (§500/mo)
  - Snowplow Taskforce (§600/mo)
  - Public Cooling Shelters (§400/mo)
  - Free Sunscreen Program (§300/mo)
- **SimCity Mayor Advisors**: Interactive advice from 5 city advisors:
  - **Mortimer Green** (Financial): Utility expenses & heating/cooling budget impacts.
  - **Karen Landers** (Environment): Air quality (AQI) and pollen emissions.
  - **Maria Luna** (Public Safety): Severe weather alerts & storm safety.
  - **Moe Sillan** (Transportation): Snowplow advisories, icing hazards, & highway traffic.
  - **Dr. Constable** (Public Health): Extreme UV, heat stroke, & outdoor activity index.
- **RCI Demand Gauge**: Real-time Residential, Commercial, and Industrial demand bars fluctuating based on weather productivity.

### ⚠️ Dynamic Disasters (Effects)
Toggle visual weather effects in the Gameplay Settings:
- Rain 🌧️
- Snow ❄️
- Fog 🌫️
- Wind 💨
- Thunderstorm & Lightning ⚡
- Meteor Shower ☄️
- Swirling Tornado Vortex 🌪️

### 🕹️ Gameplay & Interface Settings
Customize your experience:
- **Disasters**: Toggle weather & disaster overlay effects.
- **Sound Effects**: Toggle synthesized 8-bit sound effects.
- **Simulation Speed**: Adjust the speed of the simulation.
- **Game HUD**: Show/Hide the "Funds" and "News Ticker" for a cleaner look.
- **Real-Time Clock**: Switch between game-style date (Dec 2025) and exact local time.

## Releases

### v1.6.0 - The Modern Weather Data Update
*Released: August 2026*

- **Astronomy & Moon Card**: Detailed sunrise/sunset times, moon phase name, and illumination percentage.
- **Wind & Pressure Card**: Wind gusts, bearing angle degrees, barometric pressure trend (Rising/Falling/Steady).
- **Precipitation & Dew Point Card**: 24h precipitation total, calculated dew point temperature, relative humidity, and cloud coverage.
- **UV Index & Solar Safety Card**: UV index risk level, peak solar time, and sun protection guidance.
- **Expanded City Planning**: Fully integrated into the drag-and-drop dashboard planner.

### v1.5.0 - The City Economy & Ordinances Update
*Released: August 2026*

- **City Budget Simulator**: Persistent Treasury accumulation based on municipal tax rates and weather prosperity factors.
- **Weather Ordinances**: Interactive policy toggles for Smog Scrubbers, Snowplows, Cooling Shelters, and Sunscreen programs.
- **Budget & Ordinance Dialog**: Retro SC3K financial statement dialog to adjust taxes and hear cash register sound effects (§ Simoleons).
- **Swirling Tornado Disaster**: Isometric tornado vortex particle effect with flying debris.
- **Expanded Audio Effects**: Cash register, disaster sirens, and tornado whistle sound synthesis.

### v1.4.0 - The Ultracode Mayor Update
*Released: August 2026*

- **Synthesized 8-Bit Audio Engine**: Added real-time sound effects for click, spline reticulation, disaster sirens, and speed switches.
- **Multi-Advisor System**: Expanded advisor advice across 5 departments with interactive tab navigation.
- **RCI Demand Gauge**: Added custom SimCity RCI demand view reacting to temperature, rain, and air quality.
- **Expanded Disasters**: Added Lightning Thunderstorms and Meteor Shower overlay effects.
- **Metropolis Search Dialog**: Added interactive city search and metropolis preset selector.
- **Spotless Formatting & Test Suite**: Full unit testing suite for converters, advisors, and simulation engine.

### v1.3.0 - The Gameplay Update
*Released: December 2025*

- **Interface Toggles**: Added settings to toggle the Game HUD (Funds/Ticker) and Real-Time clock.
- **Bug Fixes**: Improved stability and layout rendering.

### v1.2.0 - The City Life Update
*Released: December 2025*

- **News Ticker**: A scrolling marquee at the bottom displaying weather alerts, traffic reports, and city news.
- **Heads-Up Display (HUD)**: Track your "Funds" (Simoleons) and current Date.
- **Advisor Avatars**: Pixel-art avatar layout feel.

### v1.1.0 - The Mayor's Update
*Released: December 2025*

- **New UI**: Complete re-skin with concrete/grass backgrounds and SC3K color palette.
- **Scanner View**: Satellite laser scanning visual effect.

## Setup

1. Get an API key from [WeatherAPI](https://www.weatherapi.com/).
2. Add it to your `local.properties` or string resources.
3. Build and run!

---
*Inspired by Maxis' SimCity 3000.*