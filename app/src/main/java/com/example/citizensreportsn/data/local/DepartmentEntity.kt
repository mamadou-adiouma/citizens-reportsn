package com.example.citizensreportsn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val id: String,
    val nomService: String,
    val emailContact: String
)