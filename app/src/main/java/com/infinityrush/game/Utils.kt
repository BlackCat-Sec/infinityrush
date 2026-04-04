package com.infinityrush.game

import android.content.Context
import android.graphics.RectF
import android.util.TypedValue

object Utils {
    fun dpToPx(context: Context, value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        )
    }

    fun spToPx(context: Context, value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            context.resources.displayMetrics
        )
    }

    fun isInside(rect: RectF, x: Float, y: Float): Boolean = rect.contains(x, y)

    fun clamp(value: Float, minValue: Float, maxValue: Float): Float {
        return value.coerceIn(minValue, maxValue)
    }

    fun getHighScore(context: Context): Int {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(Constants.HIGH_SCORE_KEY, 0)
    }

    fun saveHighScore(context: Context, highScore: Int) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(Constants.HIGH_SCORE_KEY, highScore)
            .apply()
    }
}

