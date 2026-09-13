package com.infinityrush.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.SystemClock
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    private enum class GameState {
        START,
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    private data class Star(
        val xRatio: Float,
        val yRatio: Float,
        val radiusRatio: Float,
        val speed: Float,
        var phase: Float
    )

    @Volatile
    private var isLoopRunning = false

    private var gameThread: Thread? = null
    private var gameState = GameState.START
    private var isSettingsOverlayVisible = false

    private val random = Random(System.currentTimeMillis())
    private val obstacles = mutableListOf<Obstacle>()
    private val particles = mutableListOf<Particle>()
    private val collectibles = mutableListOf<Collectible>()
    private val stars = mutableListOf<Star>()

    private val pauseButtonRect = RectF()
    private val primaryButtonRect = RectF()
    private val settingsButtonRect = RectF()
    private val settingsPanelRect = RectF()
    private val settingsCloseButtonRect = RectF()
    private val musicToggleRect = RectF()
    private val sfxToggleRect = RectF()

    private val btnLeftRect = RectF()
    private val btnRightRect = RectF()
    private val btnJumpRect = RectF()
    private val btnSlideRect = RectF()
    private val btnHoverboardRect = RectF()

    private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val tunnelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E1B4B") }
    private val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A") }
    private val railPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8")
        strokeWidth = 5f
    }
    private val sleeperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334155")
        strokeWidth = 4f
    }

    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(205, 10, 15, 30) }
    private val panelStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 6, 182, 212)
        style = Paint.Style.STROKE
        strokeWidth = Utils.dpToPx(context, 2f)
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#06B6D4") }
    private val buttonMutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#334155") }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#082F49")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val touchButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 15, 23, 42) }
    private val touchButtonStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 6, 182, 212)
        style = Paint.Style.STROKE
        strokeWidth = Utils.dpToPx(context, 2f)
    }
    private val touchButtonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0F2FE")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val panelTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BAE6FD")
        textAlign = Paint.Align.CENTER
    }
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
    }
    private val hudSecondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CBD5E1")
        textAlign = Paint.Align.LEFT
    }
    private val coinHudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FBBF24")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val newBestBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val toggleLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
    }
    private val toggleValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val pauseIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val trackPath = Path()

    private val player: Player = Player()
    private val soundManager = SoundManager(context)

    private var sceneReady = false
    private var trackScrollZ = 0f

    private var score = 0
    private var highScore = Utils.getHighScore(context)
    private var currentRunCoins = 0
    private var totalCoins = Utils.getTotalCoins(context)
    private var isNewHighScore = false

    private var distanceTravelled = 0f
    private var difficultyLevel = 0
    private var worldSpeed = Constants.INITIAL_WORLD_SPEED
    private var elapsedRunTimeMs = 0L
    private var obstacleTimerMs = 0L
    private var nextObstacleDelayMs = Constants.INITIAL_SPAWN_DELAY_MS

    private var touchDownX = 0f
    private var touchDownY = 0f
    private var touchDownTime = 0L
    private var lastTapTime = 0L
    private var gestureConsumed = false
    private var touchStartedOnUi = false

    private var shakeTimeRemaining = 0f
    private var shakeDuration = 0f
    private var shakeStrength = 0f
    private var shakeOffsetX = 0f
    private var shakeOffsetY = 0f

    init {
        holder.addCallback(this)
        isFocusable = true
        keepScreenOn = true
        generateStarfield()
    }

    private fun generateStarfield() {
        stars.clear()
        repeat(30) {
            stars += Star(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat() * 0.35f,
                radiusRatio = 0.0015f + random.nextFloat() * 0.0025f,
                speed = 2f + random.nextFloat() * 3f,
                phase = random.nextFloat() * (Math.PI.toFloat() * 2f)
            )
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        ensureScene()
        soundManager.startMusic()
        startGameLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        configureScene(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGameLoop()
    }

    override fun run() {
        var previousFrameTime = SystemClock.elapsedRealtime()
        while (isLoopRunning) {
            val frameStart = SystemClock.elapsedRealtime()
            val deltaSeconds = ((frameStart - previousFrameTime).coerceIn(1L, 40L)) / 1000f
            previousFrameTime = frameStart

            if (sceneReady) {
                if (gameState == GameState.RUNNING) {
                    updateGame(deltaSeconds)
                } else if (gameState != GameState.PAUSED) {
                    updateAmbient(deltaSeconds)
                }
                renderFrame()
            }

            val frameDuration = SystemClock.elapsedRealtime() - frameStart
            val sleepTime = (Constants.FRAME_TIME_MS - frameDuration).coerceAtLeast(2L)
            try {
                Thread.sleep(sleepTime)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    fun onHostResume() {
        if (gameState != GameState.PAUSED) {
            soundManager.startMusic()
        }
        if (holder.surface.isValid) {
            startGameLoop()
        }
    }

    fun onHostPause() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED
        }
        isSettingsOverlayVisible = false
        soundManager.pauseMusic()
        stopGameLoop()
    }

    fun release() {
        stopGameLoop()
        soundManager.release()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        configureScene(w, h)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val now = SystemClock.elapsedRealtime()
                touchDownX = event.x
                touchDownY = event.y
                touchDownTime = now
                gestureConsumed = false
                touchStartedOnUi = shouldTreatTouchAsUi(event.x, event.y)

                if (gameState == GameState.RUNNING && !touchStartedOnUi && !isSettingsOverlayVisible) {
                    if (now - lastTapTime < Constants.DOUBLE_TAP_MAX_DELAY_MS) {
                        player.activateHoverboard()
                        soundManager.playHoverboard()
                        gestureConsumed = true
                    }
                    lastTapTime = now

                    if (!gestureConsumed) {
                        when {
                            Utils.isInside(btnLeftRect, event.x, event.y) -> {
                                if (player.moveLeft()) soundManager.playLaneSwitch()
                                gestureConsumed = true
                            }
                            Utils.isInside(btnRightRect, event.x, event.y) -> {
                                if (player.moveRight()) soundManager.playLaneSwitch()
                                gestureConsumed = true
                            }
                            Utils.isInside(btnJumpRect, event.x, event.y) -> {
                                player.queueJump()
                                gestureConsumed = true
                            }
                            Utils.isInside(btnSlideRect, event.x, event.y) -> {
                                player.queueSlide()
                                soundManager.playSlide()
                                gestureConsumed = true
                            }
                            Utils.isInside(btnHoverboardRect, event.x, event.y) -> {
                                player.activateHoverboard()
                                soundManager.playHoverboard()
                                gestureConsumed = true
                            }
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (gameState == GameState.RUNNING && !touchStartedOnUi && !gestureConsumed && !isSettingsOverlayVisible) {
                    val swipeThreshold = Utils.dpToPx(context, Constants.SWIPE_THRESHOLD_DP)
                    val deltaX = event.x - touchDownX
                    val deltaY = event.y - touchDownY

                    if (abs(deltaX) > swipeThreshold && abs(deltaX) > abs(deltaY)) {
                        if (deltaX < 0) {
                            if (player.moveLeft()) soundManager.playLaneSwitch()
                        } else {
                            if (player.moveRight()) soundManager.playLaneSwitch()
                        }
                        gestureConsumed = true
                    } else if (abs(deltaY) > swipeThreshold) {
                        if (deltaY < 0) {
                            player.queueJump()
                        } else {
                            player.queueSlide()
                            soundManager.playSlide()
                        }
                        gestureConsumed = true
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                performClick()
                handleTouchRelease(event.x, event.y)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun ensureScene() {
        if (width > 0 && height > 0) {
            configureScene(width, height)
        }
    }

    private fun configureScene(viewWidth: Int, viewHeight: Int) {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return
        }

        sceneReady = true

        skyPaint.shader = LinearGradient(
            0f,
            0f,
            0f,
            viewHeight.toFloat(),
            intArrayOf(
                Color.parseColor("#0F172A"),
                Color.parseColor("#1E1B4B"),
                Color.parseColor("#311B92"),
                Color.parseColor("#581C87")
            ),
            floatArrayOf(0.0f, 0.35f, 0.7f, 1.0f),
            Shader.TileMode.CLAMP
        )

        val sunCenterX = viewWidth * 0.78f
        val sunCenterY = viewHeight * 0.20f
        val sunRadius = viewHeight * 0.12f
        sunPaint.shader = RadialGradient(
            sunCenterX,
            sunCenterY,
            sunRadius,
            intArrayOf(
                Color.parseColor("#FDE047"),
                Color.parseColor("#F59E0B"),
                Color.argb(0, 245, 158, 11)
            ),
            floatArrayOf(0.0f, 0.5f, 1.0f),
            Shader.TileMode.CLAMP
        )

        titlePaint.textSize = viewHeight * 0.095f
        panelTitlePaint.textSize = viewHeight * 0.055f
        subtitlePaint.textSize = viewHeight * 0.035f
        buttonTextPaint.textSize = viewHeight * 0.04f
        touchButtonTextPaint.textSize = viewHeight * 0.03f
        hudPaint.textSize = viewHeight * 0.048f
        hudSecondaryPaint.textSize = viewHeight * 0.03f
        coinHudPaint.textSize = viewHeight * 0.045f
        newBestBadgePaint.textSize = viewHeight * 0.036f
        toggleLabelPaint.textSize = viewHeight * 0.034f
        toggleValuePaint.textSize = viewHeight * 0.028f

        val buttonWidth = viewWidth * Constants.MENU_BUTTON_WIDTH_RATIO
        val buttonHeight = viewHeight * Constants.MENU_BUTTON_HEIGHT_RATIO
        primaryButtonRect.set(
            viewWidth * 0.5f - buttonWidth / 2f,
            viewHeight * 0.62f,
            viewWidth * 0.5f + buttonWidth / 2f,
            viewHeight * 0.62f + buttonHeight
        )

        val iconSize = viewHeight * Constants.HUD_ICON_SIZE_RATIO
        pauseButtonRect.set(
            viewWidth - iconSize - viewWidth * 0.03f,
            viewHeight * 0.05f,
            viewWidth - viewWidth * 0.03f,
            viewHeight * 0.05f + iconSize
        )
        settingsButtonRect.set(
            viewWidth * 0.03f,
            viewHeight * 0.05f,
            viewWidth * 0.03f + iconSize,
            viewHeight * 0.05f + iconSize
        )

        val btnW = viewWidth * 0.12f
        val btnH = viewHeight * 0.11f
        val marginX = viewWidth * 0.03f
        val marginY = viewHeight * 0.04f

        btnLeftRect.set(marginX, viewHeight - btnH - marginY, marginX + btnW, viewHeight - marginY)
        btnRightRect.set(btnLeftRect.right + viewWidth * 0.015f, viewHeight - btnH - marginY, btnLeftRect.right + viewWidth * 0.015f + btnW, viewHeight - marginY)

        btnSlideRect.set(viewWidth - marginX - btnW, viewHeight - btnH - marginY, viewWidth - marginX, viewHeight - marginY)
        btnJumpRect.set(btnSlideRect.left - viewWidth * 0.015f - btnW, viewHeight - btnH - marginY, btnSlideRect.left - viewWidth * 0.015f, viewHeight - marginY)

        val hbW = viewWidth * 0.18f
        btnHoverboardRect.set(viewWidth * 0.5f - hbW / 2f, viewHeight - btnH - marginY, viewWidth * 0.5f + hbW / 2f, viewHeight - marginY)

        val panelWidth = viewWidth * Constants.SETTINGS_PANEL_WIDTH_RATIO
        val panelHeight = viewHeight * Constants.SETTINGS_PANEL_HEIGHT_RATIO
        settingsPanelRect.set(
            viewWidth * 0.5f - panelWidth / 2f,
            viewHeight * 0.5f - panelHeight / 2f,
            viewWidth * 0.5f + panelWidth / 2f,
            viewHeight * 0.5f + panelHeight / 2f
        )

        val closeWidth = panelWidth * 0.24f
        val closeHeight = panelHeight * 0.18f
        settingsCloseButtonRect.set(
            settingsPanelRect.centerX() - closeWidth / 2f,
            settingsPanelRect.bottom - closeHeight - panelHeight * 0.08f,
            settingsPanelRect.centerX() + closeWidth / 2f,
            settingsPanelRect.bottom - panelHeight * 0.08f
        )

        val toggleWidth = panelWidth * 0.78f
        val toggleHeight = panelHeight * 0.2f
        musicToggleRect.set(
            settingsPanelRect.centerX() - toggleWidth / 2f,
            settingsPanelRect.top + panelHeight * 0.26f,
            settingsPanelRect.centerX() + toggleWidth / 2f,
            settingsPanelRect.top + panelHeight * 0.26f + toggleHeight
        )
        sfxToggleRect.set(
            settingsPanelRect.centerX() - toggleWidth / 2f,
            musicToggleRect.bottom + panelHeight * 0.08f,
            settingsPanelRect.centerX() + toggleWidth / 2f,
            musicToggleRect.bottom + panelHeight * 0.08f + toggleHeight
        )

        player.reset()
    }

    private fun startGameLoop() {
        if (isLoopRunning || !sceneReady) {
            return
        }

        isLoopRunning = true
        gameThread = Thread(this, "SubwaySurfersThread").apply { start() }
    }

    private fun stopGameLoop() {
        if (!isLoopRunning) {
            return
        }

        isLoopRunning = false
        gameThread?.interrupt()
        try {
            gameThread?.join(500)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        gameThread = null
    }

    private fun updateAmbient(deltaSeconds: Float) {
        trackScrollZ += width * 0.35f * deltaSeconds
        stars.forEach { star -> star.phase += deltaSeconds * star.speed }
        updateParticles(deltaSeconds)
        updateScreenShake(deltaSeconds)
    }

    private fun updateGame(deltaSeconds: Float) {
        elapsedRunTimeMs += (deltaSeconds * 1000f).toLong()
        val updatedLevel = (elapsedRunTimeMs / Constants.SPEED_STEP_INTERVAL_MS).toInt()
        if (updatedLevel != difficultyLevel) {
            difficultyLevel = updatedLevel
            worldSpeed = Constants.INITIAL_WORLD_SPEED + difficultyLevel * Constants.WORLD_SPEED_STEP
        }

        trackScrollZ += worldSpeed * deltaSeconds
        stars.forEach { star -> star.phase += deltaSeconds * star.speed }

        val scoreMultiplier = if (player.multiplierTimer > 0f) 2 else 1
        distanceTravelled += worldSpeed * deltaSeconds * scoreMultiplier
        score = max(score, (distanceTravelled / Constants.SCORE_DISTANCE_DIVISOR).toInt())

        val frameEvents = player.update(deltaSeconds)
        handlePlayerFrameEvents(frameEvents)

        updateObstacles(deltaSeconds)
        updateCollectibles(deltaSeconds)
        updateParticles(deltaSeconds)
        updateScreenShake(deltaSeconds)

        val collidingObstacle = obstacles.find { it.intersectsPlayer(player) }
        if (collidingObstacle != null) {
            if (player.triggerCrashProtection()) {
                soundManager.playShieldBreak()
                startScreenShake(Constants.JUMP_SHAKE_DURATION * 1.5f, Constants.JUMP_SHAKE_STRENGTH * 2f)
                spawnSparkleBurst(width * 0.5f, height * 0.6f, Color.parseColor("#F59E0B"))
            } else {
                handleCrash()
            }
        }
    }

    private fun handlePlayerFrameEvents(frameEvents: PlayerFrameEvents?) {
        val events = frameEvents ?: return

        if (events.jumped) {
            soundManager.playJump()
            startScreenShake(Constants.JUMP_SHAKE_DURATION, Constants.JUMP_SHAKE_STRENGTH)
        }

        if (events.slideStarted || events.fastDropped) {
            soundManager.playSlide()
        }
    }

    private fun updateObstacles(deltaSeconds: Float) {
        obstacleTimerMs += (deltaSeconds * 1000f).toLong()

        if (obstacleTimerMs >= nextObstacleDelayMs) {
            val obstacle = Obstacle.createRandom(random, difficultyLevel)
            obstacles += obstacle
            spawnCollectiblesForObstacle(obstacle)

            obstacleTimerMs = 0L
            nextObstacleDelayMs = (Constants.INITIAL_SPAWN_DELAY_MS - difficultyLevel * Constants.SPAWN_DELAY_STEP_MS)
                .coerceAtLeast(Constants.MIN_SPAWN_DELAY_MS)
        }

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            obstacle.update(deltaSeconds, worldSpeed)
            if (obstacle.isOffScreen()) {
                iterator.remove()
            }
        }
    }

    private fun updateCollectibles(deltaSeconds: Float) {
        val iterator = collectibles.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()

            if (item.type == CollectibleType.COIN && player.magnetTimer > 0f) {
                item.attractTowards(player.currentLanePos, player.z, deltaSeconds)
            } else {
                item.update(deltaSeconds, worldSpeed)
            }

            if (item.intersectsPlayer(player)) {
                when (item.type) {
                    CollectibleType.COIN -> {
                        val coinBonus = if (player.multiplierTimer > 0f) Constants.COIN_SCORE_BONUS * 2 else Constants.COIN_SCORE_BONUS
                        score += coinBonus
                        currentRunCoins++
                        totalCoins++
                        soundManager.playCoin()
                        spawnFloatingText(width * 0.5f, height * 0.45f, "+$coinBonus", Color.parseColor("#FBBF24"))
                    }

                    CollectibleType.HOVERBOARD -> {
                        player.activateHoverboard()
                        soundManager.playHoverboard()
                        spawnFloatingText(width * 0.5f, height * 0.45f, "HOVERBOARD!", Color.parseColor("#10B981"))
                    }

                    CollectibleType.JETPACK -> {
                        player.hasJetpack = true
                        player.jetpackTimer = Constants.JETPACK_DURATION_SECONDS
                        soundManager.playHoverboard()
                        spawnFloatingText(width * 0.5f, height * 0.45f, "JETPACK!", Color.parseColor("#84CC16"))
                    }

                    CollectibleType.MAGNET -> {
                        player.magnetTimer = Constants.MAGNET_DURATION_SECONDS
                        soundManager.playMagnetGet()
                        spawnFloatingText(width * 0.5f, height * 0.45f, "MAGNET!", Color.parseColor("#F87171"))
                    }

                    CollectibleType.MULTIPLIER -> {
                        player.multiplierTimer = Constants.MULTIPLIER_DURATION_SECONDS
                        soundManager.playMultiplierGet()
                        spawnFloatingText(width * 0.5f, height * 0.45f, "2X SCORE!", Color.parseColor("#C084FC"))
                    }
                }
                iterator.remove()
            } else if (item.isOffScreen()) {
                iterator.remove()
            }
        }
    }

    private fun spawnCollectiblesForObstacle(obstacle: Obstacle) {
        val count = random.nextInt(3, 7)
        val startZ = obstacle.z - 80f

        if (random.nextFloat() < 0.22f) {
            val powerupType = when (random.nextInt(4)) {
                0 -> CollectibleType.HOVERBOARD
                1 -> CollectibleType.JETPACK
                2 -> CollectibleType.MAGNET
                else -> CollectibleType.MULTIPLIER
            }
            collectibles += Collectible(powerupType, obstacle.lane, startZ - 60f, 40f)
        }

        for (i in 0 until count) {
            val zPos = startZ - i * 50f
            val yPos = if (obstacle.type == ObstacleType.HURDLE) 80f else 20f
            collectibles += Collectible(CollectibleType.COIN, obstacle.lane, zPos, yPos, i * 0.4f)
        }
    }

    private fun handleCrash() {
        if (gameState == GameState.GAME_OVER) {
            return
        }

        gameState = GameState.GAME_OVER
        isSettingsOverlayVisible = false
        soundManager.playCrash()
        startScreenShake(Constants.CRASH_SHAKE_DURATION, Constants.CRASH_SHAKE_STRENGTH)
        spawnCrashBurst(width * 0.5f, height * 0.6f)

        Utils.saveTotalCoins(context, totalCoins)

        if (score > highScore) {
            highScore = score
            isNewHighScore = true
            Utils.saveHighScore(context, highScore)
        } else {
            isNewHighScore = false
        }
    }

    private fun startNewRun() {
        obstacles.clear()
        particles.clear()
        collectibles.clear()
        player.reset()
        score = 0
        currentRunCoins = 0
        isNewHighScore = false
        distanceTravelled = 0f
        difficultyLevel = 0
        elapsedRunTimeMs = 0L
        obstacleTimerMs = 0L
        worldSpeed = Constants.INITIAL_WORLD_SPEED
        nextObstacleDelayMs = Constants.INITIAL_SPAWN_DELAY_MS
        isSettingsOverlayVisible = false
        gameState = GameState.RUNNING
        soundManager.startMusic()
    }

    private fun pauseGame() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED
            isSettingsOverlayVisible = false
            soundManager.pauseMusic()
        }
    }

    private fun resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING
            isSettingsOverlayVisible = false
            soundManager.resumeMusic()
        }
    }

    private fun handleTouchRelease(x: Float, y: Float) {
        if (!sceneReady) {
            return
        }

        if (isSettingsOverlayVisible) {
            handleSettingsTouch(x, y)
            return
        }

        when (gameState) {
            GameState.START -> {
                soundManager.playButtonClick()
                when {
                    Utils.isInside(settingsButtonRect, x, y) -> isSettingsOverlayVisible = true
                    Utils.isInside(primaryButtonRect, x, y) -> startNewRun()
                }
            }

            GameState.RUNNING -> {
                if (Utils.isInside(pauseButtonRect, x, y)) {
                    soundManager.playButtonClick()
                    pauseGame()
                }
            }

            GameState.PAUSED -> {
                soundManager.playButtonClick()
                when {
                    Utils.isInside(settingsButtonRect, x, y) -> isSettingsOverlayVisible = true
                    Utils.isInside(primaryButtonRect, x, y) || Utils.isInside(pauseButtonRect, x, y) -> resumeGame()
                }
            }

            GameState.GAME_OVER -> {
                soundManager.playButtonClick()
                when {
                    Utils.isInside(settingsButtonRect, x, y) -> isSettingsOverlayVisible = true
                    Utils.isInside(primaryButtonRect, x, y) -> startNewRun()
                }
            }
        }
    }

    private fun handleSettingsTouch(x: Float, y: Float) {
        soundManager.playButtonClick()
        when {
            Utils.isInside(musicToggleRect, x, y) -> {
                val enabled = !soundManager.isMusicEnabled()
                soundManager.setMusicEnabled(enabled, shouldPlayImmediately = gameState != GameState.PAUSED)
            }

            Utils.isInside(sfxToggleRect, x, y) -> {
                val enabled = !soundManager.isSfxEnabled()
                soundManager.setSfxEnabled(enabled)
            }

            Utils.isInside(settingsCloseButtonRect, x, y) || !Utils.isInside(settingsPanelRect, x, y) -> {
                isSettingsOverlayVisible = false
            }
        }
    }

    private fun shouldTreatTouchAsUi(x: Float, y: Float): Boolean {
        return when {
            isSettingsOverlayVisible -> true
            gameState == GameState.RUNNING -> Utils.isInside(pauseButtonRect, x, y)
            else -> Utils.isInside(primaryButtonRect, x, y) || Utils.isInside(settingsButtonRect, x, y)
        }
    }

    private fun updateParticles(deltaSeconds: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.update(deltaSeconds)
            if (!particle.isAlive) {
                iterator.remove()
            }
        }
    }

    private fun updateScreenShake(deltaSeconds: Float) {
        if (shakeTimeRemaining <= 0f) {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
            return
        }

        shakeTimeRemaining = (shakeTimeRemaining - deltaSeconds).coerceAtLeast(0f)
        val intensity = if (shakeDuration == 0f) 0f else (shakeTimeRemaining / shakeDuration) * shakeStrength
        shakeOffsetX = randomBetween(-intensity, intensity)
        shakeOffsetY = randomBetween(-intensity, intensity)
    }

    private fun startScreenShake(duration: Float, strength: Float) {
        if (strength >= shakeStrength || duration >= shakeTimeRemaining) {
            shakeDuration = duration
            shakeTimeRemaining = duration
            shakeStrength = strength
        }
    }

    private fun spawnSparkleBurst(originX: Float, originY: Float, color: Int) {
        repeat(14) {
            particles += Particle(
                x = originX,
                y = originY,
                velocityX = randomBetween(-250f, 250f),
                velocityY = randomBetween(-250f, 200f),
                radius = randomBetween(height * 0.008f, height * 0.016f),
                color = color,
                lifeSeconds = randomBetween(0.25f, 0.45f)
            )
        }
    }

    private fun spawnFloatingText(originX: Float, originY: Float, text: String, color: Int) {
        particles += Particle(
            x = originX,
            y = originY,
            velocityX = 0f,
            velocityY = -130f,
            radius = height * 0.04f,
            color = color,
            lifeSeconds = 0.85f,
            text = text
        )
    }

    private fun spawnCrashBurst(originX: Float, originY: Float) {
        repeat(28) {
            particles += Particle(
                x = originX,
                y = originY,
                velocityX = randomBetween(-550f, 520f),
                velocityY = randomBetween(-550f, 180f),
                radius = randomBetween(height * 0.008f, height * 0.018f),
                color = if (it % 2 == 0) Color.parseColor("#0284C7") else Color.parseColor("#EA580C"),
                lifeSeconds = randomBetween(0.3f, 0.6f)
            )
        }
    }

    private fun randomBetween(minValue: Float, maxValue: Float): Float {
        return minValue + random.nextFloat() * (maxValue - minValue)
    }

    private fun renderFrame() {
        if (!holder.surface.isValid) {
            return
        }

        val canvas = holder.lockCanvas() ?: return
        try {
            drawScene(canvas)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawScene(canvas: Canvas) {
        canvas.save()
        canvas.translate(shakeOffsetX, shakeOffsetY)

        val vpX = width * 0.5f
        val vpY = height * 0.28f
        val groundFrontY = height * 0.92f

        if (abs(player.cameraTiltAngle) > 0.01f) {
            canvas.rotate(player.cameraTiltAngle, vpX, vpY)
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)
        draw3DBackground(canvas, vpY)
        draw3DTrack(canvas, vpX, vpY, groundFrontY)

        val renderList = mutableListOf<Any>()
        renderList.addAll(obstacles)
        renderList.addAll(collectibles)

        renderList.sortWith { a, b ->
            val zA = if (a is Obstacle) a.z else (a as Collectible).z
            val zB = if (b is Obstacle) b.z else (b as Collectible).z
            zB.compareTo(zA)
        }

        renderList.forEach { obj ->
            if (obj is Obstacle) {
                obj.draw3D(canvas, vpX, vpY, groundFrontY, width.toFloat(), height.toFloat())
            } else if (obj is Collectible) {
                obj.draw3D(canvas, vpX, vpY, groundFrontY, width.toFloat(), height.toFloat())
            }
        }

        player.draw3D(canvas, vpX, vpY, groundFrontY, width.toFloat(), height.toFloat())

        particles.forEach { it.draw(canvas, particlePaint) }
        drawHud(canvas)

        when (gameState) {
            GameState.START -> drawStartOverlay(canvas)
            GameState.PAUSED -> drawPauseOverlay(canvas)
            GameState.GAME_OVER -> drawGameOverOverlay(canvas)
            GameState.RUNNING -> drawTouchControls(canvas)
        }

        if (gameState != GameState.RUNNING) {
            drawSettingsButton(canvas)
        }

        if (isSettingsOverlayVisible) {
            drawSettingsOverlay(canvas)
        }

        canvas.restore()
    }

    private fun draw3DBackground(canvas: Canvas, vpY: Float) {
        stars.forEach { star ->
            val starX = star.xRatio * width
            val starY = star.yRatio * height
            val alpha = (130 + sin(star.phase) * 110).toInt().coerceIn(10, 255)
            starPaint.alpha = alpha
            canvas.drawCircle(starX, starY, star.radiusRatio * height, starPaint)
        }

        canvas.drawCircle(width * 0.78f, height * 0.20f, height * 0.12f, sunPaint)

        canvas.drawRect(0f, vpY - height * 0.12f, width.toFloat(), vpY, tunnelPaint)
    }

    private fun draw3DTrack(canvas: Canvas, vpX: Float, vpY: Float, groundFrontY: Float) {
        trackPath.reset()
        trackPath.moveTo(vpX - width * 0.08f, vpY)
        trackPath.lineTo(vpX + width * 0.08f, vpY)
        trackPath.lineTo(width.toFloat(), groundFrontY)
        trackPath.lineTo(0f, groundFrontY)
        trackPath.close()

        canvas.drawPath(trackPath, groundPaint)

        for (i in 0..Constants.LANE_COUNT) {
            val laneRatio = i.toFloat() / Constants.LANE_COUNT
            val backX = vpX - width * 0.08f + (width * 0.16f) * laneRatio
            val frontX = 0f + width * laneRatio
            canvas.drawLine(backX, vpY, frontX, groundFrontY, railPaint)
        }

        var tieZ = -(trackScrollZ % 50f)
        while (tieZ < 1000f) {
            val scale = Constants.FOCAL_LENGTH / (tieZ + Constants.FOCAL_LENGTH)
            val yOnScreen = vpY + (groundFrontY - vpY) * scale
            val leftX = vpX - (width * 0.5f) * scale
            val rightX = vpX + (width * 0.5f) * scale
            canvas.drawLine(leftX, yOnScreen, rightX, yOnScreen, sleeperPaint)
            tieZ += 50f
        }
    }

    private fun drawHud(canvas: Canvas) {
        hudPaint.textAlign = Paint.Align.LEFT
        hudSecondaryPaint.textAlign = Paint.Align.LEFT

        val multText = if (player.multiplierTimer > 0f) " (2X!)" else ""
        canvas.drawText("Score $score$multText", width * 0.04f, height * 0.09f, hudPaint)
        canvas.drawText("Best $highScore", width * 0.04f, height * 0.14f, hudSecondaryPaint)

        canvas.drawText("Coins $totalCoins", width * 0.5f, height * 0.09f, coinHudPaint)

        if (gameState == GameState.RUNNING) {
            var pBarY = height * 0.13f
            val barW = width * 0.18f
            val barH = height * 0.018f
            val barLeft = width * 0.5f - barW / 2f

            if (player.hasHoverboard) {
                drawPowerupBar(canvas, barLeft, pBarY, barW, barH, player.hoverboardTimer / Constants.HOVERBOARD_DURATION_SECONDS, "HOVERBOARD", Color.parseColor("#F59E0B"))
                pBarY += barH + height * 0.012f
            }
            if (player.magnetTimer > 0f) {
                drawPowerupBar(canvas, barLeft, pBarY, barW, barH, player.magnetTimer / Constants.MAGNET_DURATION_SECONDS, "MAGNET", Color.parseColor("#EF4444"))
                pBarY += barH + height * 0.012f
            }
            if (player.multiplierTimer > 0f) {
                drawPowerupBar(canvas, barLeft, pBarY, barW, barH, player.multiplierTimer / Constants.MULTIPLIER_DURATION_SECONDS, "2X SCORE", Color.parseColor("#A855F7"))
            }
        }

        if (gameState == GameState.RUNNING) {
            canvas.drawRoundRect(
                pauseButtonRect,
                pauseButtonRect.height() * 0.3f,
                pauseButtonRect.height() * 0.3f,
                panelPaint
            )
            val barWidth = pauseButtonRect.width() * 0.14f
            val insetX = pauseButtonRect.width() * 0.32f
            val insetY = pauseButtonRect.height() * 0.24f
            canvas.drawRoundRect(
                pauseButtonRect.left + insetX,
                pauseButtonRect.top + insetY,
                pauseButtonRect.left + insetX + barWidth,
                pauseButtonRect.bottom - insetY,
                barWidth,
                barWidth,
                pauseIconPaint
            )
            canvas.drawRoundRect(
                pauseButtonRect.right - insetX - barWidth,
                pauseButtonRect.top + insetY,
                pauseButtonRect.right - insetX,
                pauseButtonRect.bottom - insetY,
                barWidth,
                barWidth,
                pauseIconPaint
            )
        }
    }

    private fun drawPowerupBar(canvas: Canvas, left: Float, top: Float, width: Float, height: Float, progress: Float, label: String, color: Int) {
        val bgRect = RectF(left, top, left + width, top + height)
        val fillRect = RectF(left, top, left + width * progress.coerceIn(0f, 1f), top + height)

        pBarPaint.color = Color.argb(120, 15, 23, 42)
        canvas.drawRoundRect(bgRect, height * 0.5f, height * 0.5f, pBarPaint)

        pBarPaint.color = color
        canvas.drawRoundRect(fillRect, height * 0.5f, height * 0.5f, pBarPaint)

        pBarPaint.color = Color.WHITE
        pBarPaint.textSize = height * 0.85f
        pBarPaint.textAlign = Paint.Align.LEFT
        pBarPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(label, left + width + 12f, top + height * 0.85f, pBarPaint)
    }

    private fun drawTouchControls(canvas: Canvas) {
        drawTouchBtn(canvas, btnLeftRect, "◄ LEFT")
        drawTouchBtn(canvas, btnRightRect, "RIGHT ►")
        drawTouchBtn(canvas, btnJumpRect, "⬆ JUMP")
        drawTouchBtn(canvas, btnSlideRect, "⚡ ROLL")
        drawTouchBtn(canvas, btnHoverboardRect, "🛹 HOVERBOARD")
    }

    private fun drawTouchBtn(canvas: Canvas, rect: RectF, label: String) {
        canvas.drawRoundRect(rect, rect.height() * 0.35f, rect.height() * 0.35f, touchButtonPaint)
        canvas.drawRoundRect(rect, rect.height() * 0.35f, rect.height() * 0.35f, touchButtonStrokePaint)
        val baseline = rect.centerY() - (touchButtonTextPaint.descent() + touchButtonTextPaint.ascent()) / 2f
        canvas.drawText(label, rect.centerX(), baseline, touchButtonTextPaint)
    }

    private fun drawStartOverlay(canvas: Canvas) {
        drawOverlayPanel(canvas, 0.20f, 0.08f, 0.80f, 0.86f)
        canvas.drawText("SUBWAY SURFERS RUSH", width * 0.5f, height * 0.23f, titlePaint)
        canvas.drawText("3D Subway Lane Runner Experience", width * 0.5f, height * 0.31f, subtitlePaint)
        canvas.drawText("Swipe LEFT / RIGHT or Tap ◄ / ► to Switch Lanes", width * 0.5f, height * 0.39f, subtitlePaint)
        canvas.drawText("Swipe UP / DOWN or Tap JUMP / ROLL to Jump & Roll", width * 0.5f, height * 0.45f, subtitlePaint)
        canvas.drawText("Double-Tap or Tap 🛹 HOVERBOARD for Crash Protection!", width * 0.5f, height * 0.51f, subtitlePaint)
        canvas.drawText("Dodge Subway Trains, collect Coins & Power-ups!", width * 0.5f, height * 0.57f, subtitlePaint)
        drawPrimaryButton(canvas, "PLAY")
    }

    private fun drawPauseOverlay(canvas: Canvas) {
        drawOverlayPanel(canvas, 0.3f, 0.2f, 0.7f, 0.78f)
        canvas.drawText("PAUSED", width * 0.5f, height * 0.34f, titlePaint)
        canvas.drawText("Take a breather.", width * 0.5f, height * 0.44f, subtitlePaint)
        canvas.drawText("Tune settings or jump back into the subway rush.", width * 0.5f, height * 0.51f, subtitlePaint)
        drawPrimaryButton(canvas, "RESUME")
    }

    private fun drawGameOverOverlay(canvas: Canvas) {
        drawOverlayPanel(canvas, 0.24f, 0.12f, 0.76f, 0.88f)
        canvas.drawText("GAME OVER", width * 0.5f, height * 0.27f, titlePaint)

        if (isNewHighScore) {
            canvas.drawText("★ NEW BEST SCORE! ★", width * 0.5f, height * 0.36f, newBestBadgePaint)
        }

        canvas.drawText("Final Score: $score", width * 0.5f, height * 0.44f, subtitlePaint)
        canvas.drawText("High Score: $highScore", width * 0.5f, height * 0.50f, subtitlePaint)
        canvas.drawText("Coins Collected: +$currentRunCoins  (Total: $totalCoins)", width * 0.5f, height * 0.56f, subtitlePaint)

        drawPrimaryButton(canvas, "REPLAY")
    }

    private fun drawOverlayPanel(
        canvas: Canvas,
        leftRatio: Float,
        topRatio: Float,
        rightRatio: Float,
        bottomRatio: Float
    ) {
        canvas.drawColor(Color.argb(80, 2, 6, 16))
        val left = width * leftRatio
        val top = height * topRatio
        val right = width * rightRatio
        val bottom = height * bottomRatio
        canvas.drawRoundRect(left, top, right, bottom, height * 0.04f, height * 0.04f, panelPaint)
        canvas.drawRoundRect(left, top, right, bottom, height * 0.04f, height * 0.04f, panelStrokePaint)
    }

    private fun drawPrimaryButton(canvas: Canvas, label: String) {
        canvas.drawRoundRect(
            primaryButtonRect,
            primaryButtonRect.height() * 0.45f,
            primaryButtonRect.height() * 0.45f,
            buttonPaint
        )
        val textBaseline = primaryButtonRect.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2f
        canvas.drawText(label, primaryButtonRect.centerX(), textBaseline, buttonTextPaint)
    }

    private fun drawSettingsButton(canvas: Canvas) {
        canvas.drawRoundRect(
            settingsButtonRect,
            settingsButtonRect.height() * 0.34f,
            settingsButtonRect.height() * 0.34f,
            panelPaint
        )
        canvas.drawRoundRect(
            settingsButtonRect,
            settingsButtonRect.height() * 0.34f,
            settingsButtonRect.height() * 0.34f,
            panelStrokePaint
        )

        val y1 = settingsButtonRect.top + settingsButtonRect.height() * 0.32f
        val y2 = settingsButtonRect.centerY()
        val y3 = settingsButtonRect.bottom - settingsButtonRect.height() * 0.32f
        val left = settingsButtonRect.left + settingsButtonRect.width() * 0.22f
        val right = settingsButtonRect.right - settingsButtonRect.width() * 0.22f
        pauseIconPaint.strokeWidth = settingsButtonRect.width() * 0.08f
        pauseIconPaint.strokeCap = Paint.Cap.ROUND

        canvas.drawLine(left, y1, right, y1, pauseIconPaint)
        canvas.drawLine(left, y2, right, y2, pauseIconPaint)
        canvas.drawLine(left, y3, right, y3, pauseIconPaint)
        canvas.drawCircle(settingsButtonRect.centerX() - settingsButtonRect.width() * 0.08f, y1, settingsButtonRect.width() * 0.08f, pauseIconPaint)
        canvas.drawCircle(settingsButtonRect.centerX() + settingsButtonRect.width() * 0.1f, y2, settingsButtonRect.width() * 0.08f, pauseIconPaint)
        canvas.drawCircle(settingsButtonRect.centerX() - settingsButtonRect.width() * 0.02f, y3, settingsButtonRect.width() * 0.08f, pauseIconPaint)
    }

    private fun drawSettingsOverlay(canvas: Canvas) {
        canvas.drawColor(Color.argb(100, 2, 6, 16))
        canvas.drawRoundRect(
            settingsPanelRect,
            height * 0.03f,
            height * 0.03f,
            panelPaint
        )
        canvas.drawRoundRect(
            settingsPanelRect,
            height * 0.03f,
            height * 0.03f,
            panelStrokePaint
        )

        canvas.drawText("Settings", settingsPanelRect.centerX(), settingsPanelRect.top + settingsPanelRect.height() * 0.14f, panelTitlePaint)
        drawToggleRow(canvas, musicToggleRect, "Music", soundManager.isMusicEnabled())
        drawToggleRow(canvas, sfxToggleRect, "Sound FX", soundManager.isSfxEnabled())

        val closePaint = if (soundManager.isMusicEnabled() || soundManager.isSfxEnabled()) buttonPaint else buttonMutedPaint
        canvas.drawRoundRect(
            settingsCloseButtonRect,
            settingsCloseButtonRect.height() * 0.45f,
            settingsCloseButtonRect.height() * 0.45f,
            closePaint
        )
        val closeBaseline = settingsCloseButtonRect.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2f
        canvas.drawText("Close", settingsCloseButtonRect.centerX(), closeBaseline, buttonTextPaint)
    }

    private fun drawToggleRow(canvas: Canvas, rect: RectF, label: String, enabled: Boolean) {
        canvas.drawRoundRect(rect, rect.height() * 0.42f, rect.height() * 0.42f, buttonMutedPaint)

        val pillWidth = rect.width() * 0.28f
        val pillRect = RectF(
            rect.right - pillWidth - rect.width() * 0.06f,
            rect.top + rect.height() * 0.18f,
            rect.right - rect.width() * 0.06f,
            rect.bottom - rect.height() * 0.18f
        )
        canvas.drawRoundRect(
            pillRect,
            pillRect.height() * 0.5f,
            pillRect.height() * 0.5f,
            if (enabled) buttonPaint else panelPaint
        )

        val labelBaseline = rect.centerY() - (toggleLabelPaint.descent() + toggleLabelPaint.ascent()) / 2f
        canvas.drawText(label, rect.left + rect.width() * 0.07f, labelBaseline, toggleLabelPaint)

        val valueBaseline = pillRect.centerY() - (toggleValuePaint.descent() + toggleValuePaint.ascent()) / 2f
        canvas.drawText(if (enabled) "On" else "Off", pillRect.centerX(), valueBaseline, toggleValuePaint)
    }
}
