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
import dev.achmad.domain.usecase.route.SyncRoute
import dev.achmad.infokrl.base.ApplicationPreference
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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SyncRouteJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    private val syncRoute by injectLazy<SyncRoute>()
    private val applicationPreference by injectLazy<ApplicationPreference>()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val zone = ZoneId.systemDefault()
            val now = LocalDateTime.ofInstant(Instant.now(), zone)

            try {
                val trainIds = inputData.getStringArray(KEY_TRAIN_ID)
                    ?: throw IllegalArgumentException("Train IDs cannot be null")
                val delay = inputData.getLong(KEY_DELAY, 0)

                trainIds.map { trainId ->
                    async {
                        val lastFetchRoute = applicationPreference.lastFetchRoute(trainId)
                        when (val result = syncRoute.await(trainId)) {
                            is SyncRoute.Result.Success -> {
                                lastFetchRoute.set(now.atZone(zone).toInstant().toEpochMilli())
                            }
                            is SyncRoute.Result.Error -> {
                                result.error.printStackTrace()
                                if (result.error is NotFoundException) {
                                    lastFetchRoute.set(now.atZone(zone).toInstant().toEpochMilli())
                                }
                            }
                        }
                    }
                }.awaitAll()

                delay(delay)
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    companion object {

        private const val TAG = "RefreshRoute"
        private const val KEY_TRAIN_ID = "KEY_TRAIN_ID"
        private const val KEY_DELAY = "KEY_DELAY"

//        val maxRoutePermits = Semaphore(10)

        fun shouldSync(
            trainId: String,
        ): Boolean {
            val applicationPreference by injectLazy<ApplicationPreference>()
            val lastFetchRoute = applicationPreference.lastFetchRoute(trainId)
            val zone = ZoneId.systemDefault()
            val now = LocalDateTime.ofInstant(Instant.now(), zone)
            val lastFetch = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastFetchRoute.get()),
                zone
            )
            return now.toLocalDate().isAfter(lastFetch.toLocalDate())
        }

        fun subscribeState(
            context: Context,
            scope: CoroutineScope,
            trainIds: List<String>,
        ): StateFlow<WorkInfo.State?> {
            val workQuery = WorkQuery.Builder
                .fromTags(trainIds)
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

        fun subscribeStateByStation(
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
            trainIds: List<String>,
            stationId: String? = null,
            finishDelay: Long = 0
        ): Boolean {
            val trainIdsToStart = trainIds.toMutableList()
            trainIds.forEach {
                if (!shouldSync(it)) {
                    trainIdsToStart.remove(it)
                }
            }
            if (trainIdsToStart.isEmpty()) return false
            return startNow(context, trainIdsToStart, stationId, finishDelay)
        }


        fun startNow(
            context: Context,
            trainIds: List<String>,
            stationId: String? = null,
            finishDelay: Long = 0
        ): Boolean {
            val workManager = context.workManager
            val trainIdsToEnqueue = trainIds.toMutableList()

            trainIds.forEach { trainId ->
                if (workManager.isRunning(trainId)) {
                    trainIdsToEnqueue.remove(trainId)
                }
            }
            if (trainIdsToEnqueue.isEmpty()) return false

            // FIXED: Use trainIdsToEnqueue instead of trainIds to prevent duplicate requests
            val inputData = workDataOf(
                KEY_TRAIN_ID to trainIdsToEnqueue.toTypedArray(),
                KEY_DELAY to finishDelay,
            )
            val request = OneTimeWorkRequestBuilder<SyncRouteJob>()
                .addTag(TAG)
                .setInputData(inputData)

            trainIdsToEnqueue.forEach {
                request.addTag(it)
            }

            // Add stationId as a tag if provided
            if (stationId != null) {
                request.addTag(stationId)
            }

            workManager.enqueue(
                request = request.build()
            )
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