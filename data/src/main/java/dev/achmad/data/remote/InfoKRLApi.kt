package dev.achmad.data.remote

import dev.achmad.core.network.GET
import dev.achmad.core.network.NetworkHelper
import dev.achmad.core.network.await
import okhttp3.Response

class InfoKRLApi(
    private val networkHelper: NetworkHelper,
    private val preference: InfoKRLApiPreference,
) {
    suspend fun getStations(): Response {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/station/")
        ).await()
    }

    suspend fun getStationById(id: String): Response {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/station/$id")
        ).await()
    }

    suspend fun getScheduleByStationId(stationId: String): Response {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/schedule/$stationId")
        ).await()
    }

    suspend fun getRouteByTrainId(trainId: String): Response {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/route/$trainId")
        ).await()
    }
}