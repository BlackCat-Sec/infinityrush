package com.infinityrush.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.abs
import kotlin.random.Random

enum class ObstacleType {
    SUBWAY_TRAIN,
    HURDLE,
    BARRIER,
    TRAIN_RAMP
}

class Obstacle private constructor(
    val type: ObstacleType,
    val lane: Int,
    var z: Float
) {
    val length: Float = when (type) {
        ObstacleType.SUBWAY_TRAIN -> 480f
        ObstacleType.TRAIN_RAMP -> 520f
        else -> 60f
    }

    private val trainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0284C7") }
    private val trainAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EA580C") }
    private val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#38BDF8") }
    private val headlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FDE047") }

    private val hurdlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F59E0B") }
    private val barrierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EF4444") }
    private val rampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#10B981") }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
    }

    private val rampPath = Path()

    fun update(deltaSeconds: Float, worldSpeed: Float) {
        z -= worldSpeed * deltaSeconds
    }

    fun isOffScreen(): Boolean = z + length < -100f

    fun intersectsPlayer(player: Player): Boolean {
        if (player.invincibilityTimer > 0f) {
            return false
        }

        val inSameLane = (player.targetLane == lane) || (abs(player.currentLanePos - lane) < 0.45f)
        if (!inSameLane) {
            return false
        }

        val inZRange = (player.z >= z - 30f) && (player.z <= z + length + 20f)
        if (!inZRange) {
            return false
        }

        return when (type) {
            ObstacleType.HURDLE -> player.currentYOffset < 45f
            ObstacleType.BARRIER -> !player.isSliding
            ObstacleType.SUBWAY_TRAIN -> player.currentYOffset < 130f
            ObstacleType.TRAIN_RAMP -> false
        }
    }

    fun draw3D(
        canvas: Canvas,
        vpX: Float,
        vpY: Float,
        groundFrontY: Float,
        viewWidth: Float,
        viewHeight: Float
    ) {
        if (z < -length) {
            return
        }

        val scale = Constants.FOCAL_LENGTH / (z.coerceAtLeast(10f) + Constants.FOCAL_LENGTH)
        val laneSpacing = viewWidth * 0.38f
        val screenX = vpX + (lane - 1.0f) * laneSpacing * scale
        val screenY = vpY + (groundFrontY - vpY) * scale

        val widthOnScreen = viewWidth * 0.28f * scale

        when (type) {
            ObstacleType.SUBWAY_TRAIN -> drawTrain(canvas, screenX, screenY, widthOnScreen, viewHeight * 0.32f * scale)
            ObstacleType.TRAIN_RAMP -> drawRamp(canvas, screenX, screenY, widthOnScreen, viewHeight * 0.32f * scale)
            ObstacleType.HURDLE -> drawHurdle(canvas, screenX, screenY, widthOnScreen, viewHeight * 0.12f * scale)
            ObstacleType.BARRIER -> drawBarrier(canvas, screenX, screenY, widthOnScreen, viewHeight * 0.22f * scale)
        }
    }

    private fun drawTrain(canvas: Canvas, cx: Float, cy: Float, width: Float, height: Float) {
        val rect = RectF(cx - width / 2f, cy - height, cx + width / 2f, cy)
        canvas.drawRoundRect(rect, height * 0.15f, height * 0.15f, trainPaint)
        canvas.drawRoundRect(rect, height * 0.15f, height * 0.15f, strokePaint)

        val windowRect = RectF(rect.left + width * 0.15f, rect.top + height * 0.2f, rect.right - width * 0.15f, rect.top + height * 0.5f)
        canvas.drawRoundRect(windowRect, height * 0.08f, height * 0.08f, windowPaint)

        val hlRadius = width * 0.12f
        canvas.drawCircle(rect.left + width * 0.25f, rect.bottom - height * 0.2f, hlRadius, headlightPaint)
        canvas.drawCircle(rect.right - width * 0.25f, rect.bottom - height * 0.2f, hlRadius, headlightPaint)
    }

    private fun drawRamp(canvas: Canvas, cx: Float, cy: Float, width: Float, height: Float) {
        rampPath.reset()
        rampPath.moveTo(cx - width / 2f, cy)
        rampPath.lineTo(cx + width / 2f, cy)
        rampPath.lineTo(cx + width * 0.4f, cy - height)
        rampPath.lineTo(cx - width * 0.4f, cy - height)
        rampPath.close()

        canvas.drawPath(rampPath, rampPaint)
        canvas.drawPath(rampPath, strokePaint)
    }

    private fun drawHurdle(canvas: Canvas, cx: Float, cy: Float, width: Float, height: Float) {
        val rect = RectF(cx - width / 2f, cy - height, cx + width / 2f, cy)
        canvas.drawRoundRect(rect, height * 0.2f, height * 0.2f, hurdlePaint)
        canvas.drawRoundRect(rect, height * 0.2f, height * 0.2f, strokePaint)
    }

    private fun drawBarrier(canvas: Canvas, cx: Float, cy: Float, width: Float, height: Float) {
        val boardH = height * 0.45f
        val rect = RectF(cx - width / 2f, cy - height, cx + width / 2f, cy - height + boardH)
        canvas.drawRoundRect(rect, boardH * 0.2f, boardH * 0.2f, barrierPaint)

        canvas.drawLine(cx - width * 0.4f, cy - height + boardH, cx - width * 0.4f, cy, strokePaint)
        canvas.drawLine(cx + width * 0.4f, cy - height + boardH, cx + width * 0.4f, cy, strokePaint)
    }

    companion object {
        fun createRandom(random: Random, difficultyLevel: Int): Obstacle {
            val lane = random.nextInt(Constants.LANE_COUNT)
            val type = when {
                difficultyLevel == 0 -> if (random.nextBoolean()) ObstacleType.HURDLE else ObstacleType.BARRIER
                else -> when (random.nextInt(4)) {
                    0 -> ObstacleType.HURDLE
                    1 -> ObstacleType.BARRIER
                    2 -> ObstacleType.SUBWAY_TRAIN
                    else -> ObstacleType.TRAIN_RAMP
                }
            }
            return Obstacle(type, lane, Constants.SPAWN_Z)
        }
    }
}
