package com.example.citizensreportsn.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("voiceful_session", Context.MODE_PRIVATE)

    companion object {
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
        private const val USER_ROLE = "user_role"
        private const val AUTH_TOKEN = "auth_token"
        private const val IS_LOGGED_IN = "is_logged_in"
    }

    fun saveSession(userId: String, name: String, role: String, token: String? = null) {
        prefs.edit().apply {
            putString(USER_ID, userId)
            putString(USER_NAME, name)
            putString(USER_ROLE, role)
            putString(AUTH_TOKEN, token)
            putBoolean(IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString(USER_ID, null)
    fun getUserName(): String = prefs.getString(USER_NAME, "Citoyen") ?: "Citoyen"
    fun getUserRole(): String? = prefs.getString(USER_ROLE, "CITIZEN")
    fun getAuthToken(): String? = prefs.getString(AUTH_TOKEN, null)
    fun isLoggedIn(): Boolean = prefs.getBoolean(IS_LOGGED_IN, false)

    fun logout() {
        prefs.edit().clear().apply()
    }
}