package com.example.citizensreportsn.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.citizensreportsn.R
import com.example.citizensreportsn.databinding.FragmentRegisterBinding
import com.example.citizensreportsn.utils.showErrorToast

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var binding: FragmentRegisterBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        binding.tabLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRegisterSubmit.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etRegEmail.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()

            if (name.isEmpty()) {
                showErrorToast("Le nom complet est obligatoire")
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                showErrorToast("L'email est obligatoire")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                showErrorToast("Le mot de passe est obligatoire")
                return@setOnClickListener
            }
            if (password.length < 4) {
                showErrorToast("Le mot de passe doit faire au moins 4 caractères")
                return@setOnClickListener
            }

            // Vérification OTP avec l'email réel, le nom et le mot de passe
            val bundle = Bundle().apply {
                putString("email", email)
                putString("name", name)
                putString("password", password)
            }
            findNavController().navigate(R.id.action_registerFragment_to_otpFragment, bundle)
        }
    }
}