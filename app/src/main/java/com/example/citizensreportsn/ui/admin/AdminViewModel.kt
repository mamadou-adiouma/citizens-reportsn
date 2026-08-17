package com.example.citizensreportsn.ui.admin

import androidx.lifecycle.*
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.data.repository.AppRepository
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: AppRepository) : ViewModel() {

    val allReports: LiveData<List<ReportEntity>> = repository.getAllReports().asLiveData()

    private val _searchQuery = MutableLiveData<String>("")
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _filter = MutableLiveData<String>("Tout")
    
    val filteredReports: LiveData<List<ReportEntity>> = MediatorLiveData<List<ReportEntity>>().apply {
        fun update() {
            val query = _searchQuery.value ?: ""
            val statusFilter = _filter.value ?: "Tout"
            val reports = allReports.value ?: emptyList()
            
            value = reports.filter { 
                val matchesStatus = when(statusFilter) {
                    "En cours" -> it.status == "En cours"
                    "Résolus" -> it.status == "Résolu" || it.status == "Clos"
                    else -> true
                }
                matchesStatus && (query.isEmpty() || it.title.contains(query, true) || it.authorName.contains(query, true))
            }
        }
        addSource(allReports) { update() }
        addSource(_filter) { update() }
        addSource(_searchQuery) { update() }
    }

    val stats: LiveData<AdminStats> = allReports.map { reports ->
        val total = reports.size
        AdminStats(
            total = total,
            inProgress = reports.count { it.status == "En cours" },
            resolved = reports.count { it.status == "Résolu" || it.status == "Clos" },
            unassigned = reports.count { it.status == "Reçu" },
            routesPct = if (total > 0) (reports.count { it.categoryId == "CAT_ROUTES" || it.categoryId == "Routes" } * 100 / total) else 0,
            eauxPct = if (total > 0) (reports.count { it.categoryId == "CAT_EAUX" || it.categoryId == "Eaux" } * 100 / total) else 0,
            eclairagePct = if (total > 0) (reports.count { it.categoryId == "CAT_ECLAIRAGE" || it.categoryId == "Éclairage" } * 100 / total) else 0
        )
    }

    fun setFilter(filter: String) {
        _filter.value = filter
    }

    fun updateReportStatus(report: ReportEntity, newStatus: String) {
        viewModelScope.launch {
            val updatedReport = report.copy(status = newStatus)
            try {
                // APPEL API SERVEUR OBLIGATOIRE POUR LA NOTIF
                val response = repository.apiService.updateReport(report.id, updatedReport)
                if (response.isSuccessful) {
                    repository.updateReport(updatedReport)
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminSync", "Erreur serveur update", e)
                repository.updateReport(updatedReport)
            }
        }
    }

    fun getTimeline(reportId: Long): LiveData<List<com.example.citizensreportsn.data.local.TimelineUpdateEntity>> {
        return repository.getTimeline(reportId).asLiveData()
    }

    fun addTimelineUpdate(reportId: Long, message: String) {
        viewModelScope.launch {
            repository.addTimelineUpdate(com.example.citizensreportsn.data.local.TimelineUpdateEntity(reportId = reportId, message = message))
        }
    }

    fun refreshReports() {
        viewModelScope.launch {
            try {
                val response = repository.apiService.getReports()
                if (response.isSuccessful && response.body() != null) {
                    repository.clearLocalReports()
                    for (report in response.body()!!) {
                        repository.insertReport(report)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminSync", "Échec refresh", e)
            }
        }
    }
}

data class AdminStats(
    val total: Int,
    val inProgress: Int,
    val resolved: Int,
    val unassigned: Int,
    val routesPct: Int = 0,
    val eauxPct: Int = 0,
    val eclairagePct: Int = 0
)

class AdminViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}