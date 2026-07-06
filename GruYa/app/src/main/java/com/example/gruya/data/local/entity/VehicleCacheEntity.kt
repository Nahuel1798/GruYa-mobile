package com.example.gruya.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_cache")
data class VehicleCacheEntity(
    @PrimaryKey val id: Int,
    val type: String,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val insurance: String,
    val color: String,
    val imageUrl: String? = null
)
