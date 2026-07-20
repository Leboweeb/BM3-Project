package com.modloader.bm3.domain.repository

import com.modloader.bm3.domain.model.ModModel
import kotlinx.coroutines.flow.Flow

interface ModsRepository {
    suspend fun getMods(limit : Int = 20, page : Int = 0) : Flow<List<ModModel>>
    suspend fun queryMods(query : String) : Flow<List<ModModel>>
    suspend fun getModByID(id : Int) : Flow<ModModel?>
    suspend fun addMod(mod : ModModel)
    suspend fun removeMod(mod: ModModel)
    suspend fun updateMod(mod: ModModel)


}