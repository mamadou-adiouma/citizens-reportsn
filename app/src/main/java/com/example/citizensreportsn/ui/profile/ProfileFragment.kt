package com.example.citizensreportsn.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citizensreportsn.R
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.databinding.FragmentProfileBinding
import com.example.citizensreportsn.ui.home.FeedAdapter
import com.example.citizensreportsn.utils.SessionManager
import com.example.citizensreportsn.utils.showSuccessToast

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var myReportsAdapter: FeedAdapter
    
    private val viewModel: ProfileViewModel by activityViewModels {
        ProfileViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        val session = SessionManager(requireContext())
        val userId = session.getUserId() ?: ""
        
        viewModel.loadUser(userId)
        setupObservers()
        setupListeners()
        setupMyReportsRecyclerView(userId)
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUserName.text = it.nom
                binding.tvUserHandle.text = "@${it.nom.lowercase().replace(" ", "")}"
                binding.tvUserPoints.text = "${it.totalPoints} Points"
                
                val badgeName = com.example.citizensreportsn.utils.GamificationHelper.getBadgeName(it.totalPoints)
                val badgeColor = com.example.citizensreportsn.utils.GamificationHelper.getBadgeColor(it.totalPoints)
                
                binding.tvUserBadge.text = badgeName
                binding.tvUserBadge.setTextColor(badgeColor)
            }
        }
    }

    private fun setupListeners() {
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
        
        binding.btnLogout.setOnClickListener {
            val session = SessionManager(requireContext())
            session.logout()
            showSuccessToast("Déconnexion réussie")
            
            // Rediriger vers l'écran de connexion (Login)
            findNavController().navigate(R.id.loginFragment, null, 
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }

    private fun setupMyReportsRecyclerView(userId: String) {
        myReportsAdapter = FeedAdapter(emptyList(), userId) { report, isEditable ->
            val bundle = Bundle().apply {
                putLong("reportId", report.id)
                putBoolean("isEditable", isEditable)
                putBoolean("isPreview", false)
            }
            findNavController().navigate(R.id.reportDetailFragment, bundle)
        }

        binding.rvMyReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myReportsAdapter
        }
        
        viewModel.getMyReports(userId).observe(viewLifecycleOwner) { reports ->
            myReportsAdapter.updateData(reports)
            binding.profileTabs.getTabAt(0)?.text = "Posts (${reports.size})"
        }
    }
}