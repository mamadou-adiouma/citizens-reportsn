package com.example.citizensreportsn.ui.report

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citizensreportsn.R
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.databinding.FragmentMyReportsBinding
import com.example.citizensreportsn.ui.home.FeedAdapter
import com.example.citizensreportsn.ui.home.ReportViewModel
import com.example.citizensreportsn.ui.home.ReportViewModelFactory
import com.example.citizensreportsn.utils.SessionManager

class MyReportsFragment : Fragment(R.layout.fragment_my_reports) {

    private lateinit var binding: FragmentMyReportsBinding
    private lateinit var adapter: FeedAdapter
    
    private val viewModel: ReportViewModel by activityViewModels {
        ReportViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyReportsBinding.bind(view)

        val userId = SessionManager(requireContext()).getUserId() ?: ""
        
        setupRecyclerView()
        setupObservers(userId)
        
        // Rafraîchir les données personnelles depuis le serveur
        viewModel.refreshReports()
    }

    private fun setupRecyclerView() {
        val userId = SessionManager(requireContext()).getUserId() ?: ""
        adapter = FeedAdapter(emptyList(), userId) { report, isEditable ->
            val bundle = Bundle().apply {
                putLong("reportId", report.id)
                putBoolean("isEditable", isEditable)
                putBoolean("isPreview", false)
            }
            findNavController().navigate(R.id.reportDetailFragment, bundle)
        }
        binding.rvMyReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyReports.adapter = adapter
    }

    private fun setupObservers(userId: String) {
        viewModel.getMyReports(userId).observe(viewLifecycleOwner) { reports ->
            adapter.updateData(reports)
        }
    }
}