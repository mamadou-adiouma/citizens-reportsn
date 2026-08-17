package com.example.citizensreportsn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val nom: String,
    val email: String,
    val telephone: String,
    val role: String, // "CITIZEN" ou "ADMIN"
    val totalPoints: Int = 0
)