package com.example.gruya.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_assistances")
data class PendingAssistanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceType: String,
    val issueType: String,
    val vehicleId: Int,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
    val capturedAt: Long,
    val status: String,
    val retryCount: Int = 0,
    val lastError: String? = null
)
