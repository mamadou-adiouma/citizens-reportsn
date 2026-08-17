package com.example.citizensreportsn.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.databinding.ItemFeedReportBinding

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

class FeedAdapter(
    private var reports: List<ReportEntity>,
    private val currentUserId: String = "",
    private val onItemClicked: (ReportEntity, Boolean) -> Unit
) : RecyclerView.Adapter<FeedAdapter.ReportViewHolder>() {

    class ReportViewHolder(val binding: ItemFeedReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemFeedReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.binding.apply {
            val badge = com.example.citizensreportsn.utils.GamificationHelper.getBadgeName(report.authorPoints)
            val badgeColor = com.example.citizensreportsn.utils.GamificationHelper.getBadgeColor(report.authorPoints)
            
            txtUserName.text = report.authorName
            tvUserBadgeLabel.text = badge
            tvUserBadgeLabel.setTextColor(badgeColor)
            
            tvReportTitle.text = report.title
            txtReportText.text = report.description
            txtStatus.text = report.status
            
            // Overlay flou/sombre pour le statut "Clos"
            if (report.status.equals("Clos", ignoreCase = true)) {
                root.alpha = 0.4f
                root.setBackgroundColor("#EEEEEE".toColorInt())
            } else {
                root.alpha = 1.0f
                root.setBackgroundColor(android.graphics.Color.WHITE)
            }
            
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            txtTimeAndCategory.text = "${sdf.format(Date(report.timestamp))} • ${report.categoryId}"
            
            if (!report.imageUri.isNullOrEmpty()) {
                imgReport.load(report.imageUri)
                imgReport.visibility = View.VISIBLE
            } else {
                imgReport.visibility = View.GONE
            }
            
            val isEditable = report.userId == currentUserId
            root.setOnClickListener { onItemClicked(report, isEditable) }
        }
    }

    override fun getItemCount() = reports.size

    fun updateData(newReports: List<ReportEntity>) {
        reports = newReports
        notifyDataSetChanged()
    }
}