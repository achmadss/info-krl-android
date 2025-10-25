package dev.achmad.infokrl.screens.fare

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.fare.interactor.GetFare
import dev.achmad.domain.fare.interactor.SyncFare
import dev.achmad.domain.fare.model.Fare
import dev.achmad.domain.station.interactor.GetStation
import dev.achmad.domain.station.interactor.SyncStation
import dev.achmad.domain.station.model.Station
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

class FareCalculatorScreenModel(
    private val getFare: GetFare = inject(),
    private val syncFare: SyncFare = inject(),
    private val getStation: GetStation = inject(),
    private val syncStation: SyncStation = inject(),
): ScreenModel {

    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery = _searchQuery.asStateFlow()

    private val _syncStationResult = MutableStateFlow<SyncStation.Result>(SyncStation.Result.Success)
    val syncStationResult = _syncStationResult.asStateFlow()

    private val _syncFareResult = MutableStateFlow<SyncFare.Result?>(null)
    val syncFareResult = _syncFareResult.asStateFlow()

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

    private val _fare = MutableStateFlow<Fare?>(null)
    val fare = _fare.asStateFlow()

    fun updateOriginStation(value: Pair<String, String>) {
        _originStation.update { value }
        resetFare()
    }

    fun updateDestinationStation(value: Pair<String, String>) {
        _destinationStation.update { value }
        resetFare()
    }

    private fun resetFare() {
        _fare.update { null }
        _syncFareResult.update { null }
    }

    fun search(query: String?) = _searchQuery.update { query }

    fun fetchStations() {
        screenModelScope.launch(Dispatchers.IO) {
            syncStation.subscribe().collect {
                _syncStationResult.update { it }
            }
        }
    }

    fun fetchFare() {
        if (
            originStation.value.first.isNotBlank() &&
            destinationStation.value.first.isNotBlank()
        ) {
            screenModelScope.launch(Dispatchers.IO) {
                syncFare.subscribe(
                    originStation.value.first,
                    destinationStation.value.first,
                ).collect { result ->
                    _syncFareResult.update { result }
                    if (result is SyncFare.Result.Success) {
                        val fetchedFare = getFare.awaitSingle(
                            originStation.value.first,
                            destinationStation.value.first
                        )
                        _fare.update { fetchedFare }
                    }
                }
            }
        }
    }

}