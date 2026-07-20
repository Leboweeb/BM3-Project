package com.modloader.bm3.data.repository

import com.modloader.bm3.data.datasource.local.ModsDao
import com.modloader.bm3.data.mapping.toDataLayer
import com.modloader.bm3.domain.model.ModModel
import com.modloader.bm3.domain.repository.ModsRepository
import com.modloader.bm3.data.mapping.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ModsRepositoryImpl @Inject constructor(
    private val modsDao: ModsDao
) : ModsRepository{

    override suspend fun getMods(
        limit: Int,
        page: Int
    ): Flow<List<ModModel>> {
        /**
        * <p> Keep in mind {@code page} <b>NEEDS</b> to be zero indexed!! </p>
        */
        return modsDao.getAllModsPaginated(limit,page * limit).toDomain()
    }

    override suspend fun queryMods(query: String) : Flow<List<ModModel>>{
        return modsDao.queryMod(query).toDomain()
    }

    override suspend fun getModByID(id: Int) : Flow<ModModel?> {
        return modsDao.getModByID(id).map {
            mod ->
            mod?.toDomain()
        }
    }

    override suspend fun addMod(mod: ModModel) {
        modsDao.addMod(mod.toDataLayer())
    }

    override suspend fun removeMod(mod: ModModel) {
        modsDao.removeMod(mod.toDataLayer())
    }

    override suspend fun updateMod(mod: ModModel) {
        modsDao.updateMod(mod.toDataLayer())
    }
}