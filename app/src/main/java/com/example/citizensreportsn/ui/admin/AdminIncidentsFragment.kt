package com.example.citizensreportsn.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citizensreportsn.R
import androidx.core.widget.addTextChangedListener
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.databinding.FragmentAdminIncidentsBinding

class AdminIncidentsFragment : Fragment() {

    private var _binding: FragmentAdminIncidentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    private lateinit var adapter: AdminReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminIncidentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.refreshReports() // Rafraîchir les données Admin
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupObservers()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.setSearchQuery(text.toString())
        }
    }

    private fun setupFilters() {
        binding.filterAll.setOnClickListener { updateFilter("Tout") }
        binding.filterInProgress.setOnClickListener { updateFilter("En cours") }
        binding.filterResolved.setOnClickListener { updateFilter("Résolus") }
    }

    private fun updateFilter(filter: String) {
        viewModel.setFilter(filter)
        
        // Mise à jour visuelle des backgrounds (Neumorphic style)
        val activeBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_admin_filter_active)
        
        binding.filterAll.background = if (filter == "Tout") activeBg else null
        binding.filterInProgress.background = if (filter == "En cours") activeBg else null
        binding.filterResolved.background = if (filter == "Résolus") activeBg else null

        val white = ContextCompat.getColor(requireContext(), R.color.admin_text_white)
        val dim = ContextCompat.getColor(requireContext(), R.color.admin_text_dim)

        binding.filterAll.setTextColor(if (filter == "Tout") white else dim)
        binding.filterInProgress.setTextColor(if (filter == "En cours") white else dim)
        binding.filterResolved.setTextColor(if (filter == "Résolus") white else dim)
    }

    private fun setupRecyclerView() {
        adapter = AdminReportAdapter(emptyList()) { report ->
            val bundle = Bundle().apply {
                putLong("reportId", report.id)
            }
            findNavController().navigate(R.id.action_navigation_incidents_to_adminIncidentDetailFragment, bundle)
        }
        
        binding.rvIncidents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvIncidents.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.filteredReports.observe(viewLifecycleOwner) { reports ->
            adapter.updateData(reports)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}