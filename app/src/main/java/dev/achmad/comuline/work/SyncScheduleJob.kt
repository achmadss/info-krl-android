package dev.achmad.comuline.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.achmad.comuline.base.ApplicationPreference
import dev.achmad.comuline.util.isRunning
import dev.achmad.comuline.util.workManager
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class SyncScheduleJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    private val scheduleRepository by injectLazy<ScheduleRepository>()
    private val stationRepository by injectLazy<StationRepository>()
    private val applicationPreference by injectLazy<ApplicationPreference>()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val stationId = inputData.getString(KEY_STATION_ID)
                val delay = inputData.getLong(KEY_DELAY, 0)
                val zone = ZoneId.systemDefault()
                val now = LocalDateTime.ofInstant(Instant.now(), zone)

                if (stationId.isNullOrEmpty()) {
                    syncAllFavoriteStations(now, zone)
                } else {
                    syncSingleStation(stationId, now, zone)
                }

                delay(delay)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    /**
     * Syncs all favorite stations for the daily periodic sync.
     * Skips stations that already have a running worker to prevent duplicate work.
     */
    private suspend fun syncAllFavoriteStations(now: LocalDateTime, zone: ZoneId) {
        val favoriteStations = stationRepository.awaitAllFavorites()
        val workManager = applicationContext.workManager

        favoriteStations.forEach { station ->
            // Check if sync is needed AND no worker is already running for this station
            if (shouldSync(station.id) && !workManager.isRunning(station.id)) {
                val lastFetchSchedule = applicationPreference.lastFetchSchedule(station.id)
                maxSchedulePermits.withPermit {
                    scheduleRepository.fetchAndStoreByStationId(station.id)
                    lastFetchSchedule.set(now.atZone(zone).toInstant().toEpochMilli())
                }
            }
        }
    }

    /**
     * Syncs a single station schedule.
     */
    private suspend fun syncSingleStation(stationId: String, now: LocalDateTime, zone: ZoneId) {
        val lastFetchSchedule = applicationPreference.lastFetchSchedule(stationId)
        maxSchedulePermits.withPermit {
            scheduleRepository.fetchAndStoreByStationId(stationId)
            lastFetchSchedule.set(now.atZone(zone).toInstant().toEpochMilli())
        }
    }

    companion object {

        private const val TAG = "RefreshSchedule"
        private const val KEY_STATION_ID = "KEY_STATION_ID"
        private const val KEY_DELAY = "KEY_DELAY"

        val maxSchedulePermits =  Semaphore(5)

        private fun shouldSync(
            stationId: String,
        ): Boolean {
            val applicationPreference by injectLazy<ApplicationPreference>()
            val lastFetchSchedule = applicationPreference.lastFetchSchedule(stationId)
            val zone = ZoneId.systemDefault()
            val now = LocalDateTime.ofInstant(Instant.now(), zone)
            val lastFetch = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastFetchSchedule.get()),
                zone
            )
            return now.toLocalDate().isAfter(lastFetch.toLocalDate())
        }

        fun subscribeState(
            context: Context,
            scope: CoroutineScope,
            stationId: String,
        ): StateFlow<WorkInfo.State?> {
            val workQuery = WorkQuery.Builder
                .fromTags(listOf(stationId))
                .build()

            return context.workManager
                .getWorkInfosFlow(workQuery)
                .map { it.firstOrNull()?.state }
                .distinctUntilChanged()
                .stateIn(
                    scope = scope,
                    started = SharingStarted.Eagerly,
                    initialValue = null
                )
        }

        fun start(
            context: Context,
            stationId: String,
            finishDelay: Long = 0
        ): Boolean {
            if (!shouldSync(stationId)) {
                return false
            }
            return startNow(context, stationId, finishDelay)
        }

        fun startNow(
            context: Context,
            stationId: String,
            finishDelay: Long = 0
        ): Boolean {
            val workManager = context.workManager
            if (workManager.isRunning(stationId)) {
                return false
            }

            val inputData = workDataOf(
                KEY_STATION_ID to stationId,
                KEY_DELAY to finishDelay,
            )
            val request = OneTimeWorkRequestBuilder<SyncScheduleJob>()
                .addTag(TAG)
                .addTag(stationId)
                .setInputData(inputData)
                .build()
            workManager.enqueueUniqueWork(stationId, ExistingWorkPolicy.KEEP, request)
            return true
        }

        fun stop(
            context: Context,
            stationId: String,
        ) {
            val workManager = context.workManager
            val workQuery = WorkQuery.Builder
                .fromTags(listOf(stationId))
                .addStates(listOf(WorkInfo.State.RUNNING))
                .build()
            workManager
                .getWorkInfos(workQuery).get()
                .forEach { workManager.cancelWorkById(it.id) }
        }

        fun stopAll(
            context: Context
        ) {
            val workManager = context.workManager
            val workQuery = WorkQuery.Builder
                .fromTags(listOf(TAG))
                .addStates(listOf(WorkInfo.State.RUNNING))
                .build()
            workManager
                .getWorkInfos(workQuery).get()
                .forEach { workManager.cancelWorkById(it.id) }
        }

        fun scheduleDailySync(context: Context) {
            val workManager = context.workManager

            // Calculate delay until next midnight
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delayMillis = java.time.Duration.between(now, nextMidnight).toMillis()

            val request = PeriodicWorkRequestBuilder<SyncScheduleJob>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .addTag(TAG)
                .addTag("DAILY_SYNC")
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        KEY_STATION_ID to "",  // Empty string signals sync all favorites
                        KEY_DELAY to 0L
                    )
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                "DailyScheduleSync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

    }

}