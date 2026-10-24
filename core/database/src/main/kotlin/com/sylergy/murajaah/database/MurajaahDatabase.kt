package com.sylergy.murajaah.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sylergy.murajaah.database.converter.Converters
import com.sylergy.murajaah.database.dao.AchievementDao
import com.sylergy.murajaah.database.dao.CardDao
import com.sylergy.murajaah.database.dao.FriendDao
import com.sylergy.murajaah.database.dao.GradeOutboxDao
import com.sylergy.murajaah.database.dao.MemorizedRangeDao
import com.sylergy.murajaah.database.dao.RevisionPlanDao
import com.sylergy.murajaah.database.entity.AchievementEntity
import com.sylergy.murajaah.database.entity.CardEntity
import com.sylergy.murajaah.database.entity.FriendEntity
import com.sylergy.murajaah.database.entity.GradeOutboxEntity
import com.sylergy.murajaah.database.entity.MemorizedRangeEntity
import com.sylergy.murajaah.database.entity.RevisionPlanEntity

@Database(
    entities = [
        CardEntity::class,
        MemorizedRangeEntity::class,
        RevisionPlanEntity::class,
        GradeOutboxEntity::class,
        AchievementEntity::class,
        FriendEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MurajaahDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun memorizedRangeDao(): MemorizedRangeDao
    abstract fun revisionPlanDao(): RevisionPlanDao
    abstract fun gradeOutboxDao(): GradeOutboxDao
    abstract fun achievementDao(): AchievementDao
    abstract fun friendDao(): FriendDao

    companion object {
        private const val DATABASE_NAME = "murajaah.db"

        @Volatile
        private var INSTANCE: MurajaahDatabase? = null

        fun getInstance(context: Context): MurajaahDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MurajaahDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        fun createInMemory(context: Context): MurajaahDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                MurajaahDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
