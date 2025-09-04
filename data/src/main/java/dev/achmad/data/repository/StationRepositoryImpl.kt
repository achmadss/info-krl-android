package dev.achmad.data.repository

import dev.achmad.data.local.ComulineDatabase
import dev.achmad.data.local.dao.StationDao
import dev.achmad.data.local.entity.station.toDomain
import dev.achmad.data.local.entity.station.toEntity
import dev.achmad.data.remote.ComulineApi
import dev.achmad.data.remote.model.station.toStationUpdate
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StationRepositoryImpl(
    private val api: ComulineApi,
    private val database: ComulineDatabase,
    private val stationDao: StationDao = database.stationDao(),
): StationRepository {

    override val stations: Flow<List<Station>> =
        stationDao.subscribeAll()
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override val favoriteStations: Flow<List<Station>> =
        stationDao.subscribeAll(favorite = true)
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val stationUpdates = api.getStations().data.map { it.toStationUpdate() }
            stationDao.upsert(stationUpdates)
        }
    }

    override suspend fun toggleFavorite(station: Station) {
        withContext(Dispatchers.IO) {
            val updated = station.copy(favorite = !station.favorite).toEntity()
            stationDao.update(updated)
        }
    }

    override suspend fun awaitSingle(id: String): Station? {
        return stationDao.awaitSingle(id)?.toDomain()
    }

}