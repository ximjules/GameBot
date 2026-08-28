package com.gamebot.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.gamebot.app.navigation.GameBotNavigation
import com.gamebot.app.notifications.NotificationHelper
import com.gamebot.app.data.FirestoreProvider
import com.gamebot.app.ui.theme.GameBotTheme
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Notifikacije odobrene!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureFirestore()
        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermission()
        enableEdgeToEdge()
        setContent {
            GameBotTheme {
                GameBotNavigation()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    android.util.Log.d("MainActivity", "Dozvola već postoji!")
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun configureFirestore() {
        try {
            val firestore = FirestoreProvider.db
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
            firestore.enableNetwork()
                .addOnFailureListener { e ->
                    android.util.Log.e("MainActivity", "Firestore network could not be enabled", e)
                }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Firestore settings were already initialized: ${e.message}")
        }
    }
}
