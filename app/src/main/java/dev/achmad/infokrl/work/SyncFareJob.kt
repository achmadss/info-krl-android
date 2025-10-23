package dev.achmad.infokrl.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.achmad.core.di.util.injectLazy
import dev.achmad.domain.fare.interactor.SyncFare
import dev.achmad.infokrl.util.isRunning
import dev.achmad.infokrl.util.workManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlin.Exception
import kotlin.IllegalStateException

class SyncFareJob(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val originStationId = inputData.getString(KEY_ORIGIN_STATION_ID)
                    ?: throw IllegalStateException("originStationId cannot be null")
                val destinationStationId = inputData.getString(KEY_DESTINATION_STATION_ID)
                    ?: throw IllegalStateException("destinationStationId cannot be null")

                val syncFare by injectLazy<SyncFare>()
                if (syncFare.shouldSync(originStationId, destinationStationId)) {
                    syncFare.await(originStationId, destinationStationId)
                }
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    companion object {

        private const val TAG = "SyncFare"
        private const val KEY_ORIGIN_STATION_ID = "KEY_ORIGIN_STATION_ID"
        private const val KEY_DESTINATION_STATION_ID = "KEY_DESTINATION_STATION_ID"

        fun subscribeState(
            context: Context,
            scope: CoroutineScope,
            originStationId: String,
            destinationStationId: String,
        ): StateFlow<WorkInfo.State?> {
            val workQuery = WorkQuery.Builder
                .fromTags(
                    listOf(
                        "$originStationId|$destinationStationId"
                    )
                )
                .addStates(
                    listOf(
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.RUNNING,
                        WorkInfo.State.BLOCKED,
                    )
                )
                .build()

            return context.workManager
                .getWorkInfosFlow(workQuery)
                .map { it.firstOrNull()?.state }
                .distinctUntilChanged()
                .stateIn(
                    scope = scope,
                    started = SharingStarted.Eagerly,
                    initialValue = null,
                )
        }

        fun start(
            context: Context,
            originStationId: String,
            destinationStationId: String,
        ): Boolean {
            val workManager = context.workManager
            if (workManager.isRunning("$originStationId|$destinationStationId")) {
                return false
            }

            val inputData = workDataOf(
                KEY_ORIGIN_STATION_ID to originStationId,
                KEY_DESTINATION_STATION_ID to destinationStationId,
            )
            val request = OneTimeWorkRequestBuilder<SyncFareJob>()
                .addTag(TAG)
                .addTag("$originStationId|$destinationStationId")
                .setInputData(inputData)
                .build()
            workManager.enqueueUniqueWork(
                "$originStationId|$destinationStationId",
                ExistingWorkPolicy.KEEP,
                request
            )
            return true
        }

    }

}