package com.example.citizensreportsn.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.citizensreportsn.R
import com.example.citizensreportsn.databinding.FragmentLoginBinding

import androidx.fragment.app.viewModels
import android.content.Intent
import android.util.Log
import com.example.citizensreportsn.VoicefulApplication
import com.example.citizensreportsn.ui.admin.AdminMainActivity
import com.example.citizensreportsn.utils.SessionManager
import com.example.citizensreportsn.utils.showErrorToast
import com.example.citizensreportsn.utils.showInfoToast

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as VoicefulApplication
        AuthViewModelFactory(app.repository, SessionManager(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        setupObservers()

        binding.tabRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                showErrorToast("Veuillez saisir votre email")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                showErrorToast("Veuillez saisir votre mot de passe")
                return@setOnClickListener
            }

            // Détection du rôle basée sur l'email pour la démo
            viewModel.login(email, password)
        }
    }

    private fun setupObservers() {
        viewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            Log.d("Voiceful_Redir", "Result: $result")
            
            when (result) {
                is AuthViewModel.LoginResult.Success -> {
                    showInfoToast("Redirection vers Accueil...")
                    viewModel.resetLoginStatus() 
                    
                    if (result.role == "ADMIN") {
                        val intent = Intent(requireContext(), AdminMainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                }
                is AuthViewModel.LoginResult.Error -> {
                    showErrorToast(result.message)
                }
            }
        }
    }
}