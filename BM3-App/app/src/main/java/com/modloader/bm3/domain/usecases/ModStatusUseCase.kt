package com.modloader.bm3.domain.usecases

import com.modloader.bm3.domain.interfaces.repository.ModsRepository
import com.modloader.bm3.domain.model.ModModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ModStatusUseCase @Inject constructor(
    private val modsRepository: ModsRepository
) {
    private suspend fun updateMod(mod: ModModel, newModModelFun: (ModModel) -> (ModModel)) =
        withContext(Dispatchers.IO) {
            modsRepository.updateMod(
                newModModelFun(mod)
            )
        }

    suspend fun disableMod(mod: ModModel) {
        updateMod(mod) {
            it.copy(
                isDisabled = true
            )
        }
    }


    suspend fun enableMod(mod: ModModel) {

        updateMod(mod) {
            it.copy(
                isDisabled = false
            )
        }
    }

    suspend fun markModInstalled(mod: ModModel) {
        updateMod(mod) {
            it.copy(
                isInstalled = true
            )
        }

    }

    suspend fun removeMod(mod: ModModel, modFile: File) {
        withContext(Dispatchers.IO) {
            if (modFile.exists()) {
                modFile.delete()
            }
            updateMod(mod) {
                it.copy(
                    isInstalled = false
                )
            }
//        modRepo.removeMod(mod)
        }
    }
}