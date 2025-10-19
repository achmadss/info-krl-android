package dev.achmad.data.repository

import dev.achmad.core.network.parseAs
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.local.dao.StationDao
import dev.achmad.data.local.entity.station.toDomain
import dev.achmad.data.local.entity.station.toEntity
import dev.achmad.data.remote.InfoKRLApi
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.station.StationResponse
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
    private val api: InfoKRLApi,
    private val database: InfoKRLDatabase,
): StationRepository {

    private val stationDao: StationDao = database.stationDao()

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

    override suspend fun fetchAndStore() {
        withContext(Dispatchers.IO) {
            val response = api.getStations()
            val stationUpdates = response
                .parseAs<BaseResponse<List<StationResponse>>>().data
                .map { it.toStationUpdate() }
            stationDao.upsert(stationUpdates)
        }
    }

    override suspend fun toggleFavorite(station: Station) {
        withContext(Dispatchers.IO) {
            val updated = station.copy(
                favorite = !station.favorite,
                favoritePosition = if (!station.favorite) null else station.favoritePosition
            ).toEntity()
            stationDao.update(updated)
        }
    }

    override suspend fun updateFavorite(station: Station) {
        withContext(Dispatchers.IO) {
            val updated = station.toEntity()
            stationDao.update(updated)
        }
    }

    override suspend fun reorderFavorites(stations: List<Station>) {
        withContext(Dispatchers.IO) {
            val updates = stations.mapIndexed { index, station ->
                station.copy(favoritePosition = index).toEntity()
            }
            stationDao.update(updates)
        }
    }

    override suspend fun awaitAllFavorites(): List<Station> {
        return withContext(Dispatchers.IO) {
            stationDao.awaitAll(favorite = true).map { it.toDomain() }
        }
    }

    override suspend fun awaitSingle(id: String): Station? {
        return stationDao.awaitSingle(id)?.toDomain()
    }

    override fun subscribeSingle(id: String): Flow<Station?> {
        return stationDao.subscribeSingle(id)
            .map { it?.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

}