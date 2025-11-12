package com.thejaustin.simweather.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.thejaustin.simweather.R
import kotlin.random.Random

class WeatherOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<WeatherParticle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var weatherType: WeatherType = WeatherType.CLEAR
    private var isAnimating = false

    enum class WeatherType {
        CLEAR, RAIN, SNOW, CLOUDY
    }

    init {
        paint.style = Paint.Style.FILL
    }

    fun setWeatherType(type: WeatherType) {
        weatherType = type
        particles.clear()
        when (type) {
            WeatherType.RAIN -> {
                paint.color = context.getColor(R.color.sim_cyan_bright)
                paint.alpha = 180
                createRainParticles()
            }
            WeatherType.SNOW -> {
                paint.color = context.getColor(R.color.white)
                paint.alpha = 200
                createSnowParticles()
            }
            else -> {
                particles.clear()
            }
        }
        isAnimating = type != WeatherType.CLEAR
        if (isAnimating) {
            invalidate()
        }
    }

    private fun createRainParticles() {
        repeat(50) {
            particles.add(
                WeatherParticle(
                    x = Random.nextFloat() * width,
                    y = Random.nextFloat() * height,
                    speedX = Random.nextFloat() * 2 - 1,
                    speedY = Random.nextFloat() * 15 + 10,
                    size = Random.nextFloat() * 3 + 1
                )
            )
        }
    }

    private fun createSnowParticles() {
        repeat(30) {
            particles.add(
                WeatherParticle(
                    x = Random.nextFloat() * width,
                    y = Random.nextFloat() * height,
                    speedX = Random.nextFloat() * 2 - 1,
                    speedY = Random.nextFloat() * 3 + 1,
                    size = Random.nextFloat() * 6 + 3
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isAnimating || particles.isEmpty()) return

        particles.forEach { particle ->
            when (weatherType) {
                WeatherType.RAIN -> {
                    // Draw rain lines
                    canvas.drawLine(
                        particle.x,
                        particle.y,
                        particle.x + particle.speedX,
                        particle.y + particle.size * 5,
                        paint
                    )
                }
                WeatherType.SNOW -> {
                    // Draw snowflakes
                    canvas.drawCircle(particle.x, particle.y, particle.size, paint)
                }
                else -> {}
            }

            // Update particle position
            particle.x += particle.speedX
            particle.y += particle.speedY

            // Reset particle if it goes off screen
            if (particle.y > height) {
                particle.y = -10f
                particle.x = Random.nextFloat() * width
            }
            if (particle.x < 0 || particle.x > width) {
                particle.x = Random.nextFloat() * width
            }
        }

        invalidate()
    }

    private data class WeatherParticle(
        var x: Float,
        var y: Float,
        val speedX: Float,
        val speedY: Float,
        val size: Float
    )
}
