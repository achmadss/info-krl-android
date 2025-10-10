package dev.achmad.data.repository

import android.content.res.Resources.NotFoundException
import dev.achmad.core.network.parseAs
import dev.achmad.data.local.ComulineDatabase
import dev.achmad.data.local.dao.RouteDao
import dev.achmad.data.local.entity.route.toDomain
import dev.achmad.data.remote.ComulineApi
import dev.achmad.data.remote.model.BaseResponse
import dev.achmad.data.remote.model.route.RouteResponse
import dev.achmad.data.remote.model.route.toEntity
import dev.achmad.domain.model.Route
import dev.achmad.domain.repository.RouteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RouteRepositoryImpl(
    private val api: ComulineApi,
    private val database: ComulineDatabase
): RouteRepository {

    private val routeDao: RouteDao = database.routeDao()

    override val routes: Flow<List<Route>> =
        routeDao.subscribeAll()
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    override suspend fun fetchAndStoreByTrainId(trainId: String) {
        withContext(Dispatchers.IO) {
            val response = api.getRouteByTrainId(trainId)
            if (response.code == 404) {
                throw NotFoundException("trainId not found")
            }
            val data = response.parseAs<BaseResponse<RouteResponse>>()
            if (data.metadata.success == false) {
                throw Exception(data.metadata.message)
            }
            routeDao.insert(data.data.toEntity())
        }
    }

    override fun subscribeSingle(trainId: String): Flow<Route?> {
        return routeDao.subscribeSingle(trainId)
            .map { it?.toDomain() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
    }

}