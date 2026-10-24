package com.sylergy.murajaah.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sylergy.murajaah.database.entity.FriendEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY youReviewDueToday DESC, name ASC")
    fun observeAllFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE userId = :userId")
    suspend fun getFriendById(userId: String): FriendEntity?

    @Query("SELECT * FROM friends WHERE userId = :userId")
    fun observeFriendById(userId: String): Flow<FriendEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE userId = :userId")
    suspend fun deleteFriend(userId: String)

    @Query("DELETE FROM friends")
    suspend fun clearAll()
}
