package com.example.citizensreportsn.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.citizensreportsn.R
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.databinding.FragmentAdminIncidentDetailBinding
import com.example.citizensreportsn.utils.showSuccessToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminIncidentDetailFragment : Fragment() {

    private var _binding: FragmentAdminIncidentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    private var currentReport: ReportEntity? = null
    private var selectedStatus: String = "Reçu"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminIncidentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val reportId = arguments?.getLong("reportId") ?: -1L
        
        loadReport(reportId)
        setupStatusListeners()
        setupServiceSpinner()

        binding.btnSaveAssignment.setOnClickListener {
            saveChanges()
        }

        binding.ivBack.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun loadReport(id: Long) {
        viewModel.allReports.observe(viewLifecycleOwner) { reports ->
            currentReport = reports.find { it.id == id }
            currentReport?.let { 
                selectedStatus = it.status
                displayReport(it) 
                updateStatusUI(it.status)
                loadTimeline(it.id)
                
                // Désactiver les actions si déjà Résolu ou Clos
                val isTerminal = it.status == "Résolu" || it.status == "Clos"
                if (isTerminal) {
                    disableAdminActions()
                }
            }
        }
    }

    private fun disableAdminActions() {
        binding.spService.isEnabled = false
        binding.btnSaveAssignment.isEnabled = false
        binding.btnSaveAssignment.alpha = 0.5f
        binding.btnSaveAssignment.text = "Signalement Bouclé"
        
        binding.btnStatusRecu.isEnabled = false
        binding.btnStatusEnCours.isEnabled = false
        binding.btnStatusResolu.isEnabled = false
        binding.btnStatusClos.isEnabled = false
    }

    private fun displayReport(report: ReportEntity) {
        binding.tvTitle.text = report.title
        binding.tvIncidentId.text = "ID #${report.id}"
        binding.tvCategoryLabel.text = "${report.categoryId} - Priorité: ${report.priority}"
        binding.tvStatusBadge.text = report.status
        binding.tvLocation.text = "Reporté par: ${report.authorName}\nEmplacement: ${report.address ?: "Inconnue"}"
        
        // Mémoriser et afficher le service technique
        report.departmentId?.let { dept ->
            val services = arrayOf("Entretien Routier", "Éclairage Public", "Eaux & Assainissement", "Sécurité Urbaine")
            val index = services.indexOf(dept)
            if (index >= 0) binding.spService.setSelection(index)
        }

        if (!report.imageUri.isNullOrEmpty()) {
            val file = java.io.File(report.imageUri)
            if (file.exists()) {
                binding.ivMainImage.load(file)
            } else {
                binding.ivMainImage.load(report.imageUri)
            }
        }
    }

    private fun loadTimeline(id: Long) {
        viewModel.getTimeline(id).observe(viewLifecycleOwner) { updates ->
            binding.llTimeline.removeAllViews()
            for (update in updates) {
                val tv = android.widget.TextView(requireContext()).apply {
                    val sdf = SimpleDateFormat("dd MMMM - HH:mm", Locale.getDefault())
                    text = "• ${sdf.format(Date(update.timestamp))} - ${update.message}"
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.admin_text_dim))
                    textSize = 12f
                    setPadding(0, 4, 0, 4)
                }
                binding.llTimeline.addView(tv)
            }
        }
    }

    private fun setupStatusListeners() {
        binding.btnStatusRecu.setOnClickListener { updateStatusUI("Reçu") }
        binding.btnStatusEnCours.setOnClickListener { updateStatusUI("En cours") }
        binding.btnStatusResolu.setOnClickListener { updateStatusUI("Résolu") }
        binding.btnStatusClos.setOnClickListener { updateStatusUI("Clos") }
    }

    private fun updateStatusUI(status: String) {
        selectedStatus = status
        
        val activeBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_admin_badge_in_progress)
        val yellow = ContextCompat.getColor(requireContext(), R.color.admin_yellow)
        val dim = ContextCompat.getColor(requireContext(), R.color.admin_text_dim)

        binding.btnStatusRecu.background = if (status == "Reçu") activeBg else null
        binding.btnStatusRecu.setTextColor(if (status == "Reçu") yellow else dim)

        binding.btnStatusEnCours.background = if (status == "En cours") activeBg else null
        binding.btnStatusEnCours.setTextColor(if (status == "En cours") yellow else dim)

        binding.btnStatusResolu.background = if (status == "Résolu") activeBg else null
        binding.btnStatusResolu.setTextColor(if (status == "Résolu") yellow else dim)

        binding.btnStatusClos.background = if (status == "Clos") activeBg else null
        binding.btnStatusClos.setTextColor(if (status == "Clos") yellow else dim)
    }

    private fun setupServiceSpinner() {
        val services = arrayOf("Entretien Routier", "Éclairage Public", "Eaux & Assainissement", "Sécurité Urbaine")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, services)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spService.adapter = adapter
    }

    private fun saveChanges() {
        currentReport?.let {
            val selectedService = binding.spService.selectedItem.toString()
            val updatedReport = it.copy(
                status = selectedStatus,
                departmentId = selectedService
            )
            viewModel.updateReportStatus(updatedReport, selectedStatus)
            
            // AJOUT D'UNE MISE À JOUR DANS LA TIMELINE
            viewModel.addTimelineUpdate(it.id, "Statut: $selectedStatus | Service: $selectedService")
            
            showSuccessToast("Modifications enregistrées !")
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}