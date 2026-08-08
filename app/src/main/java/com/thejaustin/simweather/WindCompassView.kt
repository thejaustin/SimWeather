package com.thejaustin.simweather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom canvas view rendering a retro 16-point nautical wind compass rose
 * with an animated needle indicating exact live wind bearing and speed.
 */
class WindCompassView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private var windDegrees: Float = 0f
        private var windSpeedText: String = "0 km/h"
        private var windDirText: String = "N"

        private val circleRect = RectF()
        private val needlePath = Path()

        private val bgPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1A2B1A")
                style = Paint.Style.FILL
            }

        private val ringPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00FF66")
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }

        private val cardinalTextPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00FF66")
                textSize = 22f
                isFakeBoldText = true
            }

        private val needlePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FF3366")
                style = Paint.Style.FILL
            }

        private val needleOutlinePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

        private val centerDotPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.YELLOW
                style = Paint.Style.FILL
            }

        fun updateWind(
            degrees: Float,
            speedText: String,
            dirText: String,
        ) {
            windDegrees = degrees % 360f
            windSpeedText = speedText
            windDirText = dirText
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()
            val cx = w / 2f
            val cy = h / 2f
            val radius = (minOf(w, h) / 2f) - 16f

            if (radius <= 0) return

            // Outer Ring
            circleRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
            canvas.drawCircle(cx, cy, radius, bgPaint)
            canvas.drawCircle(cx, cy, radius, ringPaint)

            // Ticks & Cardinal Labels (N, E, S, W)
            val cardinals = arrayOf("N", "E", "S", "W")
            val cardinalAngles = floatArrayOf(-90f, 0f, 90f, 180f)

            for (i in cardinals.indices) {
                val rad = Math.toRadians(cardinalAngles[i].toDouble())
                val tx = cx + (radius - 24f) * cos(rad).toFloat()
                val ty = cy + (radius - 24f) * sin(rad).toFloat() + 8f

                val tw = cardinalTextPaint.measureText(cardinals[i])
                canvas.drawText(cardinals[i], tx - (tw / 2f), ty, cardinalTextPaint)
            }

            // Draw Wind Direction Needle
            canvas.save()
            canvas.rotate(windDegrees, cx, cy)

            needlePath.reset()
            needlePath.moveTo(cx, cy - (radius - 30f))
            needlePath.lineTo(cx - 12f, cy + 15f)
            needlePath.lineTo(cx + 12f, cy + 15f)
            needlePath.close()

            canvas.drawPath(needlePath, needlePaint)
            canvas.drawPath(needlePath, needleOutlinePaint)

            canvas.restore()

            // Center Pin
            canvas.drawCircle(cx, cy, 6f, centerDotPaint)
        }
    }
