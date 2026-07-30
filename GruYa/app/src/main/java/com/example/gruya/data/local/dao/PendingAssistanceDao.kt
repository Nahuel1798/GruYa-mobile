package com.example.gruya.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gruya.data.local.entity.PendingAssistanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingAssistanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PendingAssistanceEntity): Long

    @Update
    suspend fun updateStatus(entity: PendingAssistanceEntity)

    @Query("SELECT * FROM pending_assistances WHERE status = 'PENDING' AND userId = :userId ORDER BY capturedAt ASC")
    suspend fun readPending(userId: Int): List<PendingAssistanceEntity>

    @Query("SELECT * FROM pending_assistances WHERE status IN ('PENDING', 'NEEDS_REAUTH') AND userId = :userId ORDER BY capturedAt ASC")
    suspend fun readNeedsSync(userId: Int): List<PendingAssistanceEntity>

    @Query("SELECT * FROM pending_assistances WHERE id = :id")
    suspend fun getById(id: Long): PendingAssistanceEntity?

    @Query("DELETE FROM pending_assistances WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM pending_assistances WHERE status = 'PENDING' AND userId = :userId")
    fun observeUserPendingCount(userId: Int): Flow<Int>

    @Query("SELECT * FROM pending_assistances WHERE userId = :userId ORDER BY capturedAt DESC")
    fun observeUserAll(userId: Int): Flow<List<PendingAssistanceEntity>>
}
