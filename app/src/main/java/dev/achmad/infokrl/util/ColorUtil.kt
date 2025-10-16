package dev.achmad.infokrl.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt

fun String?.toColor(): Color {
    if (this == null) return Color.Transparent
    return Color(this.toColorInt())
}

fun Color.brighter(factor: Float = 0.2f): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)
    hsl[2] = (hsl[2] + factor).coerceAtMost(1f) // increase lightness
    return Color(ColorUtils.HSLToColor(hsl))
}

fun Color.darken(factor: Float = 0.2f): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)
    hsl[2] = (hsl[2] - factor).coerceAtLeast(0f) // decrease lightness
    return Color(ColorUtils.HSLToColor(hsl))
}