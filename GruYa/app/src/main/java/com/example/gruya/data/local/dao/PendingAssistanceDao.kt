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

    @Query("SELECT * FROM pending_assistances WHERE status = 'PENDING' ORDER BY capturedAt ASC")
    suspend fun readPending(): List<PendingAssistanceEntity>

    @Query("SELECT * FROM pending_assistances WHERE id = :id")
    suspend fun getById(id: Long): PendingAssistanceEntity?

    @Query("DELETE FROM pending_assistances WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM pending_assistances")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM pending_assistances ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<PendingAssistanceEntity>>
}
