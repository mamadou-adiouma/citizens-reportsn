package com.example.citizensreportsn.utils

import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.citizensreportsn.R

fun Fragment.showCustomToast(message: String, emoji: String) {
    try {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(R.layout.layout_custom_toast, null)
        
        val text: TextView = layout.findViewById(R.id.tvToastMessage)
        text.text = "$emoji $message"

        with(Toast(requireContext())) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    } catch (e: Exception) {
        // Fallback si le layout custom échoue
        Toast.makeText(requireContext(), "$emoji $message", Toast.LENGTH_SHORT).show()
    }
}

fun Fragment.showSuccessToast(message: String) = showCustomToast(message, "✅")
fun Fragment.showErrorToast(message: String) = showCustomToast(message, "❌")
fun Fragment.showInfoToast(message: String) = showCustomToast(message, "ℹ️")