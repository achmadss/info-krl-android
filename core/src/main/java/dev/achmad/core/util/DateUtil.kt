package dev.achmad.core.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun String.toLocalDateTime(
    pattern: String = "yyyy-MM-dd HH:mm:ss.nX"
): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return LocalDateTime.parse(this, formatter)
}

fun LocalDateTime.format(
    pattern: String = "yyyy-MM-dd HH:mm:ss.nX"
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return format(formatter)
}