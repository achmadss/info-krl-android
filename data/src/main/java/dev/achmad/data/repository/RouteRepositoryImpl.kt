package dev.achmad.data.repository

import android.content.res.Resources.NotFoundException
import dev.achmad.core.network.parseAs
import dev.achmad.data.local.InfoKRLDatabase
import dev.achmad.data.local.dao.RouteDao
import dev.achmad.data.local.entity.route.toDomain
import dev.achmad.data.local.entity.route.toEntity as domainToEntity
import dev.achmad.data.remote.InfoKRLApi
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.route.RouteResponse
import dev.achmad.domain.route.model.Route
import dev.achmad.domain.route.repository.RouteRepository
import dev.achmad.data.remote.model.route.toEntity as responseToEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RouteRepositoryImpl(
    private val api: InfoKRLApi,
    database: InfoKRLDatabase
): RouteRepository {

    private val routeDao: RouteDao = database.routeDao()

    override fun subscribeAll(): Flow<List<Route>> =
        routeDao.subscribeAll()
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override fun subscribe(trainId: String): Flow<Route?> {
        return routeDao.subscribeSingle(trainId)
            .map { it?.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun fetch(trainId: String): Route {
        val response = api.getRouteByTrainId(trainId)
        if (response.code == 404) {
            throw NotFoundException("trainId not found")
        }
        val data = response.parseAs<BaseResponse<RouteResponse>>()
        if (data.metadata.success == false) {
            throw Exception(data.metadata.message)
        }
        return data.data.responseToEntity().toDomain()
    }

    override suspend fun awaitAll(trainId: String): List<Route> {
        return routeDao.awaitAllByTrainId(trainId).toDomain()
    }

    override suspend fun store(route: Route) {
        routeDao.insert(route.domainToEntity())
    }

}