package com.thejaustin.simweather.ui.weatherevents

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * SimCity style disaster effect: Thunderstorm & Lightning bolts across screen.
 */
class LightningEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFE0")
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

    private val flashPaint =
        Paint().apply {
            color = Color.argb(120, 255, 255, 255)
            style = Paint.Style.FILL
        }

    private var isFlashing = false
    private var flashTicks = 0
    private var boltPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        if (Random.nextInt(0, 80) == 1 && !isFlashing) {
            triggerLightning()
        }

        if (isFlashing) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flashPaint)
            canvas.drawPath(boltPath, paint)

            flashTicks++
            if (flashTicks > 4) {
                isFlashing = false
                flashTicks = 0
            }
        }
        invalidate()
    }

    private fun triggerLightning() {
        isFlashing = true
        flashTicks = 0
        boltPath.reset()

        val startX = Random.nextInt(50, (width - 50).coerceAtLeast(60)).toFloat()
        var currentX = startX
        var currentY = 0f

        boltPath.moveTo(currentX, currentY)
        val segmentCount = Random.nextInt(6, 12)
        val segHeight = height / segmentCount.toFloat()

        for (i in 0 until segmentCount) {
            currentY += segHeight
            currentX += Random.nextInt(-40, 40)
            boltPath.lineTo(currentX, currentY)
        }
    }
}
