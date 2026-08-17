package com.example.citizensreportsn.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.citizensreportsn.R
import com.example.citizensreportsn.databinding.FragmentAuthSuccessBinding

class AuthSuccessFragment : Fragment(R.layout.fragment_auth_success) {

    private lateinit var binding: FragmentAuthSuccessBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAuthSuccessBinding.bind(view)

        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.action_authSuccessFragment_to_homeFragment)
        }
    }
}