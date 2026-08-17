package com.example.citizensreportsn

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.citizensreportsn.ui.MainViewModel
import com.example.citizensreportsn.ui.MainViewModelFactory
import com.example.citizensreportsn.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as VoicefulApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mainViewModel.initAppData()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (intent.getBooleanExtra("FORCE_LOGIN", false)) {
            navController.navigate(R.id.loginFragment)
        }
        
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.addReportFragment, R.id.profileFragment, R.id.myReportsFragment -> {
                    bottomNav.visibility = View.VISIBLE
                }
                else -> bottomNav.visibility = View.GONE
            }
        }

        checkNotificationPermission()
        
        if (SessionManager(this).isLoggedIn()) {
            getFCMToken()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result
            // Log.d("FCM_TOKEN", "Mon Token FCM : $token")
            
            val session = SessionManager(this)
            val api = (application as VoicefulApplication).apiService
            
            if (session.isLoggedIn()) {
                lifecycleScope.launch {
                    try {
                        val response = api.updateFcmToken(mapOf("fcm_token" to token))
                        if (response.isSuccessful) {
                            Log.d("FCM_SYNC", "Token synchronisé !")
                        } else {
                            Log.e("FCM_SYNC", "Erreur : ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("FCM_SYNC", "Erreur réseau : ${e.message}")
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}