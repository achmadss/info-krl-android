package dev.achmad.infokrl.screens.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.achmad.core.util.inject
import dev.achmad.core.util.injectContext
import dev.achmad.core.util.ToastHelper
import dev.achmad.domain.fare.interactor.WipeFareTables
import dev.achmad.domain.route.interactor.WipeRouteTables
import dev.achmad.domain.schedule.interactor.WipeScheduleTables
import dev.achmad.domain.station.interactor.WipeStationTables
import dev.achmad.infokrl.R
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class SettingsScreenModel(
    private val wipeStationTables: WipeStationTables = inject(),
    private val wipeScheduleTables: WipeScheduleTables = inject(),
    private val wipeRouteTables: WipeRouteTables = inject(),
    private val wipeFareTables: WipeFareTables = inject(),
    private val toastHelper: ToastHelper = inject(),
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
            toastHelper.show(injectContext().getString(R.string.clear_local_data_success))
        }
    }
}