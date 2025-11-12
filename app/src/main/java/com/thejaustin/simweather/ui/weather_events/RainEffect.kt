package com.thejaustin.simweather.ui.weather_events

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * A view to display a rain effect.
 */
class RainEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val raindrops = mutableListOf<Raindrop>()
    private val paint = Paint().apply {
        color = Color.argb(180, 173, 216, 230) // Light blue
        strokeWidth = 3f
    }

    init {
        for (i in 0..100) {
            raindrops.add(Raindrop(Random.nextInt(0, width), Random.nextInt(0, height)))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        raindrops.forEach { raindrop ->
            raindrop.fall()
            canvas.drawLine(raindrop.x.toFloat(), raindrop.y.toFloat(), raindrop.x.toFloat(), (raindrop.y + 20).toFloat(), paint)
            if (raindrop.y > height) {
                raindrop.y = 0
                raindrop.x = Random.nextInt(0, width)
            }
        }
        invalidate() // Redraw the view
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        raindrops.clear()
        for (i in 0..100) {
            raindrops.add(Raindrop(Random.nextInt(0, w), Random.nextInt(0, h)))
        }
    }

    private data class Raindrop(var x: Int, var y: Int) {
        private val speed = Random.nextInt(5, 15)

        fun fall() {
            y += speed
        }
    }
}
