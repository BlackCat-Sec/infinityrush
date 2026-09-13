package com.infinityrush.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

class Particle(
    private var x: Float,
    private var y: Float,
    private var velocityX: Float,
    private var velocityY: Float,
    private val radius: Float,
    private val color: Int,
    private var lifeSeconds: Float,
    val text: String? = null
) {
    private val maxLifeSeconds = lifeSeconds
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    val isAlive: Boolean
        get() = lifeSeconds > 0f

    fun update(deltaSeconds: Float) {
        lifeSeconds -= deltaSeconds
        if (lifeSeconds <= 0f) {
            return
        }

        x += velocityX * deltaSeconds
        y += velocityY * deltaSeconds
        if (text == null) {
            velocityY += Constants.PARTICLE_GRAVITY * deltaSeconds
            velocityX *= (1f - Constants.PARTICLE_DRAG * deltaSeconds).coerceAtLeast(0.25f)
        } else {
            velocityY *= 0.94f
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (!isAlive) {
            return
        }

        val progress = (lifeSeconds / maxLifeSeconds).coerceIn(0f, 1f)
        val alpha = (progress * 255).toInt().coerceIn(0, 255)

        if (text != null) {
            textPaint.color = color
            textPaint.alpha = alpha
            textPaint.textSize = radius * (1.2f - (1f - progress) * 0.3f)
            canvas.drawText(text, x, y, textPaint)
        } else {
            paint.color = color
            paint.alpha = alpha
            canvas.drawCircle(x, y, radius * (0.65f + 0.35f * progress), paint)
        }
    }
}
