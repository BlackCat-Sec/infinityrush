package com.infinityrush.game

object Constants {
    const val TARGET_FPS = 60L
    const val FRAME_TIME_MS = 1000L / TARGET_FPS

    const val PREFS_NAME = "infinity_rush_prefs"
    const val HIGH_SCORE_KEY = "high_score"
    const val MUSIC_ENABLED_KEY = "music_enabled"
    const val SFX_ENABLED_KEY = "sfx_enabled"

    const val GROUND_HEIGHT_RATIO = 0.18f

    const val PLAYER_X_RATIO = 0.16f
    const val PLAYER_WIDTH_RATIO = 0.08f
    const val PLAYER_HEIGHT_RATIO = 0.18f
    const val PLAYER_SLIDE_HEIGHT_RATIO = 0.1f

    const val BLOCK_WIDTH_RATIO = 0.085f
    const val BLOCK_HEIGHT_RATIO = 0.13f
    const val SPIKE_WIDTH_RATIO = 0.075f
    const val SPIKE_HEIGHT_RATIO = 0.085f
    const val MOVING_BARRIER_WIDTH_RATIO = 0.14f
    const val MOVING_BARRIER_HEIGHT_RATIO = 0.04f

    const val INITIAL_WORLD_SPEED = 900f
    const val WORLD_SPEED_STEP = 120f
    const val SPEED_STEP_INTERVAL_MS = 10_000L

    const val INITIAL_SPAWN_DELAY_MS = 1450L
    const val MIN_SPAWN_DELAY_MS = 700L
    const val SPAWN_DELAY_STEP_MS = 90L
    const val SPAWN_JITTER_MS = 150L
    const val MIN_OBSTACLE_GAP_RATIO = 0.22f

    const val GRAVITY = 3600f
    const val JUMP_VELOCITY = -1450f
    const val SLIDE_DURATION_SECONDS = 0.6f
    const val COYOTE_TIME_SECONDS = 0.11f
    const val JUMP_BUFFER_SECONDS = 0.14f
    const val SLIDE_BUFFER_SECONDS = 0.16f

    const val SCORE_DISTANCE_DIVISOR = 18f

    const val INPUT_TAP_MAX_DURATION_MS = 220L
    const val SWIPE_DOWN_THRESHOLD_DP = 56f
    const val PATTERN_CHAIN_DELAY_MS = 180L
    const val MIN_PATTERN_GAP_RATIO = 0.26f
    const val SAFE_GROUND_GAP_RATIO = 0.42f
    const val SAFE_DOUBLE_GAP_RATIO = 0.55f
    const val SAFE_BARRIER_GAP_RATIO = 0.62f

    const val MENU_BUTTON_WIDTH_RATIO = 0.24f
    const val MENU_BUTTON_HEIGHT_RATIO = 0.12f
    const val HUD_ICON_SIZE_RATIO = 0.09f
    const val SETTINGS_PANEL_WIDTH_RATIO = 0.34f
    const val SETTINGS_PANEL_HEIGHT_RATIO = 0.36f

    const val MUSIC_VOLUME = 0.4f
    const val SFX_VOLUME = 0.9f
    const val PARTICLE_GRAVITY = 2100f
    const val PARTICLE_DRAG = 1.9f
    const val JUMP_SHAKE_DURATION = 0.08f
    const val JUMP_SHAKE_STRENGTH = 5f
    const val CRASH_SHAKE_DURATION = 0.32f
    const val CRASH_SHAKE_STRENGTH = 22f
}
