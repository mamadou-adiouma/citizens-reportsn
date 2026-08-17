package com.example.citizensreportsn

import android.app.Application
import com.example.citizensreportsn.data.local.AppDatabase
import com.example.citizensreportsn.data.repository.AppRepository

class VoicefulApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val apiService by lazy { com.example.citizensreportsn.data.remote.RetrofitClient.getApiService(this) }
    val repository by lazy { AppRepository(database, apiService) }
}