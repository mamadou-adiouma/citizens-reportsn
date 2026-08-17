package com.example.citizensreportsn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("imageUri") val imageUri: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val priority: String = "Moyenne",
    val status: String = "Reçu",
    @SerializedName("departmentId") val departmentId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("user_id") val userId: String, // Pointage vers le serveur Laravel
    @SerializedName("authorName") val authorName: String = "Citoyen",
    @SerializedName("authorPoints") var authorPoints: Int = 0,
    val isSynced: Boolean = false
)