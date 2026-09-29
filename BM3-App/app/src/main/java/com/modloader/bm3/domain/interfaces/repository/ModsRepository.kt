package com.modloader.bm3.domain.interfaces.repository

import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.utils.PAGINATION_LIMIT
import kotlinx.coroutines.flow.Flow

interface ModsRepository {
    suspend fun getMods(limit: Int = PAGINATION_LIMIT, page: Int = 0): Flow<List<ModModel>>
    suspend fun queryMods(query: String): Flow<List<ModModel>>
    suspend fun getModByID(id: Int): Flow<ModModel?>
    suspend fun addMod(mod: ModModel)
    suspend fun removeMod(mod: ModModel)
    suspend fun updateMod(mod: ModModel)


}