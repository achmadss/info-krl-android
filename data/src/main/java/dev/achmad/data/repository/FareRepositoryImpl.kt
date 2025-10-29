package dev.achmad.data.repository

import android.content.res.Resources.NotFoundException
import dev.achmad.core.network.parseAs
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.local.dao.FareDao
import dev.achmad.data.local.entity.fare.toDomain
import dev.achmad.data.local.entity.fare.toEntity
import dev.achmad.data.remote.InfoKRLApi
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.fare.FareResponse
import dev.achmad.data.remote.model.fare.toEntity
import dev.achmad.domain.fare.model.Fare
import dev.achmad.domain.fare.repository.FareRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FareRepositoryImpl(
    private val api: InfoKRLApi,
    database: InfoKRLDatabase,
): FareRepository {

    private val fareDao: FareDao = database.fareDao()

    override suspend fun await(
        originStationId: String,
        destinationStationId: String
    ): Fare? {
        return fareDao.awaitSingle(originStationId, destinationStationId)?.toDomain()
    }

    override fun subscribe(
        originStationId: String,
        destinationStationId: String
    ): Flow<Fare?> {
        return fareDao.subscribeSingle(originStationId, destinationStationId)
            .map { it?.toDomain() }
    }

    override suspend fun fetch(
        originStationId: String,
        destinationStationId: String
    ): Fare {
        val response = api.getFare(originStationId, destinationStationId)
        if (response.code == 404) {
            throw NotFoundException("Invalid originStationId or destinationStationId")
        }
        val data = response.parseAs<BaseResponse<FareResponse>>()
        if (data.metadata.success == false) {
            throw Exception(data.metadata.message)
        }
        return data.data.toEntity().toDomain()
    }

    override suspend fun store(fare: Fare) {
        fareDao.insert(fare.toEntity())
    }

    override suspend fun deleteAll() {
        fareDao.deleteAll()
    }
}