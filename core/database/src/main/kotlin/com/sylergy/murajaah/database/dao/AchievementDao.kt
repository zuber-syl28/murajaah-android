package com.sylergy.murajaah.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sylergy.murajaah.database.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY earnedAt ASC")
    fun observeAllEarned(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY earnedAt ASC")
    suspend fun getAllEarned(): List<AchievementEntity>

    @Query("SELECT key FROM achievements")
    suspend fun getAllEarnedKeys(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("DELETE FROM achievements")
    suspend fun clearAll()
}
