package com.infinityrush.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.sin
import kotlin.random.Random

enum class ObstacleType {
    BLOCK,
    SPIKE,
    MOVING_BARRIER
}

class Obstacle private constructor(
    private val type: ObstacleType,
    var x: Float,
    private var y: Float,
    private val width: Float,
    private val height: Float,
    private val baseY: Float,
    private val amplitude: Float,
    private val oscillationSpeed: Float,
    private var phase: Float
) {
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = when (type) {
            ObstacleType.BLOCK -> Color.parseColor("#D64550")
            ObstacleType.SPIKE -> Color.parseColor("#FF6B6B")
            ObstacleType.MOVING_BARRIER -> Color.parseColor("#7C3AED")
        }
    }
    private val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = when (type) {
            ObstacleType.BLOCK -> Color.parseColor("#FDE68A")
            ObstacleType.SPIKE -> Color.parseColor("#FFD9D9")
            ObstacleType.MOVING_BARRIER -> Color.parseColor("#E9D5FF")
        }
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(45, 0, 0, 0)
    }

    private val tempBounds = RectF()
    private val spikePath = Path()

    fun update(deltaSeconds: Float, worldSpeed: Float) {
        x -= worldSpeed * deltaSeconds
        if (type == ObstacleType.MOVING_BARRIER) {
            phase += oscillationSpeed * deltaSeconds
            y = baseY + sin(phase) * amplitude
        }
    }

    fun right(): Float = x + width

    fun isOffScreen(): Boolean = right() < -width

    fun intersects(playerBounds: RectF): Boolean {
        getCollisionBounds(tempBounds)
        return RectF.intersects(playerBounds, tempBounds)
    }

    fun draw(canvas: Canvas) {
        canvas.drawOval(
            x + width * 0.1f,
            y + height * 0.88f,
            x + width * 0.9f,
            y + height * 1.12f,
            shadowPaint
        )
        when (type) {
            ObstacleType.BLOCK -> drawBlock(canvas)
            ObstacleType.SPIKE -> drawSpike(canvas)
            ObstacleType.MOVING_BARRIER -> drawMovingBarrier(canvas)
        }
    }

    private fun drawBlock(canvas: Canvas) {
        val blockRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(blockRect, width * 0.18f, width * 0.18f, bodyPaint)

        val inset = width * 0.16f
        val inner = RectF(
            blockRect.left + inset,
            blockRect.top + inset,
            blockRect.right - inset,
            blockRect.bottom - inset
        )
        canvas.drawRoundRect(inner, width * 0.1f, width * 0.1f, detailPaint)
    }

    private fun drawSpike(canvas: Canvas) {
        spikePath.reset()
        spikePath.moveTo(x, y + height)
        spikePath.lineTo(x + width * 0.33f, y + height * 0.28f)
        spikePath.lineTo(x + width * 0.66f, y + height)
        spikePath.lineTo(x + width, y + height * 0.24f)
        spikePath.lineTo(x + width, y + height)
        spikePath.close()
        canvas.drawPath(spikePath, bodyPaint)

        canvas.drawRect(
            x,
            y + height * 0.78f,
            x + width,
            y + height,
            detailPaint
        )
    }

    private fun drawMovingBarrier(canvas: Canvas) {
        val barrierRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(barrierRect, height * 0.5f, height * 0.5f, bodyPaint)
        canvas.drawCircle(x + width * 0.08f, y + height / 2f, height * 0.52f, detailPaint)
        canvas.drawCircle(x + width * 0.92f, y + height / 2f, height * 0.52f, detailPaint)
    }

    private fun getCollisionBounds(outRect: RectF) {
        when (type) {
            ObstacleType.BLOCK -> outRect.set(
                x + width * 0.08f,
                y + height * 0.06f,
                x + width * 0.92f,
                y + height
            )

            ObstacleType.SPIKE -> outRect.set(
                x + width * 0.08f,
                y + height * 0.22f,
                x + width * 0.92f,
                y + height
            )

            ObstacleType.MOVING_BARRIER -> outRect.set(
                x,
                y,
                x + width,
                y + height
            )
        }
    }

    companion object {
        fun createRandom(
            spawnX: Float,
            groundTop: Float,
            viewHeight: Int,
            difficultyLevel: Int,
            random: Random
        ): Obstacle {
            val availableTypes = mutableListOf(ObstacleType.BLOCK, ObstacleType.SPIKE)
            if (difficultyLevel >= 1) {
                availableTypes += ObstacleType.BLOCK
            }
            if (difficultyLevel >= 2) {
                availableTypes += ObstacleType.MOVING_BARRIER
            }

            return when (availableTypes.random(random)) {
                ObstacleType.BLOCK -> createBlock(spawnX, groundTop, viewHeight)
                ObstacleType.SPIKE -> createSpike(spawnX, groundTop, viewHeight)
                ObstacleType.MOVING_BARRIER -> createMovingBarrier(spawnX, groundTop, viewHeight, random)
            }
        }

        private fun createBlock(spawnX: Float, groundTop: Float, viewHeight: Int): Obstacle {
            val width = viewHeight * Constants.BLOCK_WIDTH_RATIO
            val height = viewHeight * Constants.BLOCK_HEIGHT_RATIO
            val y = groundTop - height
            return Obstacle(
                type = ObstacleType.BLOCK,
                x = spawnX,
                y = y,
                width = width,
                height = height,
                baseY = y,
                amplitude = 0f,
                oscillationSpeed = 0f,
                phase = 0f
            )
        }

        private fun createSpike(spawnX: Float, groundTop: Float, viewHeight: Int): Obstacle {
            val width = viewHeight * Constants.SPIKE_WIDTH_RATIO
            val height = viewHeight * Constants.SPIKE_HEIGHT_RATIO
            val y = groundTop - height
            return Obstacle(
                type = ObstacleType.SPIKE,
                x = spawnX,
                y = y,
                width = width,
                height = height,
                baseY = y,
                amplitude = 0f,
                oscillationSpeed = 0f,
                phase = 0f
            )
        }

        private fun createMovingBarrier(
            spawnX: Float,
            groundTop: Float,
            viewHeight: Int,
            random: Random
        ): Obstacle {
            val width = viewHeight * Constants.MOVING_BARRIER_WIDTH_RATIO
            val height = viewHeight * Constants.MOVING_BARRIER_HEIGHT_RATIO
            val hoverHeight = viewHeight * 0.16f
            val baseY = groundTop - hoverHeight - height
            val amplitude = viewHeight * 0.07f

            return Obstacle(
                type = ObstacleType.MOVING_BARRIER,
                x = spawnX,
                y = baseY,
                width = width,
                height = height,
                baseY = baseY,
                amplitude = amplitude,
                oscillationSpeed = 3.2f + random.nextFloat() * 1.8f,
                phase = random.nextFloat() * (Math.PI.toFloat() * 2f)
            )
        }
    }
}

