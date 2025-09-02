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
import dev.achmad.comuline.work.RefreshStationJob.Companion
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.repository.ScheduleRepository
import kotlin.coroutines.cancellation.CancellationException

class RefreshScheduleJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    private val scheduleRepository by injectLazy<ScheduleRepository>()

    override suspend fun doWork(): Result {
        return try {
            val stationId = inputData.getString(KEY_STATION_ID)
                ?: throw IllegalArgumentException("Station ID cannot be null")

            scheduleRepository.refreshScheduleByStationId(stationId)
            Result.success()
        } catch (e: Exception) {
            if (e is CancellationException) {
                // assume success although cancelled
                Result.success()
            } else {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    companion object {

        private const val TAG = "RefreshSchedule"
        private const val KEY_STATION_ID = "KEY_STATION_ID"

        fun start(
            context: Context,
            stationId: String,
        ): Boolean {
            val workManager = context.workManager
            if (workManager.isRunning(stationId)) {
                return false
            }

            val inputData = workDataOf(KEY_STATION_ID to stationId)
            val request = OneTimeWorkRequestBuilder<RefreshScheduleJob>()
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