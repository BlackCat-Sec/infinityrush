package com.infinityrush.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import java.io.DataOutputStream
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private var musicEnabled = Utils.isMusicEnabled(appContext)
    private var sfxEnabled = Utils.isSfxEnabled(appContext)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val rawJumpSoundId = try { soundPool.load(appContext, R.raw.jump, 1) } catch (_: Exception) { 0 }
    private val rawCrashSoundId = try { soundPool.load(appContext, R.raw.crash, 1) } catch (_: Exception) { 0 }

    private val coinSoundId = createSynthWav("synth_coin.wav", 0.15f) { t, total ->
        val progress = t.toFloat() / total
        val freq = if (progress < 0.5f) 987.77f else 1318.51f
        val env = (1f - progress)
        (sin(2.0 * PI * freq * t / 22050.0) * env * 24000).toInt().toShort()
    }

    private val laneSwitchSoundId = createSynthWav("synth_laneswitch.wav", 0.10f) { t, total ->
        val progress = t.toFloat() / total
        val noise = (Random.nextFloat() * 2f - 1f)
        val env = sin(PI * progress)
        (noise * env * 16000).toInt().toShort()
    }

    private val hoverboardSoundId = createSynthWav("synth_hoverboard.wav", 0.28f) { t, total ->
        val progress = t.toFloat() / total
        val freq = 300f + progress * 900f
        val env = (1f - progress)
        (sin(2.0 * PI * freq * t / 22050.0) * env * 24000).toInt().toShort()
    }

    private val shieldSoundId = createSynthWav("synth_shield.wav", 0.22f) { t, total ->
        val progress = t.toFloat() / total
        val freq = when {
            progress < 0.33f -> 523.25f
            progress < 0.66f -> 659.25f
            else -> 783.99f
        }
        val env = (1f - progress)
        (sin(2.0 * PI * freq * t / 22050.0) * env * 22000).toInt().toShort()
    }

    private val magnetSoundId = createSynthWav("synth_magnet.wav", 0.20f) { t, total ->
        val progress = t.toFloat() / total
        val freq = 400f + progress * 800f
        val env = sin(PI * progress)
        (sin(2.0 * PI * freq * t / 22050.0) * env * 22000).toInt().toShort()
    }

    private val multiplierSoundId = createSynthWav("synth_multiplier.wav", 0.25f) { t, total ->
        val progress = t.toFloat() / total
        val freq = 600f + progress * 1000f
        val env = (1f - progress)
        (sin(2.0 * PI * freq * t / 22050.0) * env * 24000).toInt().toShort()
    }

    private val shieldBreakSoundId = createSynthWav("synth_shield_break.wav", 0.25f) { t, total ->
        val progress = t.toFloat() / total
        val noise = (Random.nextFloat() * 2f - 1f)
        val tone = sin(2.0 * PI * (300f - progress * 180f) * t / 22050.0)
        val env = (1f - progress) * (1f - progress)
        ((tone * 0.4 + noise * 0.6) * env * 26000).toInt().toShort()
    }

    private val slideSoundId = createSynthWav("synth_slide.wav", 0.18f) { t, total ->
        val progress = t.toFloat() / total
        val noise = (Random.nextFloat() * 2f - 1f)
        val env = sin(PI * progress)
        (noise * env * 14000).toInt().toShort()
    }

    private val buttonSoundId = createSynthWav("synth_button.wav", 0.05f) { t, total ->
        val progress = t.toFloat() / total
        val env = 1f - progress
        (sin(2.0 * PI * 880.0 * t / 22050.0) * env * 18000).toInt().toShort()
    }

    private var mediaPlayer: MediaPlayer? = try {
        MediaPlayer.create(appContext, R.raw.bgm_loop)?.apply {
            isLooping = true
            setVolume(Constants.MUSIC_VOLUME, Constants.MUSIC_VOLUME)
        }
    } catch (_: Exception) { null }

    fun startMusic() {
        if (!musicEnabled) {
            return
        }
        val player = mediaPlayer ?: return
        if (!player.isPlaying) {
            try { player.start() } catch (_: Exception) {}
        }
    }

    fun pauseMusic() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            try { player.pause() } catch (_: Exception) {}
        }
    }

    fun resumeMusic() {
        startMusic()
    }

    fun playJump() {
        if (sfxEnabled) {
            if (rawJumpSoundId != 0) {
                soundPool.play(rawJumpSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 1.08f)
            }
        }
    }

    fun playCrash() {
        if (sfxEnabled) {
            if (rawCrashSoundId != 0) {
                soundPool.play(rawCrashSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 0.95f)
            }
        }
    }

    fun playCoin() {
        if (sfxEnabled && coinSoundId != 0) {
            soundPool.play(coinSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 1.0f)
        }
    }

    fun playLaneSwitch() {
        if (sfxEnabled && laneSwitchSoundId != 0) {
            soundPool.play(laneSwitchSoundId, Constants.SFX_VOLUME * 0.6f, Constants.SFX_VOLUME * 0.6f, 1, 0, 1.0f)
        }
    }

    fun playHoverboard() {
        if (sfxEnabled && hoverboardSoundId != 0) {
            soundPool.play(hoverboardSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 1.0f)
        }
    }

    fun playShieldGet() {
        if (sfxEnabled && shieldSoundId != 0) {
            soundPool.play(shieldSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 1.0f)
        }
    }

    fun playMagnetGet() {
        if (sfxEnabled && magnetSoundId != 0) {
            soundPool.play(magnetSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 1.0f)
        }
    }

    fun playMultiplierGet() {
        if (sfxEnabled && multiplierSoundId != 0) {
            soundPool.play(multiplierSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 1.0f)
        }
    }

    fun playShieldBreak() {
        if (sfxEnabled && shieldBreakSoundId != 0) {
            soundPool.play(shieldBreakSoundId, Constants.SFX_VOLUME, Constants.SFX_VOLUME, 1, 0, 1.0f)
        }
    }

    fun playSlide() {
        if (sfxEnabled && slideSoundId != 0) {
            soundPool.play(slideSoundId, Constants.SFX_VOLUME * 0.7f, Constants.SFX_VOLUME * 0.7f, 1, 0, 1.0f)
        }
    }

    fun playButtonClick() {
        if (sfxEnabled && buttonSoundId != 0) {
            soundPool.play(buttonSoundId, Constants.SFX_VOLUME * 0.6f, Constants.SFX_VOLUME * 0.6f, 1, 0, 1.0f)
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

    private fun createSynthWav(filename: String, durationSeconds: Float, sampleGenerator: (Int, Int) -> Short): Int {
        return try {
            val file = File(appContext.cacheDir, filename)
            val sampleRate = 22050
            val totalSamples = (sampleRate * durationSeconds).toInt()
            val pcm = ShortArray(totalSamples) { i -> sampleGenerator(i, totalSamples) }

            FileOutputStream(file).use { fos ->
                DataOutputStream(fos).use { dos ->
                    val dataSize = totalSamples * 2
                    dos.writeBytes("RIFF")
                    dos.writeInt(Integer.reverseBytes(36 + dataSize))
                    dos.writeBytes("WAVE")
                    dos.writeBytes("fmt ")
                    dos.writeInt(Integer.reverseBytes(16))
                    dos.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
                    dos.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
                    dos.writeInt(Integer.reverseBytes(sampleRate))
                    dos.writeInt(Integer.reverseBytes(sampleRate * 2))
                    dos.writeShort(java.lang.Short.reverseBytes(2.toShort()).toInt())
                    dos.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt())
                    dos.writeBytes("data")
                    dos.writeInt(Integer.reverseBytes(dataSize))
                    for (s in pcm) {
                        dos.writeShort(java.lang.Short.reverseBytes(s).toInt())
                    }
                }
            }
            soundPool.load(file.absolutePath, 1)
        } catch (_: Exception) {
            0
        }
    }
}
