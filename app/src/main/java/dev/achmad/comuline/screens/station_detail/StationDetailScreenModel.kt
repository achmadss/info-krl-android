package dev.achmad.comuline.screens.station_detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.repository.ScheduleRepository
import dev.achmad.domain.repository.StationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn

class StationDetailScreenModel(
    private val stationId: String,
    private val stationRepository: StationRepository = inject(),
    private val scheduleRepository: ScheduleRepository = inject(),
): ScreenModel {

    val station = stationRepository.subscribeSingle(stationId)
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )


}