package com.thejaustin.simweather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Custom canvas view rendering precipitation accumulation volume (mm/in)
 * and hourly rain probability bars with retro SimCity 3000 styling.
 */
class PrecipitationGaugeView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private var precipMm: Double = 0.0
        private var rainChancePercent: Int = 0
        private var intensityText: String = "None"

        private val gaugeRect = RectF()
        private val fillRect = RectF()

        private val bgPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1A2B2A")
                style = Paint.Style.FILL
            }

        private val borderPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00AAFF")
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }

        private val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00CCFF")
                style = Paint.Style.FILL
            }

        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 24f
                isFakeBoldText = true
            }

        fun updatePrecipitation(
            mm: Double,
            chancePercent: Int,
            intensity: String,
        ) {
            precipMm = mm.coerceAtLeast(0.0)
            rainChancePercent = chancePercent.coerceIn(0, 100)
            intensityText = intensity
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()
            val padding = 12f

            // Outer border box
            gaugeRect.set(padding, padding, w - padding, h - padding)
            canvas.drawRect(gaugeRect, bgPaint)
            canvas.drawRect(gaugeRect, borderPaint)

            // Fill Bar based on rain chance percentage
            val barW = (w - (padding * 2)) * (rainChancePercent / 100f)
            fillRect.set(padding, padding, padding + barW, h - padding)
            canvas.drawRect(fillRect, fillPaint)

            // Center Label
            val label = "RAIN CHANCE: $rainChancePercent% ($precipMm mm)"
            val tw = textPaint.measureText(label)
            canvas.drawText(label, (w - tw) / 2f, (h / 2f) + 8f, textPaint)
        }
    }
