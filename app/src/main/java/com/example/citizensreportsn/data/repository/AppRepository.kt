package com.example.citizensreportsn.data.repository

import com.example.citizensreportsn.data.local.*
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val db: AppDatabase,
    val apiService: com.example.citizensreportsn.data.remote.ApiService // Connexion aux services backend
) {

    // Reports
    fun getAllReports(): Flow<List<ReportEntity>> = db.reportDao().getAllReports()
    fun getMyReports(userId: String): Flow<List<ReportEntity>> = db.reportDao().getReportsByUser(userId)
    suspend fun insertReport(report: ReportEntity) = db.reportDao().insertReport(report)
    suspend fun updateReport(report: ReportEntity) = db.reportDao().updateReport(report)
    suspend fun deleteReport(report: ReportEntity) = db.reportDao().deleteReport(report)
    suspend fun getReportById(id: Long) = db.reportDao().getReportById(id)
    suspend fun updateAllReportsPoints(userId: String, points: Int) = db.reportDao().updateReportsPoints(userId, points)
    suspend fun clearLocalReports() = db.reportDao().deleteAllReports()

    // Auth
    fun getUser(id: String): Flow<UserEntity?> = db.userDao().getUserById(id)
    suspend fun getUserOnce(id: String) = db.userDao().getUserByIdOnce(id)
    suspend fun getAllUsers() = db.userDao().getAllUsers()
    suspend fun saveUser(user: UserEntity) = db.userDao().insertUser(user)

    // Categories
    fun getCategories(): Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
    suspend fun initCategories(categories: List<CategoryEntity>) = db.categoryDao().insertCategories(categories)

    // Departments
    fun getDepartments(): Flow<List<DepartmentEntity>> = db.departmentDao().getAllDepartments()
    suspend fun initDepartments(departments: List<DepartmentEntity>) = db.departmentDao().insertDepartments(departments)

    // Timeline
    fun getTimeline(reportId: Long): Flow<List<TimelineUpdateEntity>> = db.timelineDao().getUpdatesForReport(reportId)
    suspend fun addTimelineUpdate(update: TimelineUpdateEntity) = db.timelineDao().insertUpdate(update)
}