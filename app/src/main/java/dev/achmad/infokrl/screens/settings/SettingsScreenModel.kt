package dev.achmad.infokrl.screens.settings

import cafe.adriel.voyager.core.model.ScreenModel
import dev.achmad.core.di.util.inject
import dev.achmad.domain.route.interactor.WipeRouteTables
import dev.achmad.domain.schedule.interactor.WipeScheduleTables
import dev.achmad.domain.station.interactor.WipeStationTables

class SettingsScreenModel(
    private val wipeStationTables: WipeStationTables = inject(),
    private val wipeScheduleTables: WipeScheduleTables = inject(),
    private val wipeRouteTables: WipeRouteTables = inject()
): ScreenModel {

    suspend fun wipeAllData() {
        wipeStationTables.execute()
        wipeScheduleTables.execute()
        wipeRouteTables.execute()
    }
    // TODO add indicator when wipe data is done
}