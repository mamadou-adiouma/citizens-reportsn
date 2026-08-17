package com.example.citizensreportsn.data.remote

import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.data.local.UserEntity
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body credentials: RegisterRequest): Response<AuthResponse>

    @GET("reports")
    suspend fun getReports(): Response<List<ReportEntity>>

    @POST("reports")
    suspend fun createReport(@Body report: ReportEntity): Response<ReportEntity>

    @PUT("reports/{id}")
    suspend fun updateReport(@Path("id") id: Long, @Body report: ReportEntity): Response<ReportEntity>

    @DELETE("reports/{id}")
    suspend fun deleteReport(@Path("id") id: Long): Response<Unit>

    @POST("user/fcm-token")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<Unit>
}

data class LoginRequest(val email: String, val password: String) // Laravel (password)
data class RegisterRequest(val nom: String, val email: String, val password: String, val telephone: String, val role: String)
data class AuthResponse(val token: String, val user: UserEntity)