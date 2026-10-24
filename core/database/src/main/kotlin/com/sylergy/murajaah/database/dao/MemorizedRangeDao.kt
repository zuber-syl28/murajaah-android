package com.sylergy.murajaah.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sylergy.murajaah.database.entity.MemorizedRangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemorizedRangeDao {
    @Query("SELECT * FROM memorized_ranges ORDER BY surah ASC, fromAyah ASC")
    fun observeAllRanges(): Flow<List<MemorizedRangeEntity>>

    @Query("SELECT * FROM memorized_ranges ORDER BY surah ASC, fromAyah ASC")
    suspend fun getAllRanges(): List<MemorizedRangeEntity>

    @Query("SELECT COUNT(*) FROM memorized_ranges")
    fun observeRangeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ranges: List<MemorizedRangeEntity>)

    @Query("DELETE FROM memorized_ranges WHERE id NOT IN (:keepIds)")
    suspend fun deleteRangesNotIn(keepIds: List<String>)

    @Query("DELETE FROM memorized_ranges")
    suspend fun clearAll()
}
