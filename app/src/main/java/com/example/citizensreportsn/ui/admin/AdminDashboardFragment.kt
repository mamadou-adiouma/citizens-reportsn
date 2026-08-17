package com.example.citizensreportsn.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citizensreportsn.R
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.databinding.FragmentAdminDashboardBinding
import com.example.citizensreportsn.utils.SessionManager
import com.example.citizensreportsn.utils.showSuccessToast

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.refreshReports() // Rafraîchir les données Admin
        setupStatsObservers()
        setupRecentReportsList()
        
        binding.btnLogout.setOnClickListener {
            SessionManager(requireContext()).logout()
            showSuccessToast("Déconnexion réussie")
            
            // Rediriger vers MainActivity en forçant l'accès au login
            val intent = Intent(requireContext(), com.example.citizensreportsn.MainActivity::class.java)
            intent.putExtra("FORCE_LOGIN", true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupStatsObservers() {
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.tvTotalReports.text = stats.total.toString()
            binding.tvInProgressReports.text = stats.inProgress.toString()
            binding.tvResolvedReports.text = stats.resolved.toString()
            binding.tvUnassignedReports.text = stats.unassigned.toString()
        }
    }

    private fun setupRecentReportsList() {
        val adapter = AdminReportAdapter(emptyList()) { report ->
            val bundle = Bundle().apply {
                putLong("reportId", report.id)
            }
            findNavController().navigate(R.id.action_navigation_dashboard_to_adminIncidentDetailFragment, bundle)
        }
        binding.rvRecentReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentReports.adapter = adapter
        
        viewModel.allReports.observe(viewLifecycleOwner) { reports ->
            adapter.updateData(reports.take(5))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}