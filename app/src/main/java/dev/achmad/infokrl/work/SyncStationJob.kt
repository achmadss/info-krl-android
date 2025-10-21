package dev.achmad.infokrl.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.infokrl.util.isRunning
import dev.achmad.infokrl.util.workManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class SyncStationJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    private val syncStation by injectLazy<SyncStation>()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            when (val result = syncStation.await()) {
                is SyncStation.Result.Success -> {
                    Result.success()
                }
                is SyncStation.Result.Error -> {
                    result.error.printStackTrace()
                    Result.failure()
                }
            }
        }
    }

    companion object {
        private const val WORK_NAME = "RefreshStation"

        fun subscribeState(
            context: Context,
            scope: CoroutineScope,
        ): StateFlow<WorkInfo.State?> {
            val workQuery = WorkQuery.Builder
                .fromTags(listOf(WORK_NAME))
                .build()

            return context.workManager
                .getWorkInfosFlow(workQuery)
                .mapNotNull { it.firstOrNull()?.state }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.Eagerly,
                    initialValue = null
                )
        }

        fun start(
            context: Context,
        ): Boolean {
            val workManager = context.workManager
            if (workManager.isRunning(WORK_NAME)) {
                return false
            }

            val request = OneTimeWorkRequestBuilder<SyncStationJob>()
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