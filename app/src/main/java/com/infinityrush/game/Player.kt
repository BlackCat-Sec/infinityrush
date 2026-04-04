package com.infinityrush.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.sin

class Player(
    private val startX: Float,
    private val width: Float,
    private val standHeight: Float,
    private val slideHeight: Float,
    private var floorTop: Float
) {
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#102A43") }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#24C8DB") }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EAF9FF") }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(55, 5, 17, 36)
    }

    private var currentHeight = standHeight
    private var y = floorTop - standHeight
    private var verticalVelocity = 0f
    private var slideTimer = 0f
    private var animationTime = 0f

    val x: Float = startX

    private val drawingBounds = RectF()

    private val isAirborne: Boolean
        get() = y + currentHeight < floorTop - 1f || verticalVelocity < 0f

    fun reset(newFloorTop: Float = floorTop) {
        floorTop = newFloorTop
        currentHeight = standHeight
        y = floorTop - standHeight
        verticalVelocity = 0f
        slideTimer = 0f
        animationTime = 0f
    }

    fun jump(): Boolean {
        if (isAirborne) {
            return false
        }

        slideTimer = 0f
        currentHeight = standHeight
        y = floorTop - currentHeight
        verticalVelocity = Constants.JUMP_VELOCITY
        return true
    }

    fun slide(): Boolean {
        if (isAirborne || slideTimer > 0f) {
            return false
        }

        currentHeight = slideHeight
        y = floorTop - currentHeight
        slideTimer = Constants.SLIDE_DURATION_SECONDS
        return true
    }

    fun advanceAnimation(deltaSeconds: Float) {
        animationTime += deltaSeconds * if (isAirborne) 4f else 10f
    }

    fun update(deltaSeconds: Float) {
        advanceAnimation(deltaSeconds)

        if (slideTimer > 0f) {
            slideTimer -= deltaSeconds
            if (slideTimer <= 0f) {
                currentHeight = standHeight
                y = floorTop - currentHeight
            }
        }

        if (isAirborne) {
            verticalVelocity += Constants.GRAVITY * deltaSeconds
            y += verticalVelocity * deltaSeconds
        }

        if (y + currentHeight >= floorTop) {
            y = floorTop - currentHeight
            verticalVelocity = 0f
        }
    }

    fun getCollisionBounds(outRect: RectF) {
        outRect.set(
            x + width * 0.12f,
            y + currentHeight * 0.08f,
            x + width * 0.88f,
            y + currentHeight * 0.96f
        )
    }

    fun draw(canvas: Canvas) {
        drawingBounds.set(x, y, x + width, y + currentHeight)
        val shadowHeight = currentHeight * 0.18f
        canvas.drawOval(
            x + width * 0.1f,
            floorTop - shadowHeight * 0.7f,
            x + width * 0.9f,
            floorTop + shadowHeight * 0.3f,
            shadowPaint
        )

        if (slideTimer > 0f) {
            drawSliding(canvas)
        } else {
            drawRunning(canvas)
        }
    }

    private fun drawRunning(canvas: Canvas) {
        val headRadius = width * 0.22f
        val headCx = x + width * 0.5f
        val headCy = y + headRadius * 1.15f
        val torso = RectF(
            x + width * 0.22f,
            y + currentHeight * 0.32f,
            x + width * 0.78f,
            y + currentHeight
        )

        canvas.drawCircle(headCx, headCy, headRadius, highlightPaint)
        canvas.drawRoundRect(torso, width * 0.18f, width * 0.18f, bodyPaint)

        val accent = RectF(
            x + width * 0.34f,
            y + currentHeight * 0.44f,
            x + width * 0.66f,
            y + currentHeight * 0.6f
        )
        canvas.drawRoundRect(accent, width * 0.08f, width * 0.08f, accentPaint)

        val stride = sin(animationTime * 1.6f) * width * 0.12f
        val legTop = y + currentHeight * 0.72f
        val leftLeg = RectF(
            x + width * 0.28f + stride * 0.2f,
            legTop,
            x + width * 0.44f + stride,
            y + currentHeight
        )
        val rightLeg = RectF(
            x + width * 0.56f - stride,
            legTop,
            x + width * 0.72f - stride * 0.2f,
            y + currentHeight
        )
        canvas.drawRoundRect(leftLeg, width * 0.08f, width * 0.08f, bodyPaint)
        canvas.drawRoundRect(rightLeg, width * 0.08f, width * 0.08f, bodyPaint)
    }

    private fun drawSliding(canvas: Canvas) {
        val slideBody = RectF(
            x,
            y + currentHeight * 0.18f,
            x + width,
            y + currentHeight
        )
        canvas.drawRoundRect(slideBody, currentHeight * 0.5f, currentHeight * 0.5f, bodyPaint)

        val accent = RectF(
            x + width * 0.22f,
            y + currentHeight * 0.34f,
            x + width * 0.78f,
            y + currentHeight * 0.56f
        )
        canvas.drawRoundRect(accent, currentHeight * 0.2f, currentHeight * 0.2f, accentPaint)

        canvas.drawCircle(
            x + width * 0.22f,
            y + currentHeight * 0.4f,
            currentHeight * 0.22f,
            highlightPaint
        )
    }
}

