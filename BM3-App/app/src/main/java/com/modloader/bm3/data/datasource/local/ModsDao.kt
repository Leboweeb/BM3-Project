package com.modloader.bm3.data.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.modloader.bm3.data.model.room.ModEntity
import com.modloader.bm3.utils.PAGINATION_LIMIT
import kotlinx.coroutines.flow.Flow

@Dao
interface ModsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMod(mod: ModEntity)

    @Delete
    suspend fun removeMod(mod: ModEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMod(mod: ModEntity)

    // I'm using a Flow here, no need to mark as suspend fun because room updates this flow automatically
    // NOTE : ranges is from start-offset, meaning LIMIT 20 OFFSET 20 retrieves records 21-40
    @Query("SELECT *  FROM Mods LIMIT :limit OFFSET :offset")
    fun getAllModsPaginated(limit: Int = PAGINATION_LIMIT, offset: Int): Flow<List<ModEntity>>

    @Query("SELECT * FROM Mods WHERE title LIKE :query||'%' ")
    fun queryMod(query: String): Flow<List<ModEntity>>

    @Query("SELECT * FROM Mods WHERE id =:id")
    fun getModByID(id: Int): Flow<ModEntity?>

}