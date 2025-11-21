package com.stand.hypnosis

import android.content.Context
import android.content.SharedPreferences
import androidx.core.graphics.toColorInt
import androidx.core.content.edit

class ConfigManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: ConfigManager? = null

        fun getInstance(context: Context): ConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val PREFS_NAME = "app_config"
        private const val KEY_IS_BEATING = "is_beating"
        private const val KEY_RING_COLOR = "ring_color"
        private const val KEY_BG_COLOR = "bg_color"
        private const val KEY_IS_FULL_SCREEN = "is_full_screen"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isBeating: Boolean
        get() = prefs.getBoolean(KEY_IS_BEATING, true)
        set(value) = prefs.edit { putBoolean(KEY_IS_BEATING, value) }

    var isFullScreen: Boolean
        get() = prefs.getBoolean(KEY_IS_FULL_SCREEN, true)
        set(value) = prefs.edit { putBoolean(KEY_IS_FULL_SCREEN, value) }

    var ringColor: Int
        get() = prefs.getInt(KEY_RING_COLOR, "#EF36CD".toColorInt())
        set(value) = prefs.edit { putInt(KEY_RING_COLOR, value) }

    var bgColor: Int
        get() = prefs.getInt(KEY_BG_COLOR, "#FFFFFF".toColorInt())
        set(value) = prefs.edit { putInt(KEY_BG_COLOR, value) }

    // 批量保存方法
    fun saveAll(isBeating: Boolean, isFullScreen: Boolean, ringColor: Int, bgColor: Int) {
        prefs.edit().apply {
            putBoolean(KEY_IS_BEATING, isBeating)
            putBoolean(KEY_IS_FULL_SCREEN, isFullScreen)
            putInt(KEY_RING_COLOR, ringColor)
            putInt(KEY_BG_COLOR, bgColor)
            apply()
        }
    }

    // 重置为默认值
    fun resetToDefault() {
        prefs.edit { clear() }
    }
}