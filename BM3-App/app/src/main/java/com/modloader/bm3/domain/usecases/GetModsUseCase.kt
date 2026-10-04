package com.modloader.bm3.domain.usecases

import com.modloader.bm3.domain.interfaces.repository.ModsRepository
import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.utils.PAGINATION_LIMIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetModsUseCase @Inject constructor(
    private val repo: ModsRepository
) {

    suspend operator fun invoke(
        limit: Int = PAGINATION_LIMIT,
        page: Int = 0,
        filter: (ModModel) -> Boolean
    ): Flow<List<ModModel>> {
        return withContext(Dispatchers.IO) {
            val mods = repo.getMods(limit, page)
            return@withContext mods
                .map {
                    it.filter { modModel -> filter(modModel) }
                }
        }
    }
}