package dev.achmad.core.util

import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun String.toLocalDateTime(): LocalDateTime {
    // remove fractional seconds and timezone (e.g. .448+00 or +00)
    val cleaned = this.replace(Regex("(?<=\\d{2}:\\d{2}:\\d{2})(?:\\.\\d+)?(?:Z|[+-]\\d{2}(?::?\\d{2})?)?$"), "")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val utcDateTime = LocalDateTime.parse(cleaned, formatter)

    // treat parsed time as UTC and convert to system default
    return utcDateTime.atOffset(ZoneOffset.UTC)
        .atZoneSameInstant(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun LocalDateTime.format(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return this.format(formatter)
}
