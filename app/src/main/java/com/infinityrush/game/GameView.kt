package com.infinityrush.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.SystemClock
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    private enum class GameState {
        START,
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    private data class SpawnInstruction(
        val type: ObstacleType,
        val minGapRatio: Float,
        val delayMs: Long = 0L
    )

    @Volatile
    private var isLoopRunning = false

    private var gameThread: Thread? = null
    private var gameState = GameState.START
    private var isSettingsOverlayVisible = false

    private val random = Random(System.currentTimeMillis())
    private val obstacles = mutableListOf<Obstacle>()
    private val pendingSpawnQueue = ArrayDeque<SpawnInstruction>()
    private val particles = mutableListOf<Particle>()

    private val playerBounds = RectF()
    private val pauseButtonRect = RectF()
    private val primaryButtonRect = RectF()
    private val settingsButtonRect = RectF()
    private val settingsPanelRect = RectF()
    private val settingsCloseButtonRect = RectF()
    private val musicToggleRect = RectF()
    private val sfxToggleRect = RectF()

    private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val farHillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#CBE9F7") }
    private val nearHillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#99D0F2") }
    private val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A") }
    private val lanePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B") }
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(190, 7, 15, 32) }
    private val panelStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(60, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = Utils.dpToPx(context, 1.5f)
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#24C8DB") }
    private val buttonMutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#334155") }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#082F49")
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val panelTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DBEAFE")
        textAlign = Paint.Align.CENTER
    }
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val hudSecondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CBD5E1")
        textAlign = Paint.Align.LEFT
    }
    private val toggleLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val toggleValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val pauseIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }
    private val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(180, 255, 255, 255) }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var player: Player? = null
    private val soundManager = SoundManager(context)

    private var sceneReady = false
    private var groundTop = 0f
    private var backgroundScroll = 0f
    private var lastSpawnedType: ObstacleType? = null

    private var score = 0
    private var highScore = Utils.getHighScore(context)
    private var distanceTravelled = 0f
    private var difficultyLevel = 0
    private var worldSpeed = Constants.INITIAL_WORLD_SPEED
    private var elapsedRunTimeMs = 0L
    private var obstacleTimerMs = 0L
    private var nextObstacleDelayMs = Constants.INITIAL_SPAWN_DELAY_MS

    private var touchDownX = 0f
    private var touchDownY = 0f
    private var touchDownTime = 0L
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
            val deltaSeconds = ((frameStart - previousFrameTime).coerceAtMost(50L)) / 1000f
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
                touchDownX = event.x
                touchDownY = event.y
                touchDownTime = SystemClock.elapsedRealtime()
                gestureConsumed = false
                touchStartedOnUi = shouldTreatTouchAsUi(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (gameState == GameState.RUNNING && !touchStartedOnUi && !gestureConsumed) {
                    val swipeThreshold = Utils.dpToPx(context, Constants.SWIPE_DOWN_THRESHOLD_DP)
                    val deltaX = event.x - touchDownX
                    val deltaY = event.y - touchDownY
                    if (deltaY > swipeThreshold && abs(deltaY) > abs(deltaX)) {
                        player?.queueSlide()
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
        groundTop = viewHeight * (1f - Constants.GROUND_HEIGHT_RATIO)

        skyPaint.shader = LinearGradient(
            0f,
            0f,
            0f,
            viewHeight.toFloat(),
            Color.parseColor("#ECFEFF"),
            Color.parseColor("#7DD3FC"),
            Shader.TileMode.CLAMP
        )

        titlePaint.textSize = viewHeight * 0.1f
        panelTitlePaint.textSize = viewHeight * 0.055f
        subtitlePaint.textSize = viewHeight * 0.035f
        buttonTextPaint.textSize = viewHeight * 0.04f
        hudPaint.textSize = viewHeight * 0.05f
        hudSecondaryPaint.textSize = viewHeight * 0.03f
        toggleLabelPaint.textSize = viewHeight * 0.034f
        toggleValuePaint.textSize = viewHeight * 0.028f

        val buttonWidth = viewWidth * Constants.MENU_BUTTON_WIDTH_RATIO
        val buttonHeight = viewHeight * Constants.MENU_BUTTON_HEIGHT_RATIO
        primaryButtonRect.set(
            viewWidth * 0.5f - buttonWidth / 2f,
            viewHeight * 0.58f,
            viewWidth * 0.5f + buttonWidth / 2f,
            viewHeight * 0.58f + buttonHeight
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

        val panelWidth = viewWidth * Constants.SETTINGS_PANEL_WIDTH_RATIO
        val panelHeight = viewHeight * Constants.SETTINGS_PANEL_HEIGHT_RATIO
        settingsPanelRect.set(
            viewWidth * 0.5f - panelWidth / 2f,
            viewHeight * 0.5f - panelHeight / 2f,
            viewWidth * 0.5f + panelWidth / 2f,
            viewHeight * 0.5f + panelHeight / 2f
        )

        val closeWidth = panelWidth * 0.22f
        val closeHeight = panelHeight * 0.18f
        settingsCloseButtonRect.set(
            settingsPanelRect.centerX() - closeWidth / 2f,
            settingsPanelRect.bottom - closeHeight - panelHeight * 0.1f,
            settingsPanelRect.centerX() + closeWidth / 2f,
            settingsPanelRect.bottom - panelHeight * 0.1f
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

        player = Player(
            startX = viewWidth * Constants.PLAYER_X_RATIO,
            width = viewHeight * Constants.PLAYER_WIDTH_RATIO,
            standHeight = viewHeight * Constants.PLAYER_HEIGHT_RATIO,
            slideHeight = viewHeight * Constants.PLAYER_SLIDE_HEIGHT_RATIO,
            floorTop = groundTop
        ).also {
            it.reset(groundTop)
        }
    }

    private fun startGameLoop() {
        if (isLoopRunning || !sceneReady) {
            return
        }

        isLoopRunning = true
        gameThread = Thread(this, "InfinityRushThread").apply { start() }
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
        backgroundScroll += width * 0.12f * deltaSeconds
        player?.advanceAnimation(deltaSeconds)
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

        backgroundScroll += worldSpeed * 0.18f * deltaSeconds
        distanceTravelled += worldSpeed * deltaSeconds
        score = max(score, (distanceTravelled / Constants.SCORE_DISTANCE_DIVISOR).toInt())

        val frameEvents = player?.update(deltaSeconds)
        handlePlayerFrameEvents(frameEvents)
        player?.getCollisionBounds(playerBounds)

        updateObstacles(deltaSeconds)
        updateParticles(deltaSeconds)
        updateScreenShake(deltaSeconds)

        if (obstacles.any { it.intersects(playerBounds) }) {
            handleCrash()
        }
    }

    private fun handlePlayerFrameEvents(frameEvents: PlayerFrameEvents?) {
        val activePlayer = player ?: return
        val events = frameEvents ?: return

        if (events.jumped) {
            soundManager.playJump()
            spawnDustBurst(activePlayer.centerX, activePlayer.feetY, 8, upward = true)
            startScreenShake(Constants.JUMP_SHAKE_DURATION, Constants.JUMP_SHAKE_STRENGTH)
        }

        if (events.slideStarted) {
            spawnDustBurst(activePlayer.centerX - width * 0.02f, activePlayer.feetY, 5, upward = false)
        }

        if (events.landed) {
            spawnDustBurst(activePlayer.centerX, activePlayer.feetY, 6, upward = false)
        }
    }

    private fun updateObstacles(deltaSeconds: Float) {
        obstacleTimerMs += (deltaSeconds * 1000f).toLong()

        if (pendingSpawnQueue.isEmpty()) {
            pendingSpawnQueue.addAll(buildSpawnPattern())
        }

        val nextSpawn = pendingSpawnQueue.firstOrNull()
        if (nextSpawn != null) {
            val spawnX = width + Utils.dpToPx(context, 64f)
            val minGap = width * max(Constants.MIN_PATTERN_GAP_RATIO, nextSpawn.minGapRatio)
            val lastObstacle = obstacles.lastOrNull()
            val hasSafeGap = lastObstacle == null || (spawnX - lastObstacle.right()) >= minGap

            if (obstacleTimerMs >= nextObstacleDelayMs && hasSafeGap) {
                obstacles += Obstacle.create(nextSpawn.type, spawnX, groundTop, height, random)
                lastSpawnedType = nextSpawn.type
                pendingSpawnQueue.removeFirst()
                obstacleTimerMs = 0L
                nextObstacleDelayMs = if (pendingSpawnQueue.isNotEmpty()) {
                    Constants.PATTERN_CHAIN_DELAY_MS + nextSpawn.delayMs
                } else {
                    computeNextSpawnDelay() + nextSpawn.delayMs
                }
            }
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

    private fun buildSpawnPattern(): List<SpawnInstruction> {
        val roll = random.nextInt(100)
        return when {
            difficultyLevel <= 0 -> {
                if (roll < 72) {
                    listOf(singleGroundSpawn(Constants.SAFE_GROUND_GAP_RATIO))
                } else {
                    buildGroundDoublePattern()
                }
            }

            difficultyLevel == 1 -> {
                when {
                    roll < 38 -> listOf(singleGroundSpawn(Constants.SAFE_GROUND_GAP_RATIO))
                    roll < 70 -> buildGroundDoublePattern()
                    else -> buildBarrierPattern()
                }
            }

            else -> {
                when {
                    roll < 28 -> listOf(singleGroundSpawn(Constants.SAFE_GROUND_GAP_RATIO))
                    roll < 55 -> buildGroundDoublePattern()
                    roll < 78 -> buildBarrierPattern()
                    else -> buildRhythmPattern()
                }
            }
        }
    }

    private fun buildGroundDoublePattern(): List<SpawnInstruction> {
        val firstType = nextGroundType()
        val secondType = if (random.nextFloat() < 0.6f) oppositeGroundType(firstType) else nextGroundType()
        return listOf(
            SpawnInstruction(firstType, Constants.SAFE_GROUND_GAP_RATIO),
            SpawnInstruction(secondType, Constants.SAFE_DOUBLE_GAP_RATIO, 40L)
        )
    }

    private fun buildBarrierPattern(): List<SpawnInstruction> {
        if (lastSpawnedType == ObstacleType.MOVING_BARRIER) {
            return listOf(singleGroundSpawn(Constants.SAFE_BARRIER_GAP_RATIO))
        }

        return listOf(
            SpawnInstruction(
                type = ObstacleType.MOVING_BARRIER,
                minGapRatio = Constants.SAFE_BARRIER_GAP_RATIO,
                delayMs = 110L
            )
        )
    }

    private fun buildRhythmPattern(): List<SpawnInstruction> {
        val firstType = nextGroundType()
        val secondType = oppositeGroundType(firstType)
        val thirdType = if (random.nextBoolean()) nextGroundType() else oppositeGroundType(secondType)
        return listOf(
            SpawnInstruction(firstType, Constants.SAFE_GROUND_GAP_RATIO),
            SpawnInstruction(secondType, Constants.SAFE_DOUBLE_GAP_RATIO),
            SpawnInstruction(thirdType, Constants.SAFE_DOUBLE_GAP_RATIO + 0.04f, 60L)
        )
    }

    private fun singleGroundSpawn(gapRatio: Float): SpawnInstruction {
        return SpawnInstruction(nextGroundType(), gapRatio)
    }

    private fun nextGroundType(): ObstacleType {
        return when (lastSpawnedType) {
            ObstacleType.BLOCK -> if (random.nextFloat() < 0.7f) ObstacleType.SPIKE else ObstacleType.BLOCK
            ObstacleType.SPIKE -> if (random.nextFloat() < 0.7f) ObstacleType.BLOCK else ObstacleType.SPIKE
            else -> if (random.nextBoolean()) ObstacleType.BLOCK else ObstacleType.SPIKE
        }
    }

    private fun oppositeGroundType(type: ObstacleType): ObstacleType {
        return if (type == ObstacleType.BLOCK) ObstacleType.SPIKE else ObstacleType.BLOCK
    }

    private fun computeNextSpawnDelay(): Long {
        val baseDelay = (Constants.INITIAL_SPAWN_DELAY_MS - difficultyLevel * Constants.SPAWN_DELAY_STEP_MS)
            .coerceAtLeast(Constants.MIN_SPAWN_DELAY_MS)
        val jitter = random.nextInt(
            -Constants.SPAWN_JITTER_MS.toInt(),
            Constants.SPAWN_JITTER_MS.toInt() + 1
        ).toLong()
        return (baseDelay + jitter).coerceAtLeast(Constants.MIN_SPAWN_DELAY_MS)
    }

    private fun handleCrash() {
        if (gameState == GameState.GAME_OVER) {
            return
        }

        gameState = GameState.GAME_OVER
        isSettingsOverlayVisible = false
        soundManager.playCrash()
        startScreenShake(Constants.CRASH_SHAKE_DURATION, Constants.CRASH_SHAKE_STRENGTH)
        player?.let { spawnCrashBurst(it.centerX, it.bodyTop + (it.feetY - it.bodyTop) * 0.45f) }

        if (score > highScore) {
            highScore = score
            Utils.saveHighScore(context, highScore)
        }
    }

    private fun startNewRun() {
        obstacles.clear()
        pendingSpawnQueue.clear()
        particles.clear()
        lastSpawnedType = null
        player?.reset(groundTop)
        score = 0
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
                when {
                    Utils.isInside(settingsButtonRect, x, y) -> isSettingsOverlayVisible = true
                    Utils.isInside(primaryButtonRect, x, y) -> startNewRun()
                }
            }

            GameState.RUNNING -> {
                if (Utils.isInside(pauseButtonRect, x, y)) {
                    pauseGame()
                    return
                }
                handleGameplayGesture(releaseX = x, releaseY = y)
            }

            GameState.PAUSED -> {
                when {
                    Utils.isInside(settingsButtonRect, x, y) -> isSettingsOverlayVisible = true
                    Utils.isInside(primaryButtonRect, x, y) || Utils.isInside(pauseButtonRect, x, y) -> resumeGame()
                }
            }

            GameState.GAME_OVER -> {
                when {
                    Utils.isInside(settingsButtonRect, x, y) -> isSettingsOverlayVisible = true
                    Utils.isInside(primaryButtonRect, x, y) -> startNewRun()
                }
            }
        }
    }

    private fun handleSettingsTouch(x: Float, y: Float) {
        when {
            Utils.isInside(musicToggleRect, x, y) -> {
                val enabled = !soundManager.isMusicEnabled()
                soundManager.setMusicEnabled(enabled, shouldPlayImmediately = gameState != GameState.PAUSED)
            }

            Utils.isInside(sfxToggleRect, x, y) -> {
                val enabled = !soundManager.isSfxEnabled()
                soundManager.setSfxEnabled(enabled)
                if (enabled) {
                    soundManager.playJump()
                }
            }

            Utils.isInside(settingsCloseButtonRect, x, y) || !Utils.isInside(settingsPanelRect, x, y) -> {
                isSettingsOverlayVisible = false
            }
        }
    }

    private fun handleGameplayGesture(releaseX: Float, releaseY: Float) {
        if (touchStartedOnUi || gestureConsumed) {
            return
        }

        val deltaX = releaseX - touchDownX
        val deltaY = releaseY - touchDownY
        val gestureDuration = SystemClock.elapsedRealtime() - touchDownTime
        val swipeThreshold = Utils.dpToPx(context, Constants.SWIPE_DOWN_THRESHOLD_DP)

        val didSlide = deltaY > swipeThreshold && abs(deltaY) > abs(deltaX)
        if (didSlide) {
            player?.queueSlide()
            gestureConsumed = true
            return
        }

        if (gestureDuration <= Constants.INPUT_TAP_MAX_DURATION_MS || abs(deltaY) < swipeThreshold) {
            player?.queueJump()
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

    private fun spawnDustBurst(originX: Float, originY: Float, count: Int, upward: Boolean) {
        repeat(count) {
            particles += Particle(
                x = originX + randomBetween(-width * 0.015f, width * 0.015f),
                y = originY - randomBetween(0f, height * 0.015f),
                velocityX = randomBetween(-180f, 120f),
                velocityY = if (upward) randomBetween(-260f, -120f) else randomBetween(-140f, -30f),
                radius = randomBetween(height * 0.006f, height * 0.012f),
                color = Color.parseColor(if (upward) "#E2E8F0" else "#94A3B8"),
                lifeSeconds = randomBetween(0.22f, 0.38f)
            )
        }
    }

    private fun spawnCrashBurst(originX: Float, originY: Float) {
        repeat(20) {
            particles += Particle(
                x = originX,
                y = originY,
                velocityX = randomBetween(-520f, 520f),
                velocityY = randomBetween(-520f, 150f),
                radius = randomBetween(height * 0.006f, height * 0.016f),
                color = if (it % 3 == 0) Color.parseColor("#24C8DB") else Color.parseColor("#F97316"),
                lifeSeconds = randomBetween(0.28f, 0.56f)
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

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)
        drawBackground(canvas)
        drawGround(canvas)
        obstacles.forEach { it.draw(canvas) }
        particles.forEach { it.draw(canvas, particlePaint) }
        player?.draw(canvas)
        drawHud(canvas)

        when (gameState) {
            GameState.START -> drawStartOverlay(canvas)
            GameState.PAUSED -> drawPauseOverlay(canvas)
            GameState.GAME_OVER -> drawGameOverOverlay(canvas)
            GameState.RUNNING -> Unit
        }

        if (gameState != GameState.RUNNING) {
            drawSettingsButton(canvas)
        }

        if (isSettingsOverlayVisible) {
            drawSettingsOverlay(canvas)
        }

        canvas.restore()
    }

    private fun drawBackground(canvas: Canvas) {
        val farOffset = backgroundScroll * 0.35f
        val nearOffset = backgroundScroll * 0.7f

        var farX = -(farOffset % (width * 0.55f)) - width * 0.45f
        while (farX < width + width * 0.6f) {
            canvas.drawOval(
                farX,
                groundTop - height * 0.42f,
                farX + width * 0.68f,
                groundTop + height * 0.03f,
                farHillPaint
            )
            farX += width * 0.42f
        }

        var nearX = -(nearOffset % (width * 0.4f)) - width * 0.3f
        while (nearX < width + width * 0.5f) {
            canvas.drawOval(
                nearX,
                groundTop - height * 0.28f,
                nearX + width * 0.54f,
                groundTop + height * 0.04f,
                nearHillPaint
            )
            nearX += width * 0.32f
        }

        canvas.drawCircle(width * 0.78f, height * 0.2f, height * 0.08f, sunPaint)
    }

    private fun drawGround(canvas: Canvas) {
        canvas.drawRect(0f, groundTop, width.toFloat(), height.toFloat(), groundPaint)

        val stripeWidth = width * 0.08f
        var stripeX = -(backgroundScroll % (stripeWidth * 1.8f))
        while (stripeX < width + stripeWidth) {
            canvas.drawRoundRect(
                stripeX,
                groundTop + height * 0.05f,
                stripeX + stripeWidth,
                groundTop + height * 0.065f,
                height * 0.01f,
                height * 0.01f,
                lanePaint
            )
            stripeX += stripeWidth * 1.8f
        }
    }

    private fun drawHud(canvas: Canvas) {
        hudPaint.textAlign = Paint.Align.LEFT
        hudSecondaryPaint.textAlign = Paint.Align.LEFT

        canvas.drawText("Score $score", width * 0.04f, height * 0.09f, hudPaint)
        canvas.drawText("Best $highScore", width * 0.04f, height * 0.14f, hudSecondaryPaint)

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

    private fun drawStartOverlay(canvas: Canvas) {
        drawOverlayPanel(canvas, 0.22f, 0.12f, 0.78f, 0.82f)
        canvas.drawText("Infinity Rush", width * 0.5f, height * 0.28f, titlePaint)
        canvas.drawText("Minimal speed. Endless pressure.", width * 0.5f, height * 0.36f, subtitlePaint)
        canvas.drawText("Tap to jump with input buffering.", width * 0.5f, height * 0.45f, subtitlePaint)
        canvas.drawText("Swipe down early to queue a slide.", width * 0.5f, height * 0.51f, subtitlePaint)
        canvas.drawText("Fairer obstacle patterns keep the run readable.", width * 0.5f, height * 0.57f, subtitlePaint)
        canvas.drawText("Beat your best distance and survive the ramp.", width * 0.5f, height * 0.74f, subtitlePaint)
        drawPrimaryButton(canvas, "Play")
    }

    private fun drawPauseOverlay(canvas: Canvas) {
        drawOverlayPanel(canvas, 0.3f, 0.2f, 0.7f, 0.78f)
        canvas.drawText("Paused", width * 0.5f, height * 0.34f, titlePaint)
        canvas.drawText("Your momentum is safe.", width * 0.5f, height * 0.44f, subtitlePaint)
        canvas.drawText("Open settings to tune audio before the next push.", width * 0.5f, height * 0.51f, subtitlePaint)
        drawPrimaryButton(canvas, "Resume")
    }

    private fun drawGameOverOverlay(canvas: Canvas) {
        drawOverlayPanel(canvas, 0.28f, 0.18f, 0.72f, 0.84f)
        canvas.drawText("Game Over", width * 0.5f, height * 0.32f, titlePaint)
        canvas.drawText("Final score: $score", width * 0.5f, height * 0.44f, subtitlePaint)
        canvas.drawText("High score: $highScore", width * 0.5f, height * 0.51f, subtitlePaint)
        canvas.drawText("The run is tougher now, but it plays fairer.", width * 0.5f, height * 0.7f, subtitlePaint)
        drawPrimaryButton(canvas, "Restart")
    }

    private fun drawOverlayPanel(
        canvas: Canvas,
        leftRatio: Float,
        topRatio: Float,
        rightRatio: Float,
        bottomRatio: Float
    ) {
        canvas.drawColor(Color.argb(60, 3, 7, 18))
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
        canvas.drawColor(Color.argb(90, 2, 6, 16))
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
