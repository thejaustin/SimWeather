package com.thejaustin.simweather.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class ScannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Semi-transparent green
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#4000FF00")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val scanPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private var scanY = 0f
    private val scanSpeed = 15f
    private val gridSize = 100f

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
        val shader = LinearGradient(
            0f, scanY - 100, 0f, scanY,
            intArrayOf(Color.TRANSPARENT, Color.GREEN),
            null,
            Shader.TileMode.CLAMP
        )
        scanPaint.shader = shader
        
        // Draw a rect for the scan beam instead of just a line
        canvas.drawRect(0f, scanY - 100, width, scanY, scanPaint)
        
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

        invalidate()
    }
}
