package com.modloader.bm3.domain.usecases

import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.domain.repository.ModsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetInstalledModsUseCase @Inject constructor(
    private val repo: ModsRepository
) {

    suspend operator fun invoke(limit: Int = 20, page: Int = 0): Flow<List<ModModel>> {
        return repo.getMods(limit, page).map {
            it.filter { modModel -> modModel.isInstalled }
        }
    }
}