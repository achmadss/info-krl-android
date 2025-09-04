package dev.achmad.comuline.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.achmad.comuline.util.isRunning
import dev.achmad.comuline.util.workManager
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SyncScheduleJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    private val scheduleRepository by injectLazy<ScheduleRepository>()

    override suspend fun doWork(): Result {
        return try {
            val stationId = inputData.getString(KEY_STATION_ID)
                ?: throw IllegalArgumentException("Station ID cannot be null")
            val delay = inputData.getLong(KEY_DELAY, 0)
            scheduleRepository.fetchByStationId(stationId)
            delay(delay)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {

        private const val TAG = "RefreshSchedule"
        private const val KEY_STATION_ID = "KEY_STATION_ID"
        private const val KEY_DELAY = "KEY_DELAY"

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
            val workManager = context.workManager
            if (workManager.isRunning(stationId)) {
                return false
            }

            val inputData = workDataOf(
                KEY_STATION_ID to stationId,
                KEY_DELAY to finishDelay
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

    }

}