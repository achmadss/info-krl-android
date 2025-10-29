package dev.achmad.infokrl.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dev.achmad.core.util.injectLazy
import dev.achmad.domain.schedule.interactor.SyncSchedule
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.infokrl.util.workManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Worker for daily periodic sync of favorite station schedules using AndroidX WorkManager.
 * Runs every 24 hours starting at midnight.
 */
class SyncFavoriteStationSchedulesJob(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val getStation by injectLazy<GetStation>()
                val syncSchedule by injectLazy<SyncSchedule>()

                val favoriteStations = getStation.awaitAll(favorite = true)
                favoriteStations.map { station ->
                    async {
                        when (val result = syncSchedule.await(station.id)) {
                            is SyncSchedule.Result.Error -> {
                                result.error.printStackTrace()
                            }
                            else -> Unit
                        }
                    }
                }.awaitAll()

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "DailySyncSchedule"

        fun scheduleDailySync(context: Context) {
            val workManager = context.workManager
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delayMillis = Duration.between(now, nextMidnight).toMillis()

            val request = PeriodicWorkRequestBuilder<SyncFavoriteStationSchedulesJob>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .addTag(TAG)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "DailyScheduleSync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
