package com.example.citizensreportsn.ui.profile

import androidx.lifecycle.*
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.data.local.UserEntity
import com.example.citizensreportsn.data.repository.AppRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AppRepository) : ViewModel() {

    private var _userData: LiveData<UserEntity?> = MutableLiveData()
    var userData: LiveData<UserEntity?> = _userData

    fun loadUser(userId: String) {
        // En observant le Flow du repository converti en LiveData, 
        // le profil se mettra à jour dès que les points changent en base.
        userData = repository.getUser(userId).asLiveData()
    }

    fun getMyReports(userId: String): LiveData<List<ReportEntity>> {
        return repository.getMyReports(userId).asLiveData()
    }
}

class ProfileViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}