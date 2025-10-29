package dev.achmad.domain.transit.interactor

import dev.achmad.domain.transit.repository.TransitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WipeTransitTables(
    private val transitRepository: TransitRepository
) {

    suspend fun await() {
        withContext(Dispatchers.IO) {
            transitRepository.deleteAll()
        }
    }

}