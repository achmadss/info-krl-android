package dev.achmad.data.remote

import dev.achmad.core.network.GET
import dev.achmad.core.network.NetworkHelper
import dev.achmad.core.network.await
import dev.achmad.core.network.parseAs
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.schedule.ScheduleResponse
import dev.achmad.data.remote.model.station.StationResponse

class ComulineApi(
    private val networkHelper: NetworkHelper,
    private val preference: ComulineApiPreference,
) {
    suspend fun getStations(): BaseResponse<List<StationResponse>> {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/station/")
        ).await().parseAs<BaseResponse<List<StationResponse>>>()
    }

    suspend fun getStationById(id: String): BaseResponse<StationResponse> {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/station/$id")
        ).await().parseAs<BaseResponse<StationResponse>>()
    }

    suspend fun getScheduleByStationId(stationId: String): BaseResponse<List<ScheduleResponse>> {
        return networkHelper.client.newCall(
            GET(preference.baseUrl().get() + "/v1/schedule/$stationId")
        ).await().parseAs<BaseResponse<List<ScheduleResponse>>>()
    }
}