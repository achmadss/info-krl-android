package dev.achmad.comuline.util

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun String?.toColor(): Color {
    if (this == null) return Color.Transparent
    return Color(this.toColorInt())
}