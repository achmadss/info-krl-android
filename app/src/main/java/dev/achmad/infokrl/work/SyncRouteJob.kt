package dev.achmad.infokrl.work

import android.content.Context
import android.content.res.Resources.NotFoundException
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.route.interactor.SyncRoute
import dev.achmad.infokrl.util.isRunning
import dev.achmad.infokrl.util.workManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class SyncRouteJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val trainId = inputData.getString(KEY_TRAIN_ID)
                    ?: throw IllegalArgumentException("Train ID cannot be null")
                val delay = inputData.getLong(KEY_DELAY, 0)
                val syncRoute by injectLazy<SyncRoute>()

                if (syncRoute.shouldSync(trainId)) {
                    when (val result = syncRoute.await(trainId)) {
                        is SyncRoute.Result.Error -> {
                            if (result.error !is NotFoundException) {
                                throw result.error
                            }
                        }
                        else -> Unit
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

    companion object {

        private const val TAG = "SyncRoute"
        private const val KEY_TRAIN_ID = "KEY_TRAIN_ID"
        private const val KEY_DELAY = "KEY_DELAY"

        fun subscribeState(
            context: Context,
            scope: CoroutineScope,
            trainId: String,
        ): StateFlow<WorkInfo.State?> {
            val workQuery = WorkQuery.Builder
                .fromTags(listOf(trainId))
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
            trainId: String,
            finishDelay: Long = 0
        ): Boolean {
            val workManager = context.workManager
            if (workManager.isRunning(trainId)) {
                return false
            }

            val inputData = workDataOf(
                KEY_TRAIN_ID to trainId,
                KEY_DELAY to finishDelay,
            )
            val request = OneTimeWorkRequestBuilder<SyncRouteJob>()
                .addTag(TAG)
                .addTag(trainId)
                .setInputData(inputData)
                .build()

            workManager.enqueue(request)
            return true
        }

        fun stop(
            context: Context,
            trainId: String,
        ) {
            val workManager = context.workManager
            val workQuery = WorkQuery.Builder
                .fromTags(listOf(trainId))
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