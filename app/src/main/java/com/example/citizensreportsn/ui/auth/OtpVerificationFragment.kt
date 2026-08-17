package com.example.citizensreportsn.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.citizensreportsn.R
import com.example.citizensreportsn.databinding.FragmentOtpVerificationBinding

import androidx.fragment.app.viewModels
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.utils.SessionManager

import com.example.citizensreportsn.utils.showErrorToast
import com.example.citizensreportsn.utils.showSuccessToast

class OtpVerificationFragment : Fragment(R.layout.fragment_otp_verification) {

    private lateinit var binding: FragmentOtpVerificationBinding

    private val viewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as VoicefulApplication
        AuthViewModelFactory(app.repository, SessionManager(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOtpVerificationBinding.bind(view)

        val email = arguments?.getString("email") ?: "votre email"
        val name = arguments?.getString("name") ?: "Citoyen"
        val password = arguments?.getString("password") ?: ""
        
        binding.tvOtpDesc.text = "Un code à 4 chiffres est envoyé à : $email"

        setupObservers()

        binding.btnVerifyOtp.setOnClickListener {
            val role = if (email.contains("admin")) "ADMIN" else "CITIZEN"
            viewModel.register(name, email, password, "0000", role)
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthViewModel.LoginResult.Success -> {
                    showSuccessToast("Inscription réussie !")
                    findNavController().navigate(R.id.action_otpFragment_to_authSuccessFragment)
                    viewModel.resetLoginStatus()
                }
                is AuthViewModel.LoginResult.Error -> {
                    showErrorToast(result.message)
                    viewModel.resetLoginStatus()
                }
                else -> {}
            }
        }
    }
}