package dev.achmad.infokrl.screens.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.di.util.inject
import dev.achmad.domain.fare.interactor.WipeFareTables
import dev.achmad.domain.route.interactor.WipeRouteTables
import dev.achmad.domain.schedule.interactor.WipeScheduleTables
import dev.achmad.domain.station.interactor.WipeStationTables
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class SettingsScreenModel(
    private val wipeStationTables: WipeStationTables = inject(),
    private val wipeScheduleTables: WipeScheduleTables = inject(),
    private val wipeRouteTables: WipeRouteTables = inject(),
    private val wipeFareTables: WipeFareTables = inject()
): ScreenModel {

    fun wipeAllData() {
        screenModelScope.launch {
            listOf(
                wipeStationTables::await,
                wipeScheduleTables::await,
                wipeRouteTables::await,
                wipeFareTables::await,
            ).map { async { it.invoke() }
            }.awaitAll()
            // TODO show indicator that the wipe is done
        }
    }
}