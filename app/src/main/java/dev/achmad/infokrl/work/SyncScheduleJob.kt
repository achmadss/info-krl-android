package dev.achmad.infokrl.work

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
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.domain.schedule.interactor.SyncSchedule
import dev.achmad.infokrl.util.isRunning
import dev.achmad.infokrl.util.workManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class SyncScheduleJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val stationId = inputData.getString(KEY_STATION_ID)
                val delay = inputData.getLong(KEY_DELAY, 0)

                val getStation by injectLazy<GetStation>()
                val syncSchedule by injectLazy<SyncSchedule>()

                if (stationId.isNullOrEmpty()) {
                    syncAllFavoriteStations(getStation, syncSchedule)
                } else {
                    if (syncSchedule.shouldSync(stationId)) {
                        syncSingleStation(stationId, syncSchedule)
                    }
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
    private suspend fun syncAllFavoriteStations(
        getStation: GetStation,
        syncSchedule: SyncSchedule
    ) {
        withContext(Dispatchers.IO) {
            val favoriteStations = getStation.awaitAll(favorite = true)
            val workManager = applicationContext.workManager
            favoriteStations.map { station ->
                async {
                    if (syncSchedule.shouldSync(station.id) && !workManager.isRunning(station.id)) {
                        when (val result = syncSchedule.await(station.id)) {
                            is SyncSchedule.Result.Error -> {
                                result.error.printStackTrace()
                            }
                            else -> Unit
                        }
                    }
                }
            }.awaitAll()
        }
    }

    /**
     * Syncs a single station schedule.
     */
    private suspend fun syncSingleStation(
        stationId: String,
        syncSchedule: SyncSchedule,
    ) {
        when (val result = syncSchedule.await(stationId)) {
            is SyncSchedule.Result.Error -> {
                throw result.error
            }
            else -> Unit
        }
    }

    companion object {

        private const val TAG = "SyncSchedule"
        private const val KEY_STATION_ID = "KEY_STATION_ID"
        private const val KEY_DELAY = "KEY_DELAY"

        fun subscribeState(
            context: Context,
            scope: CoroutineScope,
            stationId: String,
        ): StateFlow<WorkInfo.State?> {
            val workQuery = WorkQuery.Builder
                .fromTags(listOf(stationId))
                .addStates(listOf(
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.BLOCKED
                ))
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
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delayMillis = Duration.between(now, nextMidnight).toMillis()

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