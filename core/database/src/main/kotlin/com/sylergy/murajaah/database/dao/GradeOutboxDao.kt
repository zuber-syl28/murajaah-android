package com.sylergy.murajaah.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sylergy.murajaah.database.entity.GradeOutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeOutboxDao {
    @Query("SELECT * FROM grade_outbox WHERE synced = 0 ORDER BY recordedAt ASC, createdAt ASC")
    fun observePendingGrades(): Flow<List<GradeOutboxEntity>>

    @Query("SELECT * FROM grade_outbox WHERE synced = 0 ORDER BY recordedAt ASC, createdAt ASC")
    suspend fun getPendingGrades(): List<GradeOutboxEntity>

    @Query("SELECT * FROM grade_outbox ORDER BY recordedAt DESC, createdAt DESC")
    fun observeAllGrades(): Flow<List<GradeOutboxEntity>>

    @Query("SELECT * FROM grade_outbox ORDER BY recordedAt DESC, createdAt DESC")
    suspend fun getAllGrades(): List<GradeOutboxEntity>

    @Query("SELECT COUNT(*) FROM grade_outbox WHERE synced = 0")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grade: GradeOutboxEntity)

    @Query("UPDATE grade_outbox SET synced = 1 WHERE clientId IN (:clientIds)")
    suspend fun markSynced(clientIds: List<String>)

    @Query("DELETE FROM grade_outbox WHERE clientId IN (:clientIds)")
    suspend fun deleteByClientIds(clientIds: List<String>)

    @Query("DELETE FROM grade_outbox WHERE synced = 1")
    suspend fun deleteSynced()

    @Query("DELETE FROM grade_outbox")
    suspend fun clearAll()
}
