package dev.achmad.comuline.screens.home.station_detail

import cafe.adriel.voyager.core.model.ScreenModel
import dev.achmad.core.di.util.inject
import dev.achmad.domain.repository.RouteRepository

class StationDetailScreenModel(
    private val trainId: String,
    private val routeRepository: RouteRepository = inject()
): ScreenModel {




}