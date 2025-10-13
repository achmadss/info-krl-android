package dev.achmad.comuline.util

import java.time.Duration
import java.time.LocalDateTime

fun etaString(
    now: LocalDateTime,
    target: LocalDateTime,
    compactMode: Boolean = true
): String {
    val duration = Duration.between(now, target)
    val isPast = duration.isNegative

    val seconds = duration.abs().seconds
    val minutes = seconds / 60
    val hours = minutes / 60

    if (compactMode) {
        if (isPast || duration.isZero) return "now"

        return when {
            hours > 0 -> "$hours hr"
            minutes > 0 -> "$minutes min"
            else -> "Now"
        }
    } else {
        if (duration.isZero) return "now"

        val timeStr = when {
            hours > 0 -> {
                "$hours hour${if (hours > 1) "s" else ""}"
            }
            minutes > 0 -> {
                "$minutes minute${if (minutes > 1) "s" else ""}"
            }
            else -> return "now"
        }
        return if (isPast) "$timeStr ago" else "in $timeStr"
    }
}