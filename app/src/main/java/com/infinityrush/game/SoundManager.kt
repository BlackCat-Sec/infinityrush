package com.infinityrush.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private var musicEnabled = Utils.isMusicEnabled(appContext)
    private var sfxEnabled = Utils.isSfxEnabled(appContext)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val jumpSoundId = soundPool.load(appContext, R.raw.jump, 1)
    private val crashSoundId = soundPool.load(appContext, R.raw.crash, 1)

    private var mediaPlayer: MediaPlayer? = MediaPlayer.create(appContext, R.raw.bgm_loop)?.apply {
        isLooping = true
        setVolume(Constants.MUSIC_VOLUME, Constants.MUSIC_VOLUME)
    }

    fun startMusic() {
        if (!musicEnabled) {
            return
        }
        val player = mediaPlayer ?: return
        if (!player.isPlaying) {
            player.start()
        }
    }

    fun pauseMusic() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        }
    }

    fun resumeMusic() {
        startMusic()
    }

    fun playJump() {
        if (sfxEnabled) {
            soundPool.play(jumpSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 1.08f)
        }
    }

    fun playCrash() {
        if (sfxEnabled) {
            soundPool.play(crashSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 0.95f)
        }
    }

    fun isMusicEnabled(): Boolean = musicEnabled

    fun isSfxEnabled(): Boolean = sfxEnabled

    fun setMusicEnabled(enabled: Boolean, shouldPlayImmediately: Boolean) {
        musicEnabled = enabled
        Utils.saveMusicEnabled(appContext, enabled)

        if (enabled && shouldPlayImmediately) {
            startMusic()
        } else if (!enabled) {
            pauseMusic()
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        sfxEnabled = enabled
        Utils.saveSfxEnabled(appContext, enabled)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        soundPool.release()
    }
}
