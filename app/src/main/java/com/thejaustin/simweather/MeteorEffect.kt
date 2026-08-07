package com.thejaustin.simweather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * SimCity style disaster effect: Meteor shower streaks falling diagonally.
 */
class MeteorEffect(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val meteors = mutableListOf<Meteor>()
    private val meteorShader =
        LinearGradient(
            0f,
            0f,
            -100f,
            -100f,
            intArrayOf(Color.YELLOW, Color.RED, Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP,
        )

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 6f
            style = Paint.Style.STROKE
            shader = meteorShader
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        meteors.forEach { meteor ->
            meteor.move()

            canvas.save()
            canvas.translate(meteor.x, meteor.y)
            val scale = meteor.length / 100f
            canvas.scale(scale, scale)

            canvas.drawLine(
                0f,
                0f,
                -100f,
                -100f,
                paint,
            )
            canvas.restore()

            if (meteor.y > height || meteor.x > width) {
                meteor.reset(width, height)
            }
        }
        if (isShown) invalidate()
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int,
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        meteors.clear()
        if (w > 0 && h > 0) {
            for (i in 0..12) {
                meteors.add(Meteor().apply { reset(w, h) })
            }
        }
    }

    private class Meteor {
        var x = 0f
        var y = 0f
        var speed = 20f
        var length = 80f

        fun reset(
            w: Int,
            h: Int,
        ) {
            x = Random.nextInt(-200, w).toFloat()
            y = Random.nextInt(-300, -50).toFloat()
            speed = Random.nextFloat() * 15f + 15f
            length = Random.nextFloat() * 60f + 60f
        }

        fun move() {
            x += speed
            y += speed
        }
    }
}
