package com.example.citizensreportsn.utils

import com.example.citizensreportsn.R

object GamificationHelper {

    fun getBadgeName(points: Int): String {
        return when {
            points >= 500 -> "Citoyen d'Or"
            points >= 200 -> "Citoyen Actif"
            points >= 50 -> "Citoyen Engagé"
            else -> "Nouveau Citoyen"
        }
    }

    fun getBadgeColor(points: Int): Int {
        return when {
            points >= 500 -> android.graphics.Color.parseColor("#FFD700") // Gold
            points >= 200 -> android.graphics.Color.parseColor("#C0C0C0") // Silver
            points >= 50 -> android.graphics.Color.parseColor("#CD7F32")  // Bronze
            else -> android.graphics.Color.parseColor("#808080")          // Gray
        }
    }
}