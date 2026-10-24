package com.sylergy.murajaah.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sylergy.murajaah.database.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY surah ASC, fromAyah ASC")
    fun observeAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY surah ASC, fromAyah ASC")
    suspend fun getAllCards(): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: String): CardEntity?

    @Query("SELECT * FROM cards WHERE dueAt <= :today ORDER BY dueAt ASC, stability ASC")
    suspend fun getDueCards(today: String): List<CardEntity>

    @Query("SELECT * FROM cards WHERE dueAt <= :today ORDER BY dueAt ASC, stability ASC")
    fun observeDueCards(today: String): Flow<List<CardEntity>>

    @Query("SELECT COUNT(*) FROM cards")
    fun observeCardCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE dueAt <= :today")
    fun observeDueCount(today: String): Flow<Int>

    @Query("SELECT * FROM cards WHERE surah = :surah ORDER BY fromAyah ASC")
    suspend fun getCardsBySurah(surah: Int): List<CardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<CardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity)

    @Update
    suspend fun update(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cards WHERE id NOT IN (:keepIds)")
    suspend fun deleteCardsNotIn(keepIds: List<String>)

    @Query("DELETE FROM cards")
    suspend fun clearAll()
}
