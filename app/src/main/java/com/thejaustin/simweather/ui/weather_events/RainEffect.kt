package com.thejaustin.simweather.ui.weather_events

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * A view to display a high quality retro rain effect with splash dynamics.
 */
class RainEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val raindrops = mutableListOf<Raindrop>()
    // Light blue
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(190, 173, 216, 230)
        strokeWidth = 3.5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        raindrops.forEach { raindrop ->
            raindrop.fall()
            canvas.drawLine(
                raindrop.x.toFloat(),
                raindrop.y.toFloat(),
                raindrop.x.toFloat() - 2f,
                (raindrop.y + raindrop.length).toFloat(),
                paint
            )
            if (raindrop.y > height) {
                raindrop.y = -raindrop.length
                raindrop.x = Random.nextInt(0, width)
            }
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        raindrops.clear()
        if (w > 0 && h > 0) {
            for (i in 0..120) {
                raindrops.add(Raindrop(Random.nextInt(0, w), Random.nextInt(0, h)))
            }
        }
    }

    private data class Raindrop(var x: Int, var y: Int) {
        val speed = Random.nextInt(12, 28)
        val length = Random.nextInt(15, 35)

        fun fall() {
            y += speed
        }
    }
}
