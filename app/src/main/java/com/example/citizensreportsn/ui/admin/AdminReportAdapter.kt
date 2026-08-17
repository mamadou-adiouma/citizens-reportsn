package com.example.citizensreportsn.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.databinding.ItemAdminIncidentBinding

class AdminReportAdapter(
    private var reports: List<ReportEntity>,
    private val onItemClicked: (ReportEntity) -> Unit
) : RecyclerView.Adapter<AdminReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(val binding: ItemAdminIncidentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemAdminIncidentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.binding.apply {
            val badge = com.example.citizensreportsn.utils.GamificationHelper.getBadgeName(report.authorPoints)
            val badgeColor = com.example.citizensreportsn.utils.GamificationHelper.getBadgeColor(report.authorPoints)
            
            tvTitle.text = report.title
            tvCategory.text = report.categoryId
            tvStatus.text = report.status
            tvReporter.text = "Reporté par: ${report.authorName}"
            tvReporterBadge.text = badge
            tvReporterBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(badgeColor)
            
            root.setOnClickListener { onItemClicked(report) }
        }
    }

    override fun getItemCount() = reports.size

    fun updateData(newReports: List<ReportEntity>) {
        reports = newReports
        notifyDataSetChanged()
    }
}