package com.example.gruya.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gruya.data.local.entity.VehicleCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(vehicles: List<VehicleCacheEntity>)

    @Query("SELECT * FROM vehicle_cache WHERE userId = :userId")
    fun getAllByUser(userId: Int): Flow<List<VehicleCacheEntity>>

    @Query("DELETE FROM vehicle_cache")
    suspend fun deleteAll()

    @Query("DELETE FROM vehicle_cache WHERE userId = :userId")
    suspend fun deleteByUser(userId: Int)

    @Query("SELECT * FROM vehicle_cache WHERE id = :id")
    suspend fun getById(id: Int): VehicleCacheEntity?

    @Query("SELECT COUNT(*) FROM vehicle_cache WHERE userId = :userId")
    suspend fun countByUser(userId: Int): Int
}
