package com.example.citizensreportsn.ui.auth

import androidx.lifecycle.*
import com.example.citizensreportsn.data.local.UserEntity
import com.example.citizensreportsn.data.repository.AppRepository
import com.example.citizensreportsn.utils.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginStatus = MutableLiveData<LoginResult?>()
    val loginStatus: LiveData<LoginResult?> = _loginStatus

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.apiService.login(com.example.citizensreportsn.data.remote.LoginRequest(email, password))
                
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    
                    // Sauvegarder l'utilisateur (écrase l'ancien avec les points à jour)
                    repository.saveUser(authData.user)
                    sessionManager.saveSession(authData.user.id, authData.user.nom, authData.user.role, authData.token)
                    
                    // SYNCHRONISATION : Télécharger les signalements du serveur
                    syncUserReportsFromServer()
                    
                    // Envoyer le Token FCM
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(500)
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) syncFcmToken(task.result)
                        }
                    }

                    _loginStatus.value = LoginResult.Success(authData.user.role)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Identifiants incorrects"
                    _loginStatus.value = LoginResult.Error("Échec : $errorMsg")
                }
            } catch (e: Exception) {
                _loginStatus.value = LoginResult.Error("Connexion au serveur impossible : ${e.message}")
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String, role: String = "CITIZEN") {
        viewModelScope.launch {
            try {
                val request = com.example.citizensreportsn.data.remote.RegisterRequest(name, email, password, phone, role)
                val response = repository.apiService.register(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    
                    // Sauvegarder l'utilisateur (écrase l'ancien avec les points à jour)
                    repository.saveUser(authData.user)
                    sessionManager.saveSession(authData.user.id, authData.user.nom, authData.user.role, authData.token)
                    
                    // SYNCHRONISATION : Télécharger les signalements du serveur
                    syncUserReportsFromServer()
                    
                    // Envoyer le Token FCM
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(500)
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) syncFcmToken(task.result)
                        }
                    }

                    _loginStatus.value = LoginResult.Success(authData.user.role)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Données invalides"
                    _loginStatus.value = LoginResult.Error("Échec : $errorMsg")
                }
            } catch (e: Exception) {
                _loginStatus.value = LoginResult.Error("Serveur injoignable : ${e.message}")
            }
        }
    }

    fun resetLoginStatus() {
        _loginStatus.value = null
    }

    private fun syncFcmToken(token: String) {
        viewModelScope.launch {
            try {
                repository.apiService.updateFcmToken(mapOf("fcm_token" to token))
            } catch (e: Exception) {
                android.util.Log.e("FCM", "Erreur synchro token", e)
            }
        }
    }

    private fun syncUserReportsFromServer() {
        viewModelScope.launch {
            try {
                val response = repository.apiService.getReports()
                if (response.isSuccessful && response.body() != null) {
                    val reports = response.body()!!
                    for (report in reports) {
                        repository.insertReport(report)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Erreur synchro reports", e)
            }
        }
    }

    sealed class LoginResult {
        data class Success(val role: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}

class AuthViewModelFactory(
    private val repository: AppRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}