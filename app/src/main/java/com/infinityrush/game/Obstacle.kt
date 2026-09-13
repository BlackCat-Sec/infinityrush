package com.infinityrush.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
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
        ObstacleType.SUBWAY_TRAIN -> 540f
        ObstacleType.TRAIN_RAMP -> 580f
        else -> 70f
    }

    private val trainFrontPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trainSidePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trainTopPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val windowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0C4A6E") }
    private val windowReflectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(160, 224, 242, 254) }
    private val headlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FEF08A") }
    private val headlightBeamPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val hurdlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barrierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#DC2626") }
    private val rampPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(160, 255, 255, 255)
    }

    private val bumperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F59E0B") }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(90, 0, 0, 0) }

    private val facePath = Path()

    fun update(deltaSeconds: Float, worldSpeed: Float) {
        z -= worldSpeed * deltaSeconds
    }

    fun isOffScreen(): Boolean = z + length < -120f

    fun intersectsPlayer(player: Player): Boolean {
        if (player.invincibilityTimer > 0f) {
            return false
        }

        val inSameLane = (player.targetLane == lane) || (abs(player.currentLanePos - lane) < 0.45f)
        if (!inSameLane) {
            return false
        }

        val inZRange = (player.z >= z - 40f) && (player.z <= z + length + 20f)
        if (!inZRange) {
            return false
        }

        return when (type) {
            ObstacleType.HURDLE -> player.currentYOffset < 48f
            ObstacleType.BARRIER -> !player.isSliding
            ObstacleType.SUBWAY_TRAIN -> player.currentYOffset < 135f
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
        if (z < -length) return

        val focal = Constants.FOCAL_LENGTH
        val zNear = z.coerceAtLeast(10f)
        val zFar = z + length

        val scaleNear = focal / (zNear + focal)
        val scaleFar = focal / (zFar + focal)

        val laneSpacing = viewWidth * 0.38f
        val xCenter = (lane - 1.0f) * laneSpacing

        val xNear = vpX + xCenter * scaleNear
        val xFar = vpX + xCenter * scaleFar

        val yGroundNear = vpY + (groundFrontY - vpY) * scaleNear
        val yGroundFar = vpY + (groundFrontY - vpY) * scaleFar

        val widthWorld = 125f
        val wNear = widthWorld * scaleNear
        val wFar = widthWorld * scaleFar

        when (type) {
            ObstacleType.SUBWAY_TRAIN -> draw3DRealisticTrain(canvas, xNear, yGroundNear, wNear, xFar, yGroundFar, wFar, scaleNear, scaleFar, viewHeight)
            ObstacleType.TRAIN_RAMP -> draw3DRealisticRamp(canvas, xNear, yGroundNear, wNear, xFar, yGroundFar, wFar, viewHeight)
            ObstacleType.HURDLE -> draw3DRealisticHurdle(canvas, xNear, yGroundNear, wNear, scaleNear, viewHeight)
            ObstacleType.BARRIER -> draw3DRealisticBarrier(canvas, xNear, yGroundNear, wNear, scaleNear, viewHeight)
        }
    }

    private fun draw3DRealisticTrain(
        canvas: Canvas,
        xNear: Float, yNear: Float, wNear: Float,
        xFar: Float, yFar: Float, wFar: Float,
        scaleNear: Float, scaleFar: Float, viewHeight: Float
    ) {
        val hNear = viewHeight * 0.35f * scaleNear
        val hFar = viewHeight * 0.35f * scaleFar

        // 0. Headlight Beam on Ground
        val beamLength = wNear * 3.5f
        val beamPath = Path()
        beamPath.moveTo(xNear - wNear * 0.3f, yNear)
        beamPath.lineTo(xNear + wNear * 0.3f, yNear)
        beamPath.lineTo(xNear + wNear * 0.9f, yNear + beamLength)
        beamPath.lineTo(xNear - wNear * 0.9f, yNear + beamLength)
        beamPath.close()

        headlightBeamPaint.shader = LinearGradient(
            xNear, yNear, xNear, yNear + beamLength,
            Color.argb(110, 254, 240, 138),
            Color.argb(0, 254, 240, 138),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(beamPath, headlightBeamPaint)

        // 1. Train Ground Shadow
        val shadowRect = RectF(xNear - wNear * 0.55f, yNear - wNear * 0.1f, xNear + wNear * 0.55f, yNear + wNear * 0.2f)
        canvas.drawOval(shadowRect, shadowPaint)

        // 2. Top Roof Face (Specular Shaded Metallic)
        trainTopPaint.shader = LinearGradient(
            xNear, yNear - hNear, xFar, yFar - hFar,
            Color.parseColor("#38BDF8"), Color.parseColor("#0284C7"),
            Shader.TileMode.CLAMP
        )
        facePath.reset()
        facePath.moveTo(xNear - wNear / 2f, yNear - hNear)
        facePath.lineTo(xNear + wNear / 2f, yNear - hNear)
        facePath.lineTo(xFar + wFar / 2f, yFar - hFar)
        facePath.lineTo(xFar - wFar / 2f, yFar - hFar)
        facePath.close()
        canvas.drawPath(facePath, trainTopPaint)
        canvas.drawPath(facePath, strokePaint)

        // Roof corrugated ridges
        val ridgeCount = 3
        for (r in 1..ridgeCount) {
            val rRatio = r.toFloat() / (ridgeCount + 1)
            val rxNear = xNear - wNear / 2f + wNear * rRatio
            val rxFar = xFar - wFar / 2f + wFar * rRatio
            canvas.drawLine(rxNear, yNear - hNear, rxFar, yFar - hFar, strokePaint)
        }

        // 3. Side Face (Metallic Dark Shaded)
        trainSidePaint.shader = LinearGradient(
            xNear + wNear / 2f, yNear - hNear, xFar + wFar / 2f, yFar,
            Color.parseColor("#0369A1"), Color.parseColor("#075985"),
            Shader.TileMode.CLAMP
        )
        facePath.reset()
        facePath.moveTo(xNear + wNear / 2f, yNear - hNear)
        facePath.lineTo(xFar + wFar / 2f, yFar - hFar)
        facePath.lineTo(xFar + wFar / 2f, yFar)
        facePath.lineTo(xNear + wNear / 2f, yNear)
        facePath.close()
        canvas.drawPath(facePath, trainSidePaint)
        canvas.drawPath(facePath, strokePaint)

        // 4. Front Face (Metallic Gradient)
        trainFrontPaint.shader = LinearGradient(
            xNear - wNear / 2f, yNear - hNear, xNear + wNear / 2f, yNear,
            Color.parseColor("#0284C7"), Color.parseColor("#0369A1"),
            Shader.TileMode.CLAMP
        )
        val frontRect = RectF(xNear - wNear / 2f, yNear - hNear, xNear + wNear / 2f, yNear)
        canvas.drawRoundRect(frontRect, hNear * 0.12f, hNear * 0.12f, trainFrontPaint)
        canvas.drawRoundRect(frontRect, hNear * 0.12f, hNear * 0.12f, strokePaint)

        // Front Windscreen with Metallic Glass Reflection
        val winRect = RectF(frontRect.left + wNear * 0.12f, frontRect.top + hNear * 0.16f, frontRect.right - wNear * 0.12f, frontRect.top + hNear * 0.52f)
        canvas.drawRoundRect(winRect, hNear * 0.08f, hNear * 0.08f, windowPaint)

        facePath.reset()
        facePath.moveTo(winRect.left + winRect.width() * 0.2f, winRect.bottom)
        facePath.lineTo(winRect.right - winRect.width() * 0.1f, winRect.top)
        facePath.lineTo(winRect.right, winRect.top)
        facePath.lineTo(winRect.left + winRect.width() * 0.3f, winRect.bottom)
        facePath.close()
        canvas.drawPath(facePath, windowReflectPaint)

        // Bumper Stripe
        val bumpRect = RectF(frontRect.left + wNear * 0.08f, frontRect.bottom - hNear * 0.22f, frontRect.right - wNear * 0.08f, frontRect.bottom - hNear * 0.10f)
        canvas.drawRoundRect(bumpRect, hNear * 0.04f, hNear * 0.04f, bumperPaint)

        // Halogen Headlights
        val hlRadius = wNear * 0.11f
        canvas.drawCircle(frontRect.left + wNear * 0.22f, frontRect.bottom - hNear * 0.28f, hlRadius, headlightPaint)
        canvas.drawCircle(frontRect.right - wNear * 0.22f, frontRect.bottom - hNear * 0.28f, hlRadius, headlightPaint)
    }

    private fun draw3DRealisticRamp(
        canvas: Canvas,
        xNear: Float, yNear: Float, wNear: Float,
        xFar: Float, yFar: Float, wFar: Float, viewHeight: Float
    ) {
        val hFar = viewHeight * 0.35f * (Constants.FOCAL_LENGTH / (z + length + Constants.FOCAL_LENGTH))

        rampPaint.shader = LinearGradient(
            xNear, yNear, xFar, yFar - hFar,
            Color.parseColor("#10B981"), Color.parseColor("#047857"),
            Shader.TileMode.CLAMP
        )

        facePath.reset()
        facePath.moveTo(xNear - wNear / 2f, yNear)
        facePath.lineTo(xNear + wNear / 2f, yNear)
        facePath.lineTo(xFar + wFar / 2f, yFar - hFar)
        facePath.lineTo(xFar - wFar / 2f, yFar - hFar)
        facePath.close()

        canvas.drawPath(facePath, rampPaint)
        canvas.drawPath(facePath, strokePaint)
    }

    private fun draw3DRealisticHurdle(canvas: Canvas, xNear: Float, yNear: Float, wNear: Float, scaleNear: Float, viewHeight: Float) {
        val hNear = viewHeight * 0.12f * scaleNear

        hurdlePaint.shader = LinearGradient(
            xNear - wNear / 2f, yNear - hNear, xNear + wNear / 2f, yNear,
            Color.parseColor("#F59E0B"), Color.parseColor("#D97706"),
            Shader.TileMode.CLAMP
        )

        val rect = RectF(xNear - wNear / 2f, yNear - hNear, xNear + wNear / 2f, yNear)
        canvas.drawRoundRect(rect, hNear * 0.2f, hNear * 0.2f, hurdlePaint)
        canvas.drawRoundRect(rect, hNear * 0.2f, hNear * 0.2f, strokePaint)
    }

    private fun draw3DRealisticBarrier(canvas: Canvas, xNear: Float, yNear: Float, wNear: Float, scaleNear: Float, viewHeight: Float) {
        val hNear = viewHeight * 0.24f * scaleNear
        val boardH = hNear * 0.45f

        val boardRect = RectF(xNear - wNear / 2f, yNear - hNear, xNear + wNear / 2f, yNear - hNear + boardH)
        canvas.drawRoundRect(boardRect, boardH * 0.2f, boardH * 0.2f, barrierPaint)
        canvas.drawRoundRect(boardRect, boardH * 0.2f, boardH * 0.2f, strokePaint)

        canvas.drawLine(xNear - wNear * 0.38f, yNear - hNear + boardH, xNear - wNear * 0.38f, yNear, strokePaint)
        canvas.drawLine(xNear + wNear * 0.38f, yNear - hNear + boardH, xNear + wNear * 0.38f, yNear, strokePaint)
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
