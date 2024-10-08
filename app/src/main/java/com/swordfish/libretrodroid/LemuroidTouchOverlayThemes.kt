package com.swordfish.libretrodroid

import android.graphics.Color
import android.view.View
import com.google.android.material.color.MaterialColors
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.google.android.material.R as MaterialR

object LemuroidTouchOverlayThemes {
    private const val ALPHA_FILL_LIGHT = 0.2f
    private const val ALPHA_FILL_STRONG = 0.2f
    private const val ALPHA_FILL_SIMULATED = 0.4f
    private const val ALPHA_FILL_PRESSED = 0.6f

    private const val ALPHA_STROKE = 0.9f
    private const val ALPHA_STROKE_LIGHT = 0.15f * ALPHA_STROKE
    private const val ALPHA_STROKE_STRONG = 0.5f * ALPHA_STROKE
    private const val ALPHA_STROKE_TEXT = 0.8f * ALPHA_STROKE

    private const val BACKGROUND_COLOR = Color.BLACK

    fun getGamePadTheme(): RadialGamePadTheme {
        return buildTheme()
    }

    private fun buildTheme(): RadialGamePadTheme {
        val strokeSize = 2.0f
        val colorOnSurface = Color.WHITE
        val colorPrimary = Color.RED
        val colorSecondary = Color.BLUE

        return RadialGamePadTheme(
            normalColor = withAlpha(BACKGROUND_COLOR, ALPHA_FILL_STRONG),
            normalStrokeColor = withAlpha(colorOnSurface, ALPHA_STROKE_STRONG),
            backgroundColor = withAlpha(BACKGROUND_COLOR, ALPHA_FILL_LIGHT),
            backgroundStrokeColor = withAlpha(colorOnSurface, ALPHA_STROKE_LIGHT),
            pressedColor = withAlpha(colorPrimary, ALPHA_FILL_PRESSED),
            textColor = withAlpha(colorOnSurface, ALPHA_STROKE_TEXT),
            simulatedColor = withAlpha(colorSecondary, ALPHA_FILL_SIMULATED),
            lightColor = withAlpha(BACKGROUND_COLOR, ALPHA_FILL_STRONG),
            lightStrokeColor = withAlpha(colorOnSurface, ALPHA_STROKE_LIGHT),
            strokeWidthDp = strokeSize,
        )
    }

    private fun withAlpha(
        color: Int,
        alpha: Float,
    ): Int {
        val alphaInt = (alpha * 255).toInt()
        return MaterialColors.compositeARGBWithAlpha(color, alphaInt)
    }
}
