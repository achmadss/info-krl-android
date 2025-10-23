package dev.achmad.data.remote

import dev.achmad.core.network.GET
import dev.achmad.core.network.NetworkHelper
import dev.achmad.core.network.await
import dev.achmad.domain.preference.ApplicationPreference
import okhttp3.Response

class InfoKRLApi(
    private val networkHelper: NetworkHelper,
    private val preference: ApplicationPreference,
) {
    suspend fun getStations(): Response {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/krl/station/")
        ).await()
    }

    suspend fun getStationById(id: String): Response {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/krl/station/$id")
        ).await()
    }

    suspend fun getScheduleByStationId(stationId: String): Response {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/krl/schedule/$stationId")
        ).await()
    }

    suspend fun getRouteByTrainId(trainId: String): Response {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/krl/route/$trainId")
        ).await()
    }
}