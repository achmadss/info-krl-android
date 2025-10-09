package dev.achmad.comuline.screens.home.station_detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.model.Schedule
import dev.achmad.domain.repository.RouteRepository
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StationDetailScreenModel(
    private val trainId: String,
    private val routeRepository: RouteRepository = inject()
): ScreenModel {




}