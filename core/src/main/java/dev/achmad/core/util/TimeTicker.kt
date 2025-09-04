package dev.achmad.core.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TimeTicker(
    private val unit: TickUnit
) {
    enum class TickUnit {
        SECOND, MINUTE, HOUR
    }

    val ticks: Flow<LocalDateTime?> = flow {
        var lastValue: Long? = null
        while (true) {
            val now = LocalDateTime.now()
            val current = when (unit) {
                TickUnit.SECOND -> now.truncatedTo(ChronoUnit.SECONDS).second.toLong()
                TickUnit.MINUTE -> now.truncatedTo(ChronoUnit.MINUTES).minute.toLong()
                TickUnit.HOUR -> now.truncatedTo(ChronoUnit.HOURS).hour.toLong()
            }

            if (current != lastValue) {
                lastValue = current
                emit(now)
            }

            delay(1000) // check every second
        }
    }
}
