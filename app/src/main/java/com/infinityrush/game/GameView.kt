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

    @Volatile
    private var isLoopRunning = false

    private var gameThread: Thread? = null
    private var gameState = GameState.START

    private val random = Random(System.currentTimeMillis())
    private val obstacles = mutableListOf<Obstacle>()
    private val playerBounds = RectF()
    private val pauseButtonRect = RectF()
    private val primaryButtonRect = RectF()

    private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val farHillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#CBE9F7") }
    private val nearHillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#99D0F2") }
    private val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A") }
    private val lanePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B") }
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(190, 7, 15, 32) }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#24C8DB") }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#082F49")
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
    private val pauseIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }

    private var player: Player? = null
    private val soundManager = SoundManager(context)

    private var sceneReady = false
    private var groundTop = 0f
    private var backgroundScroll = 0f

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
        subtitlePaint.textSize = viewHeight * 0.035f
        buttonTextPaint.textSize = viewHeight * 0.04f
        hudPaint.textSize = viewHeight * 0.05f
        hudSecondaryPaint.textSize = viewHeight * 0.03f

        val buttonWidth = viewWidth * Constants.MENU_BUTTON_WIDTH_RATIO
        val buttonHeight = viewHeight * Constants.MENU_BUTTON_HEIGHT_RATIO
        primaryButtonRect.set(
            viewWidth * 0.5f - buttonWidth / 2f,
            viewHeight * 0.58f,
            viewWidth * 0.5f + buttonWidth / 2f,
            viewHeight * 0.58f + buttonHeight
        )

        val pauseSize = viewHeight * 0.09f
        pauseButtonRect.set(
            viewWidth - pauseSize - viewWidth * 0.03f,
            viewHeight * 0.05f,
            viewWidth - viewWidth * 0.03f,
            viewHeight * 0.05f + pauseSize
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

        player?.update(deltaSeconds)
        player?.getCollisionBounds(playerBounds)

        updateObstacles(deltaSeconds)

        if (obstacles.any { it.intersects(playerBounds) }) {
            handleCrash()
        }
    }

    private fun updateObstacles(deltaSeconds: Float) {
        obstacleTimerMs += (deltaSeconds * 1000f).toLong()

        val minGap = width * Constants.MIN_OBSTACLE_GAP_RATIO
        val lastObstacle = obstacles.lastOrNull()
        val canSpawn = lastObstacle == null || (width + Utils.dpToPx(context, 48f) - lastObstacle.right()) >= minGap

        if (obstacleTimerMs >= nextObstacleDelayMs && canSpawn) {
            val spawnX = width + Utils.dpToPx(context, 48f)
            obstacles += Obstacle.createRandom(spawnX, groundTop, height, difficultyLevel, random)
            obstacleTimerMs = 0L
            nextObstacleDelayMs = computeNextSpawnDelay()
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
        gameState = GameState.GAME_OVER
        soundManager.playCrash()

        if (score > highScore) {
            highScore = score
            Utils.saveHighScore(context, highScore)
        }
    }

    private fun startNewRun() {
        obstacles.clear()
        player?.reset(groundTop)
        score = 0
        distanceTravelled = 0f
        difficultyLevel = 0
        elapsedRunTimeMs = 0L
        obstacleTimerMs = 0L
        worldSpeed = Constants.INITIAL_WORLD_SPEED
        nextObstacleDelayMs = Constants.INITIAL_SPAWN_DELAY_MS
        gameState = GameState.RUNNING
        soundManager.startMusic()
    }

    private fun pauseGame() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED
            soundManager.pauseMusic()
        }
    }

    private fun resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING
            soundManager.resumeMusic()
        }
    }

    private fun handleTouchRelease(x: Float, y: Float) {
        if (!sceneReady) {
            return
        }

        when (gameState) {
            GameState.START -> {
                if (Utils.isInside(primaryButtonRect, x, y)) {
                    startNewRun()
                }
            }

            GameState.RUNNING -> {
                if (Utils.isInside(pauseButtonRect, x, y)) {
                    pauseGame()
                    return
                }
                handleGameplayGesture(x, y)
            }

            GameState.PAUSED -> {
                if (Utils.isInside(primaryButtonRect, x, y) || Utils.isInside(pauseButtonRect, x, y)) {
                    resumeGame()
                }
            }

            GameState.GAME_OVER -> {
                if (Utils.isInside(primaryButtonRect, x, y)) {
                    startNewRun()
                }
            }
        }
    }

    private fun handleGameplayGesture(releaseX: Float, releaseY: Float) {
        val deltaX = releaseX - touchDownX
        val deltaY = releaseY - touchDownY
        val gestureDuration = SystemClock.elapsedRealtime() - touchDownTime
        val swipeThreshold = Utils.dpToPx(context, Constants.SWIPE_DOWN_THRESHOLD_DP)

        val didSlide = deltaY > swipeThreshold && abs(deltaY) > abs(deltaX)
        if (didSlide) {
            player?.slide()
            return
        }

        if (gestureDuration <= Constants.INPUT_TAP_MAX_DURATION_MS || abs(deltaY) < swipeThreshold) {
            val jumped = player?.jump() == true
            if (jumped) {
                soundManager.playJump()
            }
        }
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
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)
        drawBackground(canvas)
        drawGround(canvas)
        obstacles.forEach { it.draw(canvas) }
        player?.draw(canvas)
        drawHud(canvas)

        when (gameState) {
            GameState.START -> drawStartOverlay(canvas)
            GameState.PAUSED -> drawPauseOverlay(canvas)
            GameState.GAME_OVER -> drawGameOverOverlay(canvas)
            GameState.RUNNING -> Unit
        }
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

        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(180, 255, 255, 255) }
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
        canvas.drawText("Tap to jump", width * 0.5f, height * 0.46f, subtitlePaint)
        canvas.drawText("Swipe down to slide", width * 0.5f, height * 0.52f, subtitlePaint)
        canvas.drawText("Beat your best distance and survive the ramp.", width * 0.5f, height * 0.74f, subtitlePaint)
        drawPrimaryButton(canvas, "Play")
    }

    private fun drawPauseOverlay(canvas: Canvas) {
        drawOverlayPanel(canvas, 0.3f, 0.2f, 0.7f, 0.78f)
        canvas.drawText("Paused", width * 0.5f, height * 0.34f, titlePaint)
        canvas.drawText("Take a breath. The run waits for you.", width * 0.5f, height * 0.44f, subtitlePaint)
        drawPrimaryButton(canvas, "Resume")
    }

    private fun drawGameOverOverlay(canvas: Canvas) {
        drawOverlayPanel(canvas, 0.28f, 0.18f, 0.72f, 0.84f)
        canvas.drawText("Game Over", width * 0.5f, height * 0.32f, titlePaint)
        canvas.drawText("Final score: $score", width * 0.5f, height * 0.44f, subtitlePaint)
        canvas.drawText("High score: $highScore", width * 0.5f, height * 0.51f, subtitlePaint)
        canvas.drawText("The speed always comes back stronger.", width * 0.5f, height * 0.7f, subtitlePaint)
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
        canvas.drawRoundRect(
            width * leftRatio,
            height * topRatio,
            width * rightRatio,
            height * bottomRatio,
            height * 0.04f,
            height * 0.04f,
            panelPaint
        )
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
}
