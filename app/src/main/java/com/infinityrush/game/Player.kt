package com.infinityrush.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.exp
import kotlin.math.sin

data class PlayerFrameEvents(
    val jumped: Boolean = false,
    val landed: Boolean = false,
    val slideStarted: Boolean = false,
    val fastDropped: Boolean = false,
    val laneSwitched: Boolean = false
)

class Player {
    var targetLane: Int = Constants.LANE_CENTER
        private set
    var currentLanePos: Float = 1.0f
        private set

    val z: Float = Constants.PLAYER_Z

    private var yOffset: Float = 0f
    private var verticalVelocity = 0f
    private var slideTimer = 0f
    private var animationTime = 0f
    private var coyoteTimer = 0f
    private var jumpBufferTimer = 0f
    private var slideBufferTimer = 0f

    var squashScaleX: Float = 1.0f
        private set
    var squashScaleY: Float = 1.0f
        private set

    var cameraTiltAngle: Float = 0f
        private set

    var hasHoverboard: Boolean = false
    var hoverboardTimer: Float = 0f

    var hasJetpack: Boolean = false
    var jetpackTimer: Float = 0f

    var invincibilityTimer: Float = 0f
        private set

    var magnetTimer: Float = 0f
    var multiplierTimer: Float = 0f

    private var isFastDropping = false

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0284C7") }
    private val hoodiePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#DC2626") }
    private val visorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#38BDF8") }
    private val shoePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(100, 0, 0, 0) }

    private val hoverboardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hoverboardGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(180, 245, 158, 11) }

    private val jetpackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#65A30D") }
    private val jetFlamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F97316") }

    private val magnetAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(160, 239, 68, 68)
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val multiplierAuraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(160, 168, 85, 247)
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    fun reset() {
        targetLane = Constants.LANE_CENTER
        currentLanePos = 1.0f
        yOffset = 0f
        verticalVelocity = 0f
        slideTimer = 0f
        animationTime = 0f
        coyoteTimer = Constants.COYOTE_TIME_SECONDS
        jumpBufferTimer = 0f
        slideBufferTimer = 0f
        squashScaleX = 1.0f
        squashScaleY = 1.0f
        cameraTiltAngle = 0f
        hasHoverboard = false
        hoverboardTimer = 0f
        hasJetpack = false
        jetpackTimer = 0f
        invincibilityTimer = 0f
        magnetTimer = 0f
        multiplierTimer = 0f
        isFastDropping = false
    }

    fun moveLeft(): Boolean {
        if (targetLane > Constants.LANE_LEFT) {
            targetLane--
            return true
        }
        return false
    }

    fun moveRight(): Boolean {
        if (targetLane < Constants.LANE_RIGHT) {
            targetLane++
            return true
        }
        return false
    }

    fun queueJump() {
        jumpBufferTimer = Constants.JUMP_BUFFER_SECONDS
    }

    fun releaseJump() {
        if (verticalVelocity < Constants.MIN_JUMP_VELOCITY && !hasJetpack) {
            verticalVelocity = Constants.MIN_JUMP_VELOCITY
        }
    }

    fun queueSlide() {
        slideBufferTimer = Constants.SLIDE_BUFFER_SECONDS
        if (!isGrounded && !isFastDropping && !hasJetpack) {
            isFastDropping = true
            verticalVelocity = Constants.FAST_DROP_VELOCITY
        }
    }

    fun activateHoverboard() {
        hasHoverboard = true
        hoverboardTimer = Constants.HOVERBOARD_DURATION_SECONDS
    }

    fun triggerCrashProtection(): Boolean {
        if (hasHoverboard) {
            hasHoverboard = false
            hoverboardTimer = 0f
            invincibilityTimer = Constants.INVINCIBILITY_DURATION_SECONDS
            return true
        }
        return false
    }

    fun isInvincible(): Boolean = invincibilityTimer > 0f

    val isGrounded: Boolean
        get() = yOffset <= 1f && verticalVelocity >= 0f

    val currentYOffset: Float
        get() = yOffset

    val isSliding: Boolean
        get() = slideTimer > 0f

    fun update(deltaSeconds: Float): PlayerFrameEvents {
        var jumped = false
        var landed = false
        var slideStarted = false
        var fastDropped = false

        animationTime += deltaSeconds * 12f

        if (invincibilityTimer > 0f) {
            invincibilityTimer = (invincibilityTimer - deltaSeconds).coerceAtLeast(0f)
        }
        if (magnetTimer > 0f) {
            magnetTimer = (magnetTimer - deltaSeconds).coerceAtLeast(0f)
        }
        if (multiplierTimer > 0f) {
            multiplierTimer = (multiplierTimer - deltaSeconds).coerceAtLeast(0f)
        }
        if (hoverboardTimer > 0f) {
            hoverboardTimer -= deltaSeconds
            if (hoverboardTimer <= 0f) {
                hasHoverboard = false
            }
        }
        if (jetpackTimer > 0f) {
            jetpackTimer -= deltaSeconds
            if (jetpackTimer <= 0f) {
                hasJetpack = false
            }
        }

        val blendFactor = (1f - exp(-22.0 * deltaSeconds)).toFloat()
        currentLanePos += (targetLane.toFloat() - currentLanePos) * blendFactor

        val targetTilt = (currentLanePos - targetLane.toFloat()) * -4.0f
        cameraTiltAngle += (targetTilt - cameraTiltAngle) * blendFactor

        squashScaleX += (1.0f - squashScaleX) * (1f - exp(-14.0 * deltaSeconds)).toFloat()
        squashScaleY += (1.0f - squashScaleY) * (1f - exp(-14.0 * deltaSeconds)).toFloat()

        jumpBufferTimer = (jumpBufferTimer - deltaSeconds).coerceAtLeast(0f)
        slideBufferTimer = (slideBufferTimer - deltaSeconds).coerceAtLeast(0f)

        if (slideTimer > 0f) {
            slideTimer -= deltaSeconds
        }

        if (hasJetpack) {
            val flyTargetY = 280f
            yOffset += (flyTargetY - yOffset) * (deltaSeconds * 6f)
            verticalVelocity = 0f
        } else {
            coyoteTimer = if (isGrounded) Constants.COYOTE_TIME_SECONDS else (coyoteTimer - deltaSeconds).coerceAtLeast(0f)

            val groundedAtStart = isGrounded
            if (!groundedAtStart || verticalVelocity < 0f) {
                val g = if (isFastDropping) Constants.GRAVITY * 1.8f else Constants.GRAVITY
                verticalVelocity += g * deltaSeconds
                yOffset -= verticalVelocity * deltaSeconds
            }

            if (yOffset <= 0f) {
                if (!groundedAtStart) {
                    landed = true
                    squashScaleX = 1.25f
                    squashScaleY = 0.78f
                    if (isFastDropping) {
                        fastDropped = true
                        isFastDropping = false
                        slideBufferTimer = Constants.SLIDE_BUFFER_SECONDS
                    }
                }
                yOffset = 0f
                verticalVelocity = 0f
            }

            if (jumpBufferTimer > 0f && (isGrounded || coyoteTimer > 0f)) {
                slideTimer = 0f
                isFastDropping = false
                verticalVelocity = Constants.JUMP_VELOCITY
                coyoteTimer = 0f
                jumpBufferTimer = 0f
                jumped = true
                squashScaleX = 0.82f
                squashScaleY = 1.25f
            } else if (slideBufferTimer > 0f && isGrounded && slideTimer <= 0f) {
                slideTimer = Constants.SLIDE_DURATION_SECONDS
                slideBufferTimer = 0f
                slideStarted = true
            }
        }

        return PlayerFrameEvents(
            jumped = jumped,
            landed = landed,
            slideStarted = slideStarted,
            fastDropped = fastDropped,
            laneSwitched = false
        )
    }

    fun draw3D(
        canvas: Canvas,
        vpX: Float,
        vpY: Float,
        groundFrontY: Float,
        viewWidth: Float,
        viewHeight: Float
    ) {
        if (isInvincible() && (invincibilityTimer * 16).toInt() % 2 == 0) {
            return
        }

        val scale = Constants.FOCAL_LENGTH / (z + Constants.FOCAL_LENGTH)
        val laneSpacing = viewWidth * 0.38f
        val screenX = vpX + (currentLanePos - 1.0f) * laneSpacing * scale
        val screenY = vpY + (groundFrontY - vpY) * scale - yOffset * scale

        val charWidth = viewHeight * 0.16f * scale * squashScaleX
        val charHeight = if (isSliding) viewHeight * 0.14f * scale * squashScaleY else viewHeight * 0.28f * scale * squashScaleY

        // 1. Soft Dynamic Shadow on Ground
        val shadowW = charWidth * (0.95f - (yOffset / 300f) * 0.3f).coerceAtLeast(0.4f)
        val shadowH = charWidth * 0.35f
        val shadowY = vpY + (groundFrontY - vpY) * scale
        canvas.drawOval(
            screenX - shadowW / 2f,
            shadowY - shadowH / 2f,
            screenX + shadowW / 2f,
            shadowY + shadowH / 2f,
            shadowPaint
        )

        // 2. Hoverboard
        if (hasHoverboard) {
            val hbW = charWidth * 1.55f
            val hbH = charHeight * 0.18f
            val hbY = screenY - hbH * 0.5f

            hoverboardPaint.shader = LinearGradient(
                screenX - hbW / 2f, hbY, screenX + hbW / 2f, hbY + hbH,
                Color.parseColor("#F59E0B"), Color.parseColor("#D97706"),
                Shader.TileMode.CLAMP
            )

            canvas.drawRoundRect(
                screenX - hbW / 2f, hbY, screenX + hbW / 2f, hbY + hbH,
                hbH * 0.5f, hbH * 0.5f, hoverboardPaint
            )
            canvas.drawCircle(screenX, hbY + hbH / 2f, hbH * 0.8f, hoverboardGlowPaint)
        }

        // 3. Jetpack
        if (hasJetpack) {
            val jpW = charWidth * 0.4f
            canvas.drawRect(screenX - jpW * 1.2f, screenY - charHeight * 0.7f, screenX - jpW * 0.2f, screenY - charHeight * 0.2f, jetpackPaint)
            canvas.drawRect(screenX + jpW * 0.2f, screenY - charHeight * 0.7f, screenX + jpW * 1.2f, screenY - charHeight * 0.2f, jetpackPaint)
            canvas.drawCircle(screenX - jpW * 0.7f, screenY - charHeight * 0.1f, jpW * 0.45f, jetFlamePaint)
            canvas.drawCircle(screenX + jpW * 0.7f, screenY - charHeight * 0.1f, jpW * 0.45f, jetFlamePaint)
        }

        // 4. Powerup Aura Rings
        if (magnetTimer > 0f) {
            canvas.drawCircle(screenX, screenY - charHeight * 0.5f, charWidth * 1.2f, magnetAuraPaint)
        }
        if (multiplierTimer > 0f) {
            canvas.drawCircle(screenX, screenY - charHeight * 0.5f, charWidth * 1.35f, multiplierAuraPaint)
        }

        val playerRect = RectF(
            screenX - charWidth / 2f,
            screenY - charHeight,
            screenX + charWidth / 2f,
            screenY
        )

        hoodiePaint.shader = LinearGradient(
            playerRect.left, playerRect.top, playerRect.right, playerRect.bottom,
            Color.parseColor("#EA580C"), Color.parseColor("#C2410C"),
            Shader.TileMode.CLAMP
        )

        if (isSliding) {
            canvas.drawRoundRect(playerRect, charHeight * 0.5f, charHeight * 0.5f, hoodiePaint)
            canvas.drawCircle(screenX + charWidth * 0.25f, screenY - charHeight * 0.5f, charHeight * 0.3f, visorPaint)
        } else {
            val headRadius = charWidth * 0.32f
            val headCx = screenX
            val headCy = playerRect.top + headRadius * 1.1f

            val torso = RectF(
                screenX - charWidth * 0.38f,
                playerRect.top + headRadius * 1.8f,
                screenX + charWidth * 0.38f,
                playerRect.bottom - charHeight * 0.32f
            )

            // Cap
            canvas.drawCircle(headCx, headCy, headRadius, capPaint)

            // Hoodie Torso
            canvas.drawRoundRect(torso, charWidth * 0.15f, charWidth * 0.15f, hoodiePaint)

            // Visor Glass
            val visorRect = RectF(
                headCx - headRadius * 0.7f,
                headCy - headRadius * 0.3f,
                headCx + headRadius * 0.8f,
                headCy + headRadius * 0.2f
            )
            canvas.drawRoundRect(visorRect, headRadius * 0.2f, headRadius * 0.2f, visorPaint)

            // Dynamic Running Legs & Sneakers
            val stride = sin(animationTime * 1.5f) * charWidth * 0.22f
            val legW = charWidth * 0.28f
            val leftLeg = RectF(
                screenX - charWidth * 0.35f + stride * 0.3f,
                torso.bottom,
                screenX - charWidth * 0.35f + legW + stride,
                playerRect.bottom
            )
            val rightLeg = RectF(
                screenX + charWidth * 0.35f - legW - stride,
                torso.bottom,
                screenX + charWidth * 0.35f - stride * 0.3f,
                playerRect.bottom
            )
            canvas.drawRoundRect(leftLeg, legW * 0.3f, legW * 0.3f, bodyPaint)
            canvas.drawRoundRect(rightLeg, legW * 0.3f, legW * 0.3f, bodyPaint)

            // Sneakers
            canvas.drawRoundRect(
                leftLeg.left, leftLeg.bottom - legW * 0.35f,
                leftLeg.right + legW * 0.2f, leftLeg.bottom,
                legW * 0.15f, legW * 0.15f, shoePaint
            )
            canvas.drawRoundRect(
                rightLeg.left, rightLeg.bottom - legW * 0.35f,
                rightLeg.right + legW * 0.2f, rightLeg.bottom,
                legW * 0.15f, legW * 0.15f, shoePaint
            )
        }
    }
}
