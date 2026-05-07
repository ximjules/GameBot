package com.gamebot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gamebot.app.navigation.GameBotNavigation
import com.gamebot.app.ui.theme.GameBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameBotTheme {
                GameBotNavigation()
            }
        }
    }
}