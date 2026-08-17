package com.example.citizensreportsn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeline_updates")
data class TimelineUpdateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: Long,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)