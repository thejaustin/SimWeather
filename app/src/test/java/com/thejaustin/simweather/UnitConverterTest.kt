package com.thejaustin.simweather

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {
    @Test
    fun testTemperatureMetric() {
        val result = UnitConverter.temperature(25.0, Units.METRIC)
        assertEquals("25°C", result)
    }

    @Test
    fun testTemperatureImperial() {
        val result = UnitConverter.temperature(25.0, Units.IMPERIAL)
        assertEquals("77°F", result)
    }

    @Test
    fun testSpeedMetric() {
        val result = UnitConverter.speed(100.0, Units.METRIC)
        assertEquals("100 kph", result)
    }

    @Test
    fun testSpeedImperial() {
        val result = UnitConverter.speed(100.0, Units.IMPERIAL)
        assertEquals("62 mph", result)
    }

    @Test
    fun testDistanceMetric() {
        val result = UnitConverter.distance(10.0, Units.METRIC)
        assertEquals("10 km", result)
    }

    @Test
    fun testDistanceImperial() {
        val result = UnitConverter.distance(10.0, Units.IMPERIAL)
        assertEquals("6 mi", result)
    }

    @Test
    fun testPressureMetric() {
        val result = UnitConverter.pressure(1013.25, Units.METRIC)
        assertEquals("1013 mb", result)
    }

    @Test
    fun testPressureImperial() {
        val result = UnitConverter.pressure(1013.25, Units.IMPERIAL)
        assertEquals("29.92 inHg", result)
    }

    @Test
    fun testPrecipitationMetric() {
        val result = UnitConverter.precipitation(25.4, Units.METRIC)
        assertEquals("25.4 mm", result)
    }

    @Test
    fun testPrecipitationImperial() {
        val result = UnitConverter.precipitation(25.4, Units.IMPERIAL)
        assertEquals("1.00 in", result)
    }
}
