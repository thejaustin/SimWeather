package com.thejaustin.simweather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

/**
 * A view to display a high-quality snow effect with swaying motion.
 */
class SnowEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val snowflakes = mutableListOf<Snowflake>()
    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        snowflakes.forEach { snowflake ->
            snowflake.fall()
            canvas.drawCircle(snowflake.x.toFloat(), snowflake.y.toFloat(), snowflake.radius.toFloat(), paint)
            if (snowflake.y > height) {
                snowflake.y = -10
                snowflake.x = Random.nextInt(0, width)
            }
        }
        invalidate()
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        snowflakes.clear()
        if (w > 0 && h > 0) {
            for (i in 0..100) {
                snowflakes.add(Snowflake(Random.nextInt(0, w), Random.nextInt(0, h)))
            }
        }
    }

    private data class Snowflake(var x: Int, var y: Int) {
        val radius = Random.nextInt(3, 8)
        private val speed = Random.nextInt(2, 7)
        private var angle = Random.nextDouble(0.0, 360.0)

        fun fall() {
            y += speed
            angle += 0.05
            x += (sin(angle) * 1.5).toInt()
        }
    }
}
