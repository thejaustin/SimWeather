package com.thejaustin.simweather.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Custom view representing the iconic SimCity 3000 RCI (Residential, Commercial, Industrial)
 * Demand Gauge. Dynamic bar heights reflect atmospheric city productivity indices.
 */
class RciDemandView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        // Demand levels from -1.0 to 1.0 (0.0 is neutral)
        private var rDemand = 0.8f
        private var cDemand = 0.6f
        private var iDemand = 0.4f

        private val bgPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1F2D1F")
                style = Paint.Style.FILL
            }

        private val borderPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8E9E8E")
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }

        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 28f
                isFakeBoldText = true
            }

        // Sim Green (Residential)
        private val rPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00FF66")
            }

        // Sim Blue (Commercial)
        private val cPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00AAFF")
            }

        // Sim Yellow (Industrial)
        private val iPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFCC00")
            }

        fun updateDemand(
            r: Float,
            c: Float,
            i: Float,
        ) {
            rDemand = r.coerceIn(-1f, 1f)
            cDemand = c.coerceIn(-1f, 1f)
            iDemand = i.coerceIn(-1f, 1f)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()
            val padding = 12f

            // Background box
            canvas.drawRect(0f, 0f, w, h, bgPaint)
            canvas.drawRect(0f, 0f, w, h, borderPaint)

            val barWidth = (w - (padding * 4)) / 3f
            val centerY = h / 2f
            val maxBarHeight = (h / 2f) - padding - 15f

            // Draw Center Zero Line
            val zeroPaint =
                Paint().apply {
                    color = Color.parseColor("#446644")
                    strokeWidth = 2f
                }
            canvas.drawLine(padding, centerY, w - padding, centerY, zeroPaint)

            // Draw R Bar
            drawBar(canvas, padding, centerY, barWidth, maxBarHeight, rDemand, rPaint, "R")

            // Draw C Bar
            drawBar(canvas, padding * 2 + barWidth, centerY, barWidth, maxBarHeight, cDemand, cPaint, "C")

            // Draw I Bar
            drawBar(canvas, padding * 3 + barWidth * 2, centerY, barWidth, maxBarHeight, iDemand, iPaint, "I")
        }

        private fun drawBar(
            canvas: Canvas,
            x: Float,
            centerY: Float,
            barWidth: Float,
            maxHeight: Float,
            value: Float,
            paint: Paint,
            label: String,
        ) {
            val barH = value * maxHeight
            val rect =
                if (value >= 0) {
                    RectF(x, centerY - barH, x + barWidth, centerY)
                } else {
                    RectF(x, centerY, x + barWidth, centerY - barH)
                }
            canvas.drawRect(rect, paint)

            // Label
            textPaint.color = paint.color
            val textWidth = textPaint.measureText(label)
            canvas.drawText(label, x + (barWidth - textWidth) / 2f, height - 6f, textPaint)
        }
    }
