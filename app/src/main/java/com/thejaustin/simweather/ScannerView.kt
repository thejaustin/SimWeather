package com.thejaustin.simweather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class ScannerView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        // Semi-transparent green
        private val gridPaint =
            Paint().apply {
                color = Color.parseColor("#4000FF00")
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }

        private val scanPaint =
            Paint().apply {
                color = Color.GREEN
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }

        private var scanY = 0f
        private val scanSpeed = 15f
        private val gridSize = 100f

        private var scanShader: LinearGradient? = null

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (w > 0 && h > 0) {
                scanShader =
                    LinearGradient(
                        0f,
                        -100f,
                        0f,
                        0f,
                        intArrayOf(Color.TRANSPARENT, Color.GREEN),
                        null,
                        Shader.TileMode.CLAMP,
                    )
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val width = width.toFloat()
            val height = height.toFloat()

            // Draw Grid
            for (x in 0..width.toInt() step gridSize.toInt()) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height, gridPaint)
            }
            for (y in 0..height.toInt() step gridSize.toInt()) {
                canvas.drawLine(0f, y.toFloat(), width, y.toFloat(), gridPaint)
            }

            // Draw Scanning Line
            // Add a gradient tail to the scan line
            scanPaint.shader = scanShader

            // Draw a rect for the scan beam instead of just a line
            canvas.save()
            canvas.translate(0f, scanY)
            canvas.drawRect(0f, -100f, width, 0f, scanPaint)
            canvas.restore()

            // Reset shader for the sharp leading edge
            scanPaint.shader = null
            // Bright header
            scanPaint.color = Color.parseColor("#CCFFCC")
            canvas.drawLine(0f, scanY, width, scanY, scanPaint)

            // Update position
            scanY += scanSpeed
            if (scanY > height + 100) {
                scanY = -100f
            }

            if (isShown) invalidate()
        }
    }
