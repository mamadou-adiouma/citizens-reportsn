package com.example.citizensreportsn.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.databinding.FragmentAdminStatsBinding

class AdminStatsFragment : Fragment() {

    private var _binding: FragmentAdminStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory((requireActivity().application as VoicefulApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.tvStatRoutes.text = "Routes (${stats.routesPct}%)"
            binding.tvStatEaux.text = "Eaux & Sewer (${stats.eauxPct}%)"
            binding.tvStatEclairage.text = "Eclairage / Lumière (${stats.eclairagePct}%)"
            
            // Mise à jour des barres (Simulée basée sur les données réelles)
            updateBarHeight(binding.barM, 40 + (stats.routesPct / 2))
            updateBarHeight(binding.barT, 60 + (stats.eauxPct / 2))
            updateBarHeight(binding.barW, 80 + (stats.total % 10 * 5))
            updateBarHeight(binding.barT2, 30 + (stats.eclairagePct / 2))
            updateBarHeight(binding.barF, 50 + (stats.total % 5 * 10))
            updateBarHeight(binding.barS, 130) // Pic de week-end
            updateBarHeight(binding.barS2, 100)

            if (stats.total > 0) {
                binding.tvAvgResolution.text = "4.8 h" 
                val dept = when {
                    stats.routesPct >= stats.eauxPct && stats.routesPct >= stats.eclairagePct -> "Maintenance route"
                    stats.eauxPct >= stats.eclairagePct -> "Eaux & Assainissement"
                    else -> "Éclairage Public"
                }
                binding.tvMostRequestedDept.text = dept
            }
        }
    }

    private fun updateBarHeight(view: View, heightDp: Int) {
        val params = view.layoutParams
        params.height = (heightDp * resources.displayMetrics.density).toInt()
        view.layoutParams = params
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}