package com.example.citizensreportsn.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citizensreportsn.R
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import androidx.fragment.app.activityViewModels
import com.example.citizensreportsn.VoicefulApplication

import androidx.core.widget.addTextChangedListener
import com.example.citizensreportsn.utils.SessionManager

class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback {

    private lateinit var binding: FragmentHomeBinding
    private var googleMap: GoogleMap? = null
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var sessionManager: SessionManager
    
    private val viewModel: ReportViewModel by activityViewModels {
        ReportViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        
        try {
            binding = FragmentHomeBinding.bind(view)

            binding.mapViewHome.onCreate(savedInstanceState)
            binding.mapViewHome.getMapAsync(this)

            setupRecyclerView()
            setupCategoryFilters()
            setupSearch()
            setupObservers()
            
            // Rafraîchir les données depuis le serveur au chargement
            viewModel.refreshReports()
            
            binding.fabAddReport.setOnClickListener {
                findNavController().navigate(R.id.addReportFragment)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(emptyList(), sessionManager.getUserId() ?: "") { report, isEditable ->
            val bundle = Bundle().apply {
                putLong("reportId", report.id)
                putBoolean("isEditable", isEditable)
                putBoolean("isPreview", false)
            }
            findNavController().navigate(R.id.reportDetailFragment, bundle)
        }

        binding.rvFeedReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
        }
    }

    private fun setupCategoryFilters() {
        binding.chipRoutes.setOnClickListener { filterByCategory("Routes") }
        binding.chipEclairage.setOnClickListener { filterByCategory("Éclairage") }
        binding.chipDechets.setOnClickListener { filterByCategory("Déchets") }
        
        // Optionnel : un clic long pour réinitialiser
        binding.etSearch.setOnLongClickListener { 
            loadAllReports()
            true
        }
    }

    private fun filterByCategory(categoryName: String) {
        viewModel.allReports.observe(viewLifecycleOwner) { reports ->
            val filtered = reports.filter { 
                it.categoryId.contains(categoryName, ignoreCase = true) || 
                it.title.contains(categoryName, ignoreCase = true)
            }
            feedAdapter.updateData(filtered)
        }
    }

    private fun loadAllReports() {
        viewModel.allReports.observe(viewLifecycleOwner) { reports ->
            feedAdapter.updateData(reports)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.setSearchQuery(text.toString())
        }
    }

    private fun setupObservers() {
        viewModel.filteredReports.observe(viewLifecycleOwner) { reports ->
            feedAdapter.updateData(reports)
            updateMarkers(reports)
        }
    }

    private fun updateMarkers(reports: List<ReportEntity>) {
        googleMap?.clear()
        for (report in reports) {
            val pos = LatLng(report.latitude, report.longitude)
            
            val iconRes = when {
                report.categoryId.contains("Routes", true) -> R.drawable.ic_marker_routes
                report.categoryId.contains("Éclairage", true) -> R.drawable.ic_marker_eclairage
                report.categoryId.contains("Déchets", true) -> R.drawable.ic_marker_dechets
                else -> R.drawable.ic_marker_securite
            }
            
            googleMap?.addMarker(MarkerOptions()
                .position(pos)
                .title(report.title)
                .icon(BitmapDescriptorFactory.fromResource(iconRes))
            )
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        viewModel.allReports.observe(viewLifecycleOwner) { reports ->
            googleMap?.clear()
            for (report in reports) {
                val pos = LatLng(report.latitude, report.longitude)
                googleMap?.addMarker(MarkerOptions().position(pos).title(report.title))
            }
            
            if (reports.isNotEmpty()) {
                val first = LatLng(reports[0].latitude, reports[0].longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 12f))
            } else {
                val dakar = LatLng(14.7167, -17.4677)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(dakar, 12f))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewHome.onResume()
    }

    override fun onPause() {
        binding.mapViewHome.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        binding.mapViewHome.onDestroy()
        super.onDestroyView()
    }
}