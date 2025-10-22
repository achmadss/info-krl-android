package dev.achmad.data.repository

import dev.achmad.core.network.parseAs
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.local.dao.StationDao
import dev.achmad.data.local.entity.station.toDomain
import dev.achmad.data.local.entity.station.toStationUpdate
import dev.achmad.data.remote.InfoKRLApi
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.station.StationResponse
import dev.achmad.data.remote.model.station.toDomain
import dev.achmad.domain.station.model.Station
import dev.achmad.domain.station.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StationRepositoryImpl(
    private val api: InfoKRLApi,
    private val database: InfoKRLDatabase,
): StationRepository {

    private val stationDao: StationDao = database.stationDao()

    override fun subscribeAll(favorite: Boolean?): Flow<List<Station>> =
        stationDao.subscribeAll(favorite = favorite)
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override fun subscribeSingle(id: String): Flow<Station?> {
        return stationDao.subscribeSingle(id)
            .map { it?.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override fun subscribeMultiple(ids: List<String>): Flow<List<Station>> {
        return stationDao.subscribeMultiple(ids)
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun awaitAll(favorite: Boolean?): List<Station> {
        return stationDao.awaitAll(favorite = favorite).map { it.toDomain() }
    }

    override suspend fun awaitSingle(id: String): Station? {
        return stationDao.awaitSingle(id)?.toDomain()
    }

    override suspend fun fetch(): List<Station> {
        return api.getStations()
            .parseAs<BaseResponse<List<StationResponse>>>()
            .data.map { it.toDomain() }
    }

    override suspend fun store(stations: List<Station>) {
        val stationUpdates = stations.map { it.toStationUpdate() }
        stationDao.upsert(stationUpdates)
    }

    override suspend fun favorite(stationId: String) {
        val station = stationDao.awaitSingle(stationId)?.toDomain()
            ?: throw IllegalArgumentException("Station not found: $stationId")

        val currentFavorites = stationDao.awaitAll(favorite = true)
        val nextPosition = currentFavorites
            .mapNotNull { it.favoritePosition }
            .maxOrNull()
            ?.let { it + 1 }
            ?: 0

        val updated = station.copy(
            favorite = true,
            favoritePosition = nextPosition
        ).toStationUpdate()
        stationDao.update(updated)
    }

    override suspend fun unfavorite(stationId: String) {
        val station = stationDao.awaitSingle(stationId)?.toDomain()
            ?: throw IllegalArgumentException("Station not found: $stationId")
        val updated = station.copy(
            favorite = false,
            favoritePosition = null
        ).toStationUpdate()
        stationDao.update(updated)
    }

    override suspend fun update(stations: List<Station>) {
        val updates = stations.map { it.toStationUpdate() }
        stationDao.update(updates)
    }

}