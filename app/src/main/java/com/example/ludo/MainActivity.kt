package com.example.ludo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ludo.theme.MyApplicationTheme
import com.example.ludo.ui.screens.GameScreen
import com.example.ludo.ui.screens.MainMenuScreen
import com.example.ludo.viewmodel.LudoViewModel

enum class ScreenState {
    MENU,
    GAME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: LudoViewModel = viewModel()
                    var currentScreenName by rememberSaveable { mutableStateOf(ScreenState.MENU.name) }
                    val currentScreen = try {
                        ScreenState.valueOf(currentScreenName)
                    } catch (e: Exception) {
                        ScreenState.MENU
                    }

                    when (currentScreen) {
                        ScreenState.MENU -> {
                            MainMenuScreen(
                                onStartGame = { playerCount, botCount ->
                                    viewModel.startNewGame(playerCount, botCount)
                                    currentScreenName = ScreenState.GAME.name
                                }
                            )
                        }
                        ScreenState.GAME -> {
                            GameScreen(
                                viewModel = viewModel,
                                onBackToMenu = { currentScreenName = ScreenState.MENU.name }
                            )
                        }
                    }
                }
            }
        }
    }
}
