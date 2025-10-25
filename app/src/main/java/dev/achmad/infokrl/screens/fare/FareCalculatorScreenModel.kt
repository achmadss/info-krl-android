package dev.achmad.infokrl.screens.fare

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.fare.interactor.GetFare
import dev.achmad.domain.fare.interactor.SyncFare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FareCalculatorScreenModel(
    private val getFare: GetFare = inject(),
    private val syncFare: SyncFare = inject(),
): ScreenModel {

    private val _syncFareResult = MutableStateFlow<SyncFare.Result>(SyncFare.Result.Loading)
    val syncFareResult = _syncFareResult.asStateFlow()

    private val _originStation = MutableStateFlow(Pair("", ""))
    val originStation = _originStation.asStateFlow()

    private val _destinationStation = MutableStateFlow(Pair("", ""))
    val destinationStation = _destinationStation.asStateFlow()

    val fare = combine(
        originStation,
        destinationStation,
    ) { origin, destination ->
        if (
            origin.first.isNotBlank() &&
            origin.second.isNotBlank() &&
            destination.first.isNotBlank() &&
            destination.second.isNotBlank()
        ) {
            getFare.awaitSingle(
                origin.first,
                destination.first
            ).also { if (it == null) fetchFare() }
        } else null
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    fun updateOriginStation(value: Pair<String, String>) {
        _originStation.update { value }
    }

    fun updateDestinationStation(value: Pair<String, String>) {
        _destinationStation.update { value }
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
                ).collect {
                    _syncFareResult.update { it }
                }
            }
        }
    }

}