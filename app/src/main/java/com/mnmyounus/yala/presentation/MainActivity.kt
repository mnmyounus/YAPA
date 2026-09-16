package com.mnmyounus.yala.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mnmyounus.yala.presentation.navigation.Routes
import com.mnmyounus.yala.presentation.navigation.YalaNavGraph
import com.mnmyounus.yala.presentation.theme.YalaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isOnboarded by viewModel.isOnboarded.collectAsState()

            YalaTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Wait for the first emission before deciding the start destination
                    // so we never flash the app list before onboarding on a fresh install.
                    isOnboarded?.let { onboarded ->
                        YalaNavGraph(
                            startDestination = if (onboarded) Routes.APP_LIST else Routes.ONBOARDING
                        )
                    }
                }
            }
        }
    }
}
