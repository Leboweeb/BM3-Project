package com.modloader.bm3.domain.usecases

import com.modloader.bm3.domain.interfaces.repository.ModsRepository
import com.modloader.bm3.domain.model.ModModel
import javax.inject.Inject

class MarkModInstalledUseCase @Inject constructor(
    private val repo: ModsRepository
) {
    suspend operator fun invoke(mod: ModModel) {
        repo.updateMod(
            mod.copy(
                isInstalled = true
            )
        )
    }
}