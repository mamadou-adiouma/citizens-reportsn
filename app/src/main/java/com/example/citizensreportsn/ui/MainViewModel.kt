package com.example.citizensreportsn.ui

import androidx.lifecycle.*
import com.example.citizensreportsn.data.local.CategoryEntity
import com.example.citizensreportsn.data.local.DepartmentEntity
import com.example.citizensreportsn.data.local.UserEntity
import com.example.citizensreportsn.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    fun initAppData() {
        viewModelScope.launch {
            // 1. Initialisation des catégories (Si vide)
            val categories = repository.getCategories().first()
            if (categories.isEmpty()) {
                repository.initCategories(listOf(
                    CategoryEntity("CAT_ROUTES", "Routes", "Nid-de-poule, chaussée dégradée"),
                    CategoryEntity("CAT_ECLAIRAGE", "Éclairage", "Lampadaire en panne"),
                    CategoryEntity("CAT_DECHETS", "Déchets", "Dépôt d'ordures sauvage")
                ))
            }
            
            // 2. Initialisation des départements (Si vide)
            val depts = repository.getDepartments().first()
            if (depts.isEmpty()) {
                repository.initDepartments(listOf(
                    DepartmentEntity("DEPT_ENTRETIEN", "Service d'entretien routier", "contact@routes.gov"),
                    DepartmentEntity("DEPT_ENV", "Environnement & Salubrité", "contact@salubrite.gov")
                ))
            }

            // 3. Création de l'admin par défaut (Si inexistant)
            val admin = repository.getUserOnce("admin_1")
            if (admin == null) {
                repository.saveUser(UserEntity(
                    id = "admin_1",
                    nom = "Administrateur Ville",
                    email = "admin@ville.sn",
                    telephone = "0000",
                    role = "ADMIN",
                    totalPoints = 1000
                ))
            }
        }
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}