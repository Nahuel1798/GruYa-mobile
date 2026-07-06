package com.example.gruya.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gruya.data.local.dao.PendingAssistanceDao
import com.example.gruya.data.local.dao.VehicleCacheDao
import com.example.gruya.data.local.entity.Converters
import com.example.gruya.data.local.entity.PendingAssistanceEntity
import com.example.gruya.data.local.entity.VehicleCacheEntity

@Database(
    entities = [PendingAssistanceEntity::class, VehicleCacheEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pendingAssistanceDao(): PendingAssistanceDao
    abstract fun vehicleCacheDao(): VehicleCacheDao
}
