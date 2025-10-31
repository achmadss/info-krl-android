package dev.achmad.infokrl.screens.home.trip

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.util.inject
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.domain.station.model.Station
import dev.achmad.domain.transit.interactor.GetTransit
import dev.achmad.domain.transit.interactor.SyncTransit
import dev.achmad.domain.transit.model.Transit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripTabScreenModel(
    private val getStation: GetStation = inject(),
    private val syncStation: SyncStation = inject(),
    private val getTransit: GetTransit = inject(),
    private val syncTransit: SyncTransit = inject(),
): ScreenModel {

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val _syncStationResult = MutableStateFlow<SyncStation.Result>(SyncStation.Result.Success)
    val syncStationResult = _syncStationResult.asStateFlow()

    private val _syncTransitResult = MutableStateFlow<SyncTransit.Result?>(null)
    val syncTransitResult = _syncTransitResult.asStateFlow()

    private val _originStation = MutableStateFlow(Pair("", ""))
    val originStation = _originStation.asStateFlow()

    private val _destinationStation = MutableStateFlow(Pair("", ""))
    val destinationStation = _destinationStation.asStateFlow()

    private val dbStations = getStation.subscribeAll()
        .map { stations ->
            stations.ifEmpty {
                fetchStations()
                null
            }
        }.stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val stations: StateFlow<List<Station>?> = combine(
        _searchQuery,
        dbStations
    ) { query, dbList ->
        val krlStations = dbList?.filter { it.type == Station.Type.KRL }
        when {
            query.isNullOrBlank() -> krlStations
            else -> krlStations?.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun fetchStations() {
        screenModelScope.launch(Dispatchers.IO) {
            syncStation.subscribe().collect {
                _syncStationResult.update { it }
            }
        }
    }

    private val _transit = MutableStateFlow<Transit?>(null)
    val transit = _transit.asStateFlow()

    fun updateOriginStation(value: Pair<String, String>) {
        _originStation.update { value }
        resetTransit()
    }

    fun updateDestinationStation(value: Pair<String, String>) {
        _destinationStation.update { value }
        resetTransit()
    }

    private fun resetTransit() {
        _transit.update { null }
        _syncTransitResult.update { null }
    }

    fun search(query: String?) = _searchQuery.update { query }

    fun fetchTransit() {
        if (
            originStation.value.first.isNotBlank() &&
            destinationStation.value.first.isNotBlank()
        ) {
            screenModelScope.launch(Dispatchers.IO) {
                syncTransit.subscribe(
                    originStationId = originStation.value.first,
                    destinationStationId = destinationStation.value.first
                ).collect { result ->
                    _syncTransitResult.update { result }
                    if (result is SyncTransit.Result.Success || result is SyncTransit.Result.AlreadySynced) {
                        val transit = getTransit.awaitSingle(
                            originStationId = originStation.value.first,
                            destinationStationId = destinationStation.value.first
                        )
                        _transit.update { transit }
                    }
                }
            }
        }
    }
}