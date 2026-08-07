package com.thejaustin.simweather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * A view to display a fog effect.
 */
class FogEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    // Semi-transparent gray
    private val paint =
        Paint().apply {
            color = Color.argb(100, 128, 128, 128)
            style = Paint.Style.FILL
        }
    private var alphaValue = 100
    private var alphaDirection = 1

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.alpha = alphaValue
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Animate alpha for a shimmering effect
        alphaValue += alphaDirection
        if (alphaValue <= 80 || alphaValue >= 120) {
            alphaDirection *= -1
        }

        // Redraw the view
        if (isShown) invalidate()
    }
}
