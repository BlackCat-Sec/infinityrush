package com.infinityrush.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class CollectibleType {
    COIN,
    HOVERBOARD,
    JETPACK,
    MAGNET,
    MULTIPLIER
}

class Collectible(
    val type: CollectibleType,
    val lane: Int,
    var z: Float,
    val yOffset: Float = 0f,
    private val phaseOffset: Float = 0f
) {
    private var animationPhase = phaseOffset
    private var currentLanePos: Float = lane.toFloat()

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = when (type) {
            CollectibleType.COIN -> Color.parseColor("#F59E0B")
            CollectibleType.HOVERBOARD -> Color.parseColor("#06B6D4")
            CollectibleType.JETPACK -> Color.parseColor("#84CC16")
            CollectibleType.MAGNET -> Color.parseColor("#EF4444")
            CollectibleType.MULTIPLIER -> Color.parseColor("#A855F7")
        }
    }
    private val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = when (type) {
            CollectibleType.COIN -> Color.parseColor("#FDE68A")
            CollectibleType.HOVERBOARD -> Color.parseColor("#CFFAFE")
            CollectibleType.JETPACK -> Color.parseColor("#ECFCCB")
            CollectibleType.MAGNET -> Color.parseColor("#F87171")
            CollectibleType.MULTIPLIER -> Color.parseColor("#F3E8FF")
        }
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = when (type) {
            CollectibleType.COIN -> Color.argb(90, 245, 158, 11)
            CollectibleType.HOVERBOARD -> Color.argb(100, 6, 182, 212)
            CollectibleType.JETPACK -> Color.argb(100, 132, 204, 22)
            CollectibleType.MAGNET -> Color.argb(100, 239, 68, 68)
            CollectibleType.MULTIPLIER -> Color.argb(100, 168, 85, 247)
        }
    }
    private val infinityMarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78350F")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    fun update(deltaSeconds: Float, worldSpeed: Float) {
        z -= worldSpeed * deltaSeconds
        animationPhase += deltaSeconds * 4.5f
    }

    fun attractTowards(targetLanePos: Float, targetZ: Float, deltaSeconds: Float) {
        val pullSpeed = 1200f
        val diffLane = targetLanePos - currentLanePos
        currentLanePos += diffLane * (deltaSeconds * 12f).coerceAtMost(1f)

        if (z > targetZ) {
            z -= pullSpeed * deltaSeconds
            if (z < targetZ) z = targetZ
        }
    }

    fun isOffScreen(): Boolean = z < -80f

    fun intersectsPlayer(player: Player): Boolean {
        val sameLane = abs(player.currentLanePos - currentLanePos) < 0.55f
        val sameZ = abs(player.z - z) < 60f
        val sameY = abs(player.currentYOffset - yOffset) < 120f
        return sameLane && sameZ && sameY
    }

    fun draw3D(
        canvas: Canvas,
        vpX: Float,
        vpY: Float,
        groundFrontY: Float,
        viewWidth: Float,
        viewHeight: Float
    ) {
        if (z < 0f) {
            return
        }

        val scale = Constants.FOCAL_LENGTH / (z.coerceAtLeast(10f) + Constants.FOCAL_LENGTH)
        val laneSpacing = viewWidth * 0.38f
        val screenX = vpX + (currentLanePos - 1.0f) * laneSpacing * scale
        val floatY = sin(animationPhase) * 15f
        val screenY = vpY + (groundFrontY - vpY) * scale - (yOffset + floatY) * scale

        val radius = viewHeight * 0.045f * scale
        canvas.drawCircle(screenX, screenY, radius * 1.35f, glowPaint)

        when (type) {
            CollectibleType.COIN -> drawInfinityCoin(canvas, screenX, screenY, radius)
            CollectibleType.HOVERBOARD -> drawTextIcon(canvas, screenX, screenY, radius, "🛹")
            CollectibleType.JETPACK -> drawTextIcon(canvas, screenX, screenY, radius, "🚀")
            CollectibleType.MAGNET -> drawTextIcon(canvas, screenX, screenY, radius, "U")
            CollectibleType.MULTIPLIER -> drawTextIcon(canvas, screenX, screenY, radius, "2X")
        }
    }

    private fun drawInfinityCoin(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val scaleX = abs(cos(animationPhase * 0.8f)).coerceAtLeast(0.2f)
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(scaleX, 1f)

        canvas.drawCircle(0f, 0f, radius, bodyPaint)
        canvas.drawCircle(0f, 0f, radius * 0.7f, detailPaint)

        infinityMarkPaint.textSize = radius * 0.85f
        val baseline = - (infinityMarkPaint.descent() + infinityMarkPaint.ascent()) / 2f
        canvas.drawText("∞", 0f, baseline, infinityMarkPaint)

        canvas.restore()
    }

    private fun drawTextIcon(canvas: Canvas, cx: Float, cy: Float, radius: Float, label: String) {
        canvas.drawCircle(cx, cy, radius, bodyPaint)
        canvas.drawCircle(cx, cy, radius * 0.7f, detailPaint)

        textPaint.textSize = radius * 0.85f
        textPaint.color = Color.WHITE
        val baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(label, cx, baseline, textPaint)
    }
}
