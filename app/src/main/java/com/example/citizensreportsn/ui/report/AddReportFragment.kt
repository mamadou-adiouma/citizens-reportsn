package com.example.citizensreportsn.ui.report

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.citizensreportsn.R
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.data.local.ReportEntity
import com.example.citizensreportsn.data.ml.ImageClassifierHelper
import com.example.citizensreportsn.databinding.FragmentAddReportBinding
import com.example.citizensreportsn.ui.home.ReportViewModel
import com.example.citizensreportsn.ui.home.ReportViewModelFactory
import com.example.citizensreportsn.utils.SessionManager
import com.example.citizensreportsn.utils.showErrorToast
import com.example.citizensreportsn.utils.showInfoToast
import com.example.citizensreportsn.utils.showSuccessToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class AddReportFragment : Fragment(R.layout.fragment_add_report) {

    private lateinit var binding: FragmentAddReportBinding
    private lateinit var classifierHelper: ImageClassifierHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var selectedBitmap: Bitmap? = null
    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0
    private var currentAddress: String = "Localisation en cours..."

    private val viewModel: ReportViewModel by activityViewModels {
        ReportViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    private var reportId: Long = -1L
    private var existingReport: ReportEntity? = null

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                selectedBitmap = it
                updatePhotoPreview(it)
                analyzeImage(it)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) getCurrentLocation()
    }

    // Lanceur pour la galerie
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(requireContext().contentResolver, it)) { decoder, _, _ ->
                        decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
                }
                
                // Redimensionnement pour éviter les crashs mémoire
                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 640, 640, true)
                
                selectedBitmap = scaledBitmap
                updatePhotoPreview(scaledBitmap)
                analyzeImage(scaledBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast("Erreur lors du chargement de l'image")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddReportBinding.bind(view)
        classifierHelper = ImageClassifierHelper(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        reportId = arguments?.getLong("reportId") ?: -1L
        if (reportId != -1L) {
            loadExistingReport()
            binding.tvTitle.text = "Modifier le signalement"
            binding.btnNext.text = "Enregistrer les modifications"
        } else {
            // Charger le brouillon si on revient de l'aperçu
            loadDraft()
        }

        checkLocationPermission()

        binding.btnSelectPhoto.setOnClickListener {
            showImageSourceDialog()
        }

        binding.layoutCategory.setOnClickListener { showCategoryPicker() }
        
        binding.layoutPriority.setOnClickListener { showPriorityPicker() }
        
        binding.btnRefreshLocation.setOnClickListener { checkLocationPermission() }

        binding.btnNext.setOnClickListener { submitReport() }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Prendre une photo", "Choisir dans la galerie")
        AlertDialog.Builder(requireContext())
            .setTitle("Ajouter une photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        takePhotoLauncher.launch(intent)
                    }
                    1 -> {
                        pickImageLauncher.launch("image/*")
                    }
                }
            }.show()
    }

    private fun loadExistingReport() {
        viewModel.allReports.observe(viewLifecycleOwner) { reports ->
            existingReport = reports.find { it.id == reportId }
            existingReport?.let { report ->
                binding.etTitle.setText(report.title)
                binding.etDescription.setText(report.description)
                binding.tvCategoryLabel.text = report.categoryId
                binding.etLocationManual.setText(report.address)
                currentLat = report.latitude
                currentLng = report.longitude
                currentAddress = report.address ?: ""
                
                if (!report.imageUri.isNullOrEmpty()) {
                    val file = java.io.File(report.imageUri)
                    if (file.exists()) {
                        updatePhotoPreview(android.graphics.BitmapFactory.decodeFile(report.imageUri))
                    }
                }
            }
        }
    }

    private fun loadDraft() {
        if (viewModel.draftTitle.isNotEmpty()) {
            binding.etTitle.setText(viewModel.draftTitle)
            binding.etDescription.setText(viewModel.draftDescription)
            binding.tvCategoryLabel.text = viewModel.draftCategory
            binding.tvPriorityLabel.text = viewModel.draftPriority
            binding.etLocationManual.setText(viewModel.draftAddress)
            currentAddress = viewModel.draftAddress
            
            viewModel.draftImageBitmap?.let {
                selectedBitmap = it
                updatePhotoPreview(it)
            }
        }
    }

    private fun saveToDraft() {
        viewModel.draftTitle = binding.etTitle.text.toString()
        viewModel.draftDescription = binding.etDescription.text.toString()
        viewModel.draftCategory = binding.tvCategoryLabel.text.toString()
        viewModel.draftPriority = binding.tvPriorityLabel.text.toString()
        viewModel.draftAddress = binding.etLocationManual.text.toString()
        viewModel.draftImageBitmap = selectedBitmap
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLat = it.latitude
                    currentLng = it.longitude
                    
                    try {
                        val geocoder = android.location.Geocoder(requireContext(), Locale.getDefault())
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            currentAddress = addresses[0].getAddressLine(0)
                        }
                    } catch (e: Exception) { 
                        currentAddress = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", it.latitude, it.longitude)
                    }

                    binding.etLocationManual.setText(currentAddress)
                }
            }
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    private fun updatePhotoPreview(bitmap: Bitmap) {
        binding.btnSelectPhoto.removeAllViews()
        val iv = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageBitmap(bitmap)
        }
        binding.btnSelectPhoto.addView(iv)
    }

    private fun analyzeImage(bitmap: Bitmap) {
        val result = classifierHelper.classify(bitmap)
        binding.tvCategoryLabel.text = result
        binding.tvCategoryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_purple))
        
        showInfoToast("IA : Catégorie détectée ($result)")
    }

    private fun showCategoryPicker() {
        val categories = arrayOf("Routes", "Éclairage", "Déchets", "Transports", "Sécurité")
        AlertDialog.Builder(requireContext())
            .setTitle("Catégorie")
            .setItems(categories) { _, which ->
                binding.tvCategoryLabel.text = categories[which]
                binding.tvCategoryLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }.show()
    }

    private fun showPriorityPicker() {
        val priorities = arrayOf("Basse", "Moyenne", "Haute", "Urgente")
        AlertDialog.Builder(requireContext())
            .setTitle("Priorité")
            .setItems(priorities) { _, which ->
                binding.tvPriorityLabel.text = priorities[which]
                binding.tvPriorityLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }.show()
    }

    private fun submitReport() {
        val title = binding.etTitle.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val manualAddress = binding.etLocationManual.text.toString().trim()
        val category = binding.tvCategoryLabel.text.toString()
        val priority = binding.tvPriorityLabel.text.toString()

        if (selectedBitmap == null && existingReport == null) {
            showErrorToast("Veuillez ajouter une photo")
            return
        }
        if (title.isEmpty()) {
            showErrorToast("Le titre est obligatoire")
            return
        }
        if (category == "Catérory ou laissez l'agent IA détecter" || category.isEmpty()) {
            showErrorToast("Veuillez choisir une catégorie")
            return
        }
        if (desc.isEmpty()) {
            showErrorToast("La description est obligatoire")
            return
        }
        if (priority == "Niveau de priorité" || priority.isEmpty()) {
            showErrorToast("Veuillez choisir une priorité")
            return
        }
        if (manualAddress.isEmpty()) {
            showErrorToast("La localisation est obligatoire")
            return
        }

        // Sauvegarde dans le ViewModel pour le retour
        saveToDraft()

        // Sauvegarde temporaire de l'image pour l'aperçu
        var imagePath: String? = existingReport?.imageUri
        selectedBitmap?.let { bitmap ->
            val file = java.io.File(requireContext().cacheDir, "temp_report_${System.currentTimeMillis()}.jpg")
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            imagePath = file.absolutePath
        }

        // Au lieu de sauvegarder, on passe à l'écran d'aperçu
        val bundle = Bundle().apply {
            putLong("reportId", reportId) // -1 si nouveau
            putString("title", title)
            putString("description", desc)
            putString("category", binding.tvCategoryLabel.text.toString())
            putString("priority", binding.tvPriorityLabel.text.toString())
            putString("imageUri", imagePath)
            putFloat("lat", currentLat.toFloat())
            putFloat("lng", currentLng.toFloat())
            putString("address", if (manualAddress.isNotEmpty()) manualAddress else currentAddress)
            putBoolean("isPreview", true)
        }
        findNavController().navigate(R.id.reportDetailFragment, bundle)
    }
}