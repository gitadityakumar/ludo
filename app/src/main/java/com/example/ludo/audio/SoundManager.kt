package com.example.ludo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.ludo.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()
    var isMuted: Boolean = false

    enum class SoundType {
        DICE_ROLL,
        TOKEN_STEP,
        TOKEN_EXIT,
        CAPTURE,
        TOKEN_HOME,
        VICTORY,
        BUTTON_CLICK
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSound(context, SoundType.DICE_ROLL, R.raw.dice_roll)
        loadSound(context, SoundType.TOKEN_STEP, R.raw.token_step)
        loadSound(context, SoundType.TOKEN_EXIT, R.raw.token_exit)
        loadSound(context, SoundType.CAPTURE, R.raw.capture)
        loadSound(context, SoundType.TOKEN_HOME, R.raw.token_home)
        loadSound(context, SoundType.VICTORY, R.raw.victory)
        loadSound(context, SoundType.BUTTON_CLICK, R.raw.button_click)
    }

    private fun loadSound(context: Context, type: SoundType, resId: Int) {
        try {
            val id = soundPool.load(context, resId, 1)
            soundMap[type] = id
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(type: SoundType, volume: Float = 1.0f) {
        if (isMuted) return
        val soundId = soundMap[type] ?: return
        try {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            soundPool.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
