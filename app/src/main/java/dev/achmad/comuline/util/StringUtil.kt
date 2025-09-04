package dev.achmad.comuline.util

import java.time.Duration
import java.time.LocalDateTime

fun etaString(now: LocalDateTime, target: LocalDateTime): String {
    val duration = Duration.between(now, target)

    if (duration.isNegative || duration.isZero) return "now"

    val seconds = duration.seconds
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> if (hours == 1L) "in an hour" else "in $hours hours"
        minutes > 0 -> if (minutes == 1L) "in a minute" else "in $minutes minutes"
        else -> "now"
    }
}
