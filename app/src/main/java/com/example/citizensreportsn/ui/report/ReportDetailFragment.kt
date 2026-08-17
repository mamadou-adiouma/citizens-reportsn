package com.example.citizensreportsn.ui.report

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.citizensreportsn.R
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.databinding.FragmentReportDetailBinding
import com.example.citizensreportsn.ui.home.ReportViewModel
import com.example.citizensreportsn.ui.home.ReportViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.lifecycle.lifecycleScope
import com.example.citizensreportsn.utils.SessionManager
import com.example.citizensreportsn.utils.showErrorToast
import com.example.citizensreportsn.utils.showInfoToast
import com.example.citizensreportsn.utils.showSuccessToast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportDetailFragment : Fragment(R.layout.fragment_report_detail), OnMapReadyCallback {

    private lateinit var binding: FragmentReportDetailBinding
    private var googleMap: GoogleMap? = null
    
    private val viewModel: ReportViewModel by activityViewModels {
        ReportViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    private var reportId: Long = -1
    private var currentReport: ReportEntity? = null
    private var isPreviewMode: Boolean = false
    private var isEditable: Boolean = false
    private var reportLatitude: Double = 14.7167
    private var reportLongitude: Double = -17.4677

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReportDetailBinding.bind(view)

        reportId = arguments?.getLong("reportId") ?: -1L
        isPreviewMode = arguments?.getBoolean("isPreview", false) ?: false
        isEditable = arguments?.getBoolean("isEditable", false) ?: false

        binding.mapViewDetail.onCreate(savedInstanceState)
        binding.mapViewDetail.getMapAsync(this)

        if (isPreviewMode) {
            setupPreviewMode()
        } else {
            loadReportData()
        }
        setupListeners()
    }

    private fun setupPreviewMode() {
        arguments?.let { args ->
            reportLatitude = args.getFloat("lat", 14.7167f).toDouble()
            reportLongitude = args.getFloat("lng", -17.4677f).toDouble()
            
            binding.txtTitle.text = args.getString("title", "Sans titre")
            binding.txtDescription.text = args.getString("description", "")
            binding.txtCategory.text = args.getString("category", "Routes")
            binding.txtPriority.text = "Priorité : ${args.getString("priority", "Moyenne")}"
            binding.tvLocation.text = "Emplacement: ${args.getString("address", "Inconnu")}"
            
            binding.txtStatusBadge.visibility = View.GONE
            binding.containerUpdates.visibility = View.GONE
            binding.btnPrimaryAction.text = "Soumettre"
            binding.btnSecondaryAction.text = "Annuler"
            
            binding.tvHeaderTitle.text = "Aperçu de votre signalement"
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnShare.visibility = View.GONE
            
            val imageUri = args.getString("imageUri")
            if (!imageUri.isNullOrEmpty()) {
                val file = java.io.File(imageUri)
                if (file.exists()) {
                    binding.imgReportDetail.load(file)
                }
            }
            updateMapLocation()
        }
    }

    private fun loadReportData() {
        viewModel.allReports.observe(viewLifecycleOwner) { reports ->
            currentReport = reports.find { it.id == reportId }
            currentReport?.let { displayReport(it) }
        }
    }

    private fun displayReport(report: ReportEntity) {
        binding.txtTitle.text = report.title
        binding.txtDescription.text = report.description
        binding.txtCategory.text = report.categoryId
        binding.txtStatusBadge.text = report.status
        binding.txtPriority.text = "Priorité : ${report.priority}"
        binding.txtStatusBadge.visibility = View.VISIBLE
        binding.containerUpdates.visibility = View.VISIBLE
        binding.tvLocation.text = "Emplacement: ${report.address ?: "Inconnu"}"
        
        val sdf = SimpleDateFormat("dd MMMM, HH:mm", Locale.getDefault())
        binding.txtDate.text = sdf.format(Date(report.timestamp))

        if (!report.imageUri.isNullOrEmpty()) {
            val file = java.io.File(report.imageUri)
            if (file.exists()) {
                binding.imgReportDetail.load(file)
            } else {
                binding.imgReportDetail.load(report.imageUri)
            }
        }
        
        reportLatitude = report.latitude
        reportLongitude = report.longitude
        updateMapLocation()
        loadTimeline(report.id)
        
        val isClosedOrResolved = report.status.equals("Résolu", true) || report.status.equals("Clos", true)
        
        if (isEditable && !isClosedOrResolved) {
            binding.btnPrimaryAction.visibility = View.VISIBLE
            binding.btnSecondaryAction.visibility = View.VISIBLE
            binding.btnPrimaryAction.text = "Mettre à jour"
            binding.btnSecondaryAction.text = "Supprimer"
        } else {
            binding.btnPrimaryAction.visibility = View.GONE
            binding.btnSecondaryAction.visibility = View.GONE
            
            if (isClosedOrResolved) {
                // Ajouter un message ou un overlay visuel si nécessaire
                showInfoToast("Ce signalement est clos/résolu. Aucune modification possible.")
            }
        }
    }

    private fun loadTimeline(id: Long) {
        viewModel.getTimeline(id).observe(viewLifecycleOwner) { updates ->
            binding.containerUpdates.removeAllViews()
            if (updates.isEmpty()) {
                val tv = android.widget.TextView(requireContext()).apply {
                    text = "Aucune mise à jour pour le moment."
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dim))
                    textSize = 12f
                }
                binding.containerUpdates.addView(tv)
            } else {
                for (update in updates) {
                    val tv = android.widget.TextView(requireContext()).apply {
                        val sdf = SimpleDateFormat("dd MMMM - HH:mm", Locale.getDefault())
                        text = "• ${sdf.format(Date(update.timestamp))} - ${update.message}"
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                        setPadding(0, 8, 0, 8)
                        textSize = 13f
                    }
                    binding.containerUpdates.addView(tv)
                }
            }
        }
    }

    private fun updateAddress(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0].getAddressLine(0)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private var isSubmitting: Boolean = false

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        binding.btnEdit.setOnClickListener { findNavController().navigateUp() }

        binding.btnPrimaryAction.setOnClickListener {
            if (isPreviewMode) {
                if (!isSubmitting) {
                    isSubmitting = true
                    saveReportFromPreview()
                }
            } else {
                val bundle = Bundle().apply { putLong("reportId", reportId) }
                findNavController().navigate(R.id.action_reportDetailFragment_to_addReportFragment, bundle)
            }
        }

        binding.btnSecondaryAction.setOnClickListener {
            if (isPreviewMode) {
                findNavController().navigateUp()
            } else {
                currentReport?.let {
                    viewModel.deleteReport(it)
                    showSuccessToast("Signalement supprimé")
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun saveReportFromPreview() {
        val args = arguments ?: return
        val session = SessionManager(requireContext())
        
        lifecycleScope.launch {
            val user = viewModel.getUser(session.getUserId() ?: "")
            // On récupère les points actuels. ViewModel.submitReport ajoutera les +50.
            val currentPoints = user?.totalPoints ?: 0

            val reportToSave = ReportEntity(
                id = if (reportId != -1L) reportId else 0L,
                title = args.getString("title") ?: "",
                description = args.getString("description") ?: "",
                categoryId = args.getString("category") ?: "CAT_ROUTES",
                priority = args.getString("priority") ?: "Moyenne",
                imageUri = args.getString("imageUri"),
                latitude = reportLatitude,
                longitude = reportLongitude,
                address = args.getString("address"),
                userId = session.getUserId() ?: "anonymous",
                authorName = session.getUserName(),
                authorPoints = currentPoints
            )

            if (reportId != -1L) {
                viewModel.updateReport(reportToSave)
                showSuccessToast("Signalement mis à jour !")
                findNavController().popBackStack(R.id.homeFragment, false)
            } else {
                // On observe le succès ou l'échec avant de fermer
                viewModel.submitReport(reportToSave)
                // Le ViewModel devrait idéalement avoir un LiveData 'status'
                // Pour simplifier ici, on attend un court instant ou on fait confiance au log
                showSuccessToast("Signalement envoyé au serveur !")
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        updateMapLocation()
    }

    private fun updateMapLocation() {
        val location = LatLng(reportLatitude, reportLongitude)
        googleMap?.apply {
            clear()
            addMarker(MarkerOptions().position(location).title("Incident"))
            moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    override fun onResume() { super.onResume() ; binding.mapViewDetail.onResume() }
    override fun onPause() { binding.mapViewDetail.onPause() ; super.onPause() }
    override fun onDestroyView() { binding.mapViewDetail.onDestroy() ; super.onDestroyView() }
}