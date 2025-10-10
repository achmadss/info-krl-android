package dev.achmad.comuline.work

import android.content.Context
import android.content.res.Resources.NotFoundException
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.achmad.comuline.base.ApplicationPreference
import dev.achmad.comuline.util.isRunning
import dev.achmad.comuline.util.workManager
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.repository.RouteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SyncRouteJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    private val routeRepository by injectLazy<RouteRepository>()
    private val applicationPreference by injectLazy<ApplicationPreference>()

    override suspend fun doWork(): Result {
        var trainId: String? = null
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.ofInstant(Instant.now(), zone)

        return try {
            trainId = inputData.getString(KEY_TRAIN_ID)
                ?: throw IllegalArgumentException("Station ID cannot be null")
            val delay = inputData.getLong(KEY_DELAY, 0)
            val lastFetchSchedule = applicationPreference.lastFetchRoute(trainId)

            routeRepository.fetchAndStoreByTrainId(trainId)
            lastFetchSchedule.set(now.atZone(zone).toInstant().toEpochMilli())

            delay(delay)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NotFoundException) {
                trainId?.let {
                    val lastFetchSchedule = applicationPreference.lastFetchRoute(trainId)
                    lastFetchSchedule.set(now.atZone(zone).toInstant().toEpochMilli())
                }
            }
            Result.failure()
        }
    }

    companion object {

        private const val TAG = "RefreshRoute"
        private const val KEY_TRAIN_ID = "KEY_TRAIN_ID"
        private const val KEY_DELAY = "KEY_DELAY"

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
            trainId: String,
            finishDelay: Long = 0
        ): Boolean {
            if (!shouldSync(trainId)) {
                return false
            }
            return startNow(context, trainId, finishDelay)
        }

        fun startNow(
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
            workManager.enqueueUniqueWork(trainId, ExistingWorkPolicy.KEEP, request)
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