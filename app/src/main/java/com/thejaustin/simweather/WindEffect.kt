package com.thejaustin.simweather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * A view to display a wind effect.
 */
class WindEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val windLines = mutableListOf<WindLine>()

    // Light gray, semi-transparent
    private val paint =
        Paint().apply {
            color = Color.argb(100, 200, 200, 200)
            strokeWidth = 2f
        }

    // windLines will be initialized in onSizeChanged when view has proper dimensions

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        windLines.forEach { windLine ->
            windLine.move()
            canvas.drawLine(windLine.x.toFloat(), windLine.y.toFloat(), (windLine.x + 50).toFloat(), windLine.y.toFloat(), paint)
            if (windLine.x > width) {
                windLine.x = -50
                windLine.y = if (height > 0) Random.nextInt(0, height) else 0
            }
        }
        // Redraw the view
        invalidate()
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        windLines.clear()
        if (w > 0 && h > 0) {
            for (i in 0..50) {
                windLines.add(WindLine(Random.nextInt(0, w), Random.nextInt(0, h)))
            }
        }
    }

    private data class WindLine(var x: Int, var y: Int) {
        private val speed = Random.nextInt(5, 15)

        fun move() {
            x += speed
        }
    }
}
