package com.thejaustin.simweather.ui.weather_events

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * A view to display a snow effect.
 */
class SnowEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val snowflakes = mutableListOf<Snowflake>()
    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    init {
        for (i in 0..100) {
            snowflakes.add(Snowflake(Random.nextInt(0, width), Random.nextInt(0, height)))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        snowflakes.forEach { snowflake ->
            snowflake.fall()
            canvas.drawCircle(snowflake.x.toFloat(), snowflake.y.toFloat(), snowflake.radius.toFloat(), paint)
            if (snowflake.y > height) {
                snowflake.y = 0
                snowflake.x = Random.nextInt(0, width)
            }
        }
        invalidate() // Redraw the view
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        snowflakes.clear()
        for (i in 0..100) {
            snowflakes.add(Snowflake(Random.nextInt(0, w), Random.nextInt(0, h)))
        }
    }

    private data class Snowflake(var x: Int, var y: Int) {
        val radius = Random.nextInt(2, 6)
        private val speed = Random.nextInt(2, 8)

        fun fall() {
            y += speed
        }
    }
}
