package dev.achmad.comuline.util

import android.content.Context
import dev.achmad.comuline.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun etaString(
    context: Context,
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
        if (isPast || duration.isZero) return context.getString(R.string.time_now)

        return when {
            hours > 0 -> "$hours ${context.getString(R.string.time_hr)}"
            minutes > 0 -> "$minutes ${context.getString(R.string.time_min)}"
            else -> context.getString(R.string.time_now_capitalized)
        }
    } else {
        if (duration.isZero) return context.getString(R.string.time_now)

        val timeStr = when {
            hours > 0 -> {
                val hourStr = if (hours > 1) {
                    context.getString(R.string.time_hours)
                } else {
                    context.getString(R.string.time_hour)
                }
                "$hours $hourStr"
            }
            minutes > 0 -> {
                val minuteStr = if (minutes > 1) {
                    context.getString(R.string.time_minutes)
                } else {
                    context.getString(R.string.time_minute)
                }
                "$minutes $minuteStr"
            }
            else -> return context.getString(R.string.time_now)
        }
        return if (isPast) {
            "$timeStr ${context.getString(R.string.time_ago)}"
        } else {
            "${context.getString(R.string.time_in)} $timeStr"
        }
    }
}

fun timeFormatter(is24Hour: Boolean): DateTimeFormatter {
    return if (is24Hour) {
        DateTimeFormatter.ofPattern("HH:mm")
    } else {
        DateTimeFormatter.ofPattern("h:mm a")
    }
}