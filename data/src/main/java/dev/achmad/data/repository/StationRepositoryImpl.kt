package dev.achmad.data.repository

import dev.achmad.data.local.dao.StationDao
import dev.achmad.data.local.entity.station.toDomain
import dev.achmad.data.local.entity.station.toEntity
import dev.achmad.data.remote.ComulineApi
import dev.achmad.data.remote.model.station.toStationUpdate
import dev.achmad.domain.model.Station
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StationRepositoryImpl(
    private val stationDao: StationDao,
    private val api: ComulineApi,
): StationRepository {
    override val stations: Flow<List<Station>> =
        stationDao.subscribeAll().map { it.toDomain() }

    override val favoriteStations: Flow<List<Station>> =
        stationDao.subscribeAll(favorite = true).map { it.toDomain() }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val stationUpdates = api.getStations().data.map { it.toStationUpdate() }
            stationDao.upsert(stationUpdates)
        }
    }

    override suspend fun toggleFavorite(station: Station, favorite: Boolean) {
        withContext(Dispatchers.IO) {
            stationDao.update(station.toEntity().copy(favorite = favorite))
        }
    }

    override fun subscribeSingle(id: String): Flow<Station?> {
        return stationDao.subscribeSingle(id).map { it?.toDomain() }
    }

}