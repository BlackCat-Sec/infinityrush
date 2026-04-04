package com.infinityrush.game

import android.graphics.Canvas
import android.graphics.Paint

class Particle(
    private var x: Float,
    private var y: Float,
    private var velocityX: Float,
    private var velocityY: Float,
    private val radius: Float,
    private val color: Int,
    private var lifeSeconds: Float
) {
    private val maxLifeSeconds = lifeSeconds

    val isAlive: Boolean
        get() = lifeSeconds > 0f

    fun update(deltaSeconds: Float) {
        lifeSeconds -= deltaSeconds
        if (lifeSeconds <= 0f) {
            return
        }

        x += velocityX * deltaSeconds
        y += velocityY * deltaSeconds
        velocityY += Constants.PARTICLE_GRAVITY * deltaSeconds
        velocityX *= (1f - Constants.PARTICLE_DRAG * deltaSeconds).coerceAtLeast(0.25f)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (!isAlive) {
            return
        }

        val alpha = ((lifeSeconds / maxLifeSeconds) * 255).toInt().coerceIn(0, 255)
        paint.color = color
        paint.alpha = alpha
        canvas.drawCircle(x, y, radius * (0.65f + 0.35f * (lifeSeconds / maxLifeSeconds)), paint)
    }
}
