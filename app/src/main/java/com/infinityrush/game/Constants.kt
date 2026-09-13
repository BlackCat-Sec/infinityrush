package com.infinityrush.game

object Constants {
    const val TARGET_FPS = 60L
    const val FRAME_TIME_MS = 1000L / TARGET_FPS

    const val PREFS_NAME = "infinity_rush_prefs"
    const val HIGH_SCORE_KEY = "high_score"
    const val TOTAL_COINS_KEY = "total_coins"
    const val MUSIC_ENABLED_KEY = "music_enabled"
    const val SFX_ENABLED_KEY = "sfx_enabled"

    const val LANE_COUNT = 3
    const val LANE_LEFT = 0
    const val LANE_CENTER = 1
    const val LANE_RIGHT = 2

    const val FOCAL_LENGTH = 380f
    const val SPAWN_Z = 1250f
    const val PLAYER_Z = 120f

    const val COIN_SCORE_BONUS = 50

    const val HOVERBOARD_DURATION_SECONDS = 15.0f
    const val JETPACK_DURATION_SECONDS = 6.0f
    const val MAGNET_DURATION_SECONDS = 10.0f
    const val MULTIPLIER_DURATION_SECONDS = 12.0f

    const val INITIAL_WORLD_SPEED = 780f
    const val WORLD_SPEED_STEP = 55f
    const val SPEED_STEP_INTERVAL_MS = 10_000L

    const val INITIAL_SPAWN_DELAY_MS = 1400L
    const val MIN_SPAWN_DELAY_MS = 650L
    const val SPAWN_DELAY_STEP_MS = 60L

    const val GRAVITY = 3600f
    const val FAST_DROP_VELOCITY = 2000f
    const val JUMP_VELOCITY = -1350f
    const val MIN_JUMP_VELOCITY = -600f
    const val SLIDE_DURATION_SECONDS = 0.65f
    const val COYOTE_TIME_SECONDS = 0.14f
    const val JUMP_BUFFER_SECONDS = 0.16f
    const val SLIDE_BUFFER_SECONDS = 0.18f
    const val INVINCIBILITY_DURATION_SECONDS = 1.5f

    const val SCORE_DISTANCE_DIVISOR = 16f

    const val SWIPE_THRESHOLD_DP = 36f
    const val DOUBLE_TAP_MAX_DELAY_MS = 320L

    const val MENU_BUTTON_WIDTH_RATIO = 0.24f
    const val MENU_BUTTON_HEIGHT_RATIO = 0.12f
    const val HUD_ICON_SIZE_RATIO = 0.09f
    const val SETTINGS_PANEL_WIDTH_RATIO = 0.34f
    const val SETTINGS_PANEL_HEIGHT_RATIO = 0.36f

    const val MUSIC_VOLUME = 0.45f
    const val SFX_VOLUME = 0.9f
    const val PARTICLE_GRAVITY = 1800f
    const val PARTICLE_DRAG = 1.8f
    const val JUMP_SHAKE_DURATION = 0.08f
    const val JUMP_SHAKE_STRENGTH = 5f
    const val CRASH_SHAKE_DURATION = 0.35f
    const val CRASH_SHAKE_STRENGTH = 24f
}
