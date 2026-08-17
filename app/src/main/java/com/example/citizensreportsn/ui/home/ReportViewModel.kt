package com.example.citizensreportsn.ui.home

import androidx.lifecycle.*
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.data.repository.AppRepository
import kotlinx.coroutines.launch

class ReportViewModel(private val repository: AppRepository) : ViewModel() {

    val allReports: LiveData<List<ReportEntity>> = repository.getAllReports().asLiveData()

    private val _searchQuery = MutableLiveData<String>("")
    
    val filteredReports: LiveData<List<ReportEntity>> = MediatorLiveData<List<ReportEntity>>().apply {
        fun update() {
            val reports = allReports.value ?: emptyList()
            val query = _searchQuery.value ?: ""
            
            value = if (query.isEmpty()) {
                reports
            } else {
                reports.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.description.contains(query, ignoreCase = true) ||
                    it.address?.contains(query, ignoreCase = true) == true
                }
            }
        }
        addSource(allReports) { update() }
        addSource(_searchQuery) { update() }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Stockage temporaire du brouillon
    var draftTitle: String = ""
    var draftDescription: String = ""
    var draftCategory: String = "Routes"
    var draftPriority: String = "Moyenne"
    var draftAddress: String = ""
    var draftImageBitmap: android.graphics.Bitmap? = null
    var draftImagePath: String? = null

    fun getMyReports(userId: String): LiveData<List<ReportEntity>> {
        return repository.getMyReports(userId).asLiveData()
    }

    fun getTimeline(reportId: Long): LiveData<List<com.example.citizensreportsn.data.local.TimelineUpdateEntity>> {
        return repository.getTimeline(reportId).asLiveData()
    }

    fun refreshReports() {
        viewModelScope.launch {
            try {
                val response = repository.apiService.getReports()
                if (response.isSuccessful && response.body() != null) {
                    val reports = response.body()!!
                    
                    // NETTOIE LE CACHE LOCAL pour éviter les doublons ID_LOCAL vs ID_SERVEUR
                    repository.clearLocalReports()
                    
                    for (report in reports) {
                        repository.insertReport(report)
                    }
                    android.util.Log.d("Sync", "${reports.size} signalements synchronisés")
                }
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Échec refresh : ${e.message}")
            }
        }
    }

    fun submitReport(report: ReportEntity) {
        viewModelScope.launch {
            try {
                // ENVOIE AU SERVEUR (attendre l'ID réel avant d'écrire en local)
                val response = repository.apiService.createReport(report)
                
                if (response.isSuccessful && response.body() != null) {
                    val serverReport = response.body()!!
                    
                    clearDraft()

                    // Mettre à jour l'utilisateur
                    val updatedPoints = serverReport.authorPoints
                    val user = repository.getUserOnce(report.userId)
                    user?.let {
                        repository.saveUser(it.copy(totalPoints = updatedPoints))
                        repository.updateAllReportsPoints(it.id, updatedPoints)
                    }
                    
                    //  Insertion propre du signalement avec ID serveur
                    repository.insertReport(serverReport)
                    
                    android.util.Log.d("Voiceful_Sync", "Signalement OK. ID Serveur: ${serverReport.id}")
                }
            } catch (e: Exception) {
                // Si serveur éteint, utilisation l'ID 0 (Room générera un ID local auto-incrémenté)
                android.util.Log.e("Voiceful_Sync", "Mode dégradé", e)
                repository.insertReport(report)
            }
        }
    }

    fun clearDraft() {
        draftTitle = ""
        draftDescription = ""
        draftCategory = "Routes"
        draftPriority = "Moyenne"
        draftAddress = ""
        draftImageBitmap = null
        draftImagePath = null
    }

    fun updateReport(report: ReportEntity) {
        viewModelScope.launch {
            try {
                val response = repository.apiService.updateReport(report.id, report)
                if (response.isSuccessful) {
                    repository.updateReport(report)
                }
            } catch (e: Exception) {
                // Fallback local
                repository.updateReport(report)
            }
        }
    }

    fun deleteReport(report: ReportEntity) {
        viewModelScope.launch {
            try {
                val response = repository.apiService.deleteReport(report.id)
                if (response.isSuccessful) {
                    repository.deleteReport(report)
                }
            } catch (e: Exception) {
                // Fallback local
                repository.deleteReport(report)
            }
        }
    }

    suspend fun getUser(userId: String) = repository.getUserOnce(userId)
}

class ReportViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}