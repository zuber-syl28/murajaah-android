package com.sylergy.murajaah.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sylergy.murajaah.database.entity.RevisionPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionPlanDao {
    @Query("SELECT * FROM revision_plans ORDER BY name ASC")
    fun observeAllPlans(): Flow<List<RevisionPlanEntity>>

    @Query("SELECT * FROM revision_plans ORDER BY name ASC")
    suspend fun getAllPlans(): List<RevisionPlanEntity>

    @Query("SELECT * FROM revision_plans WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlan(): RevisionPlanEntity?

    @Query("SELECT * FROM revision_plans WHERE isActive = 1 LIMIT 1")
    fun observeActivePlan(): Flow<RevisionPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<RevisionPlanEntity>)

    @Query("UPDATE revision_plans SET isActive = 0")
    suspend fun clearActivePlan()

    @Query("UPDATE revision_plans SET isActive = 1 WHERE id = :planId")
    suspend fun setActivePlan(planId: String)

    @Transaction
    suspend fun switchActivePlan(planId: String) {
        clearActivePlan()
        setActivePlan(planId)
    }

    @Query("DELETE FROM revision_plans")
    suspend fun clearAll()
}
