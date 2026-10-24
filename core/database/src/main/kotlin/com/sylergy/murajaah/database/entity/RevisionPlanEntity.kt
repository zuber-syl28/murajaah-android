package com.sylergy.murajaah.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revision_plans")
data class RevisionPlanEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val surahs: List<Int>,
    val isActive: Boolean = false
)
