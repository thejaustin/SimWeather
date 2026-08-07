package com.thejaustin.simweather.ui.weatherevents

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.random.Random

/**
 * SimCity classic disaster effect: Swirling Tornado Vortex & flying debris.
 */
class TornadoEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val funnelParticles = mutableListOf<FunnelParticle>()
    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 110, 110, 110)
            style = Paint.Style.FILL
        }

    // Brown debris
    private val debrisPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8B4513")
            style = Paint.Style.FILL
        }

    private var tornadoX = 0f
    private var tornadoSpeed = 3f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        if (tornadoX == 0f) tornadoX = width * 0.5f

        tornadoX += tornadoSpeed
        if (tornadoX > width - 100 || tornadoX < 100) {
            tornadoSpeed *= -1f
        }

        funnelParticles.forEach { p ->
            p.update(tornadoX)
            val pPaint = if (p.isDebris) debrisPaint else paint
            canvas.drawCircle(p.currentX, p.currentY, p.radius, pPaint)
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
        funnelParticles.clear()
        if (w > 0 && h > 0) {
            for (i in 0..150) {
                val yProgress = Random.nextFloat()
                funnelParticles.add(FunnelParticle(yProgress, h.toFloat()))
            }
        }
    }

    private class FunnelParticle(val yProgress: Float, val totalHeight: Float) {
        val currentY = yProgress * totalHeight

        // Funnel gets wider at top (yProgress = 0) and narrow at ground (yProgress = 1)
        val maxRadius = (1.0f - yProgress) * 120f + 15f
        val radius = Random.nextFloat() * 6f + 3f
        val isDebris = Random.nextFloat() > 0.8f
        private var angle = Random.nextFloat() * (2 * Math.PI).toFloat()
        private val rotSpeed = Random.nextFloat() * 0.15f + 0.08f
        var currentX = 0f

        fun update(centerX: Float) {
            angle += rotSpeed
            currentX = centerX + (cos(angle) * maxRadius)
        }
    }
}
