package dev.achmad.domain.fare.interactor

import dev.achmad.domain.fare.repository.FareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WipeFareTables(
    private val fareRepository: FareRepository
) {
    suspend fun await() {
        withContext(Dispatchers.IO) {
            fareRepository.deleteAll()
        }
    }
}