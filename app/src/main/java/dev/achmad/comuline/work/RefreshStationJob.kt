package dev.achmad.comuline.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import dev.achmad.comuline.util.isRunning
import dev.achmad.comuline.util.workManager
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.CancellationException

class RefreshStationJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    private val stationRepository by injectLazy<StationRepository>()

    override suspend fun doWork(): Result {
        return try {
            stationRepository.refresh()
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
        private const val WORK_NAME = "RefreshStation"

        fun start(
            context: Context,
        ): Boolean {
            val workManager = context.workManager
            if (workManager.isRunning(WORK_NAME)) {
                return false
            }

            val request = OneTimeWorkRequestBuilder<RefreshStationJob>()
                .addTag(WORK_NAME)
                .build()
            workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
            return true
        }

        fun stop(
            context: Context,
        ) {
            val workManager = context.workManager
            val workQuery = WorkQuery.Builder
                .fromTags(listOf(WORK_NAME))
                .addStates(listOf(WorkInfo.State.RUNNING))
                .build()
            workManager
                .getWorkInfos(workQuery).get()
                .forEach { workManager.cancelWorkById(it.id) }
        }

    }

}