package dev.achmad.data.repository

import android.content.res.Resources.NotFoundException
import dev.achmad.core.network.parseAs
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.local.dao.TransitDao
import dev.achmad.data.local.entity.transit.toDomain
import dev.achmad.data.local.entity.transit.toEntity as domainToEntity
import dev.achmad.data.remote.InfoKRLApi
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.transit.TransitResponse
import dev.achmad.domain.transit.model.Transit
import dev.achmad.domain.transit.repository.TransitRepository
import dev.achmad.data.remote.model.transit.toEntity as responseToEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class TransitRepositoryImpl(
    private val api: InfoKRLApi,
    database: InfoKRLDatabase
): TransitRepository {

    private val transitDao: TransitDao = database.transitDao()

    override fun subscribeAll(): Flow<List<Transit>> =
        transitDao.subscribeAll()
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override fun subscribe(
        originStationId: String,
        destinationStationId: String
    ): Flow<Transit?> = transitDao.subscribeSingle(originStationId, destinationStationId)
        .map { it?.toDomain() }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    override suspend fun fetch(
        originStationId: String,
        destinationStationId: String
    ): Transit {
        val response = api.getTransitByStationIds(
            originStationId = originStationId,
            destinationStationId = destinationStationId
        )
        if (response.code == 404) {
            throw NotFoundException("Transit from $originStationId to $destinationStationId not found")
        }
        val data = response.parseAs<BaseResponse<TransitResponse>>()
        if (data.metadata.success == false) {
            throw Exception(data.metadata.message)
        }
        return data.data.responseToEntity().toDomain()
    }

    override suspend fun await(
        originStationId: String,
        destinationStationId: String
    ): Transit? {
        return transitDao.awaitSingle(
            originStationId = originStationId,
            destinationStationId = destinationStationId
        )?.toDomain()
    }

    override suspend fun awaitAll(): List<Transit> {
        return transitDao.awaitAll().map { it.toDomain() }
    }

    override suspend fun store(transit: Transit) {
        transitDao.insert(transit.domainToEntity())
    }

    override suspend fun deleteAll() {
        transitDao.deleteAll()
    }

}