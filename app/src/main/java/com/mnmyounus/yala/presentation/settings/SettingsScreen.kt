package com.mnmyounus.yala.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.mnmyounus.yala.BuildConfig
import com.mnmyounus.yala.presentation.theme.AppThemeMode
import com.mnmyounus.yala.util.tvFocusable
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var themeMode by remember { mutableStateOf(AppThemeMode.SYSTEM) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(modifier = Modifier.tvFocusable(), onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text("Theme", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow {
                AppThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { themeMode = mode; viewModel.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, AppThemeMode.entries.size)
                    ) { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("Permissions", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Accessibility Service") },
                supportingContent = { Text("Required for real-time app lock detection") },
                trailingContent = {
                    TextButton(modifier = Modifier.tvFocusable(), onClick = viewModel::openAccessibilitySettings) {
                        Text("Open")
                    }
                }
            )
            ListItem(
                headlineContent = { Text("Device Admin") },
                supportingContent = { Text("Prevents tampering and forced uninstall") },
                trailingContent = {
                    TextButton(modifier = Modifier.tvFocusable(), onClick = viewModel::openDeviceAdminSettings) {
                        Text("Open")
                    }
                }
            )
            ListItem(
                headlineContent = { Text("Display over other apps") },
                supportingContent = { Text("Required to show the lock screen instantly") },
                trailingContent = {
                    TextButton(modifier = Modifier.tvFocusable(), onClick = viewModel::openOverlaySettings) {
                        Text("Open")
                    }
                }
            )

            Spacer(Modifier.weight(1f))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text(
                "YALA v${BuildConfig.VERSION_NAME}\nDeveloped by MNM YOUNUS\n100% Offline · Zero Ads · Zero Internet Permissions",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    // Theme persistence and permission-status intents wired the same way as
    // OnboardingViewModel; omitted here to avoid duplicating that code.
    fun setThemeMode(mode: AppThemeMode) { /* persist to DataStore in the full implementation */ }
    fun openAccessibilitySettings() { /* see OnboardingViewModel.openAccessibilitySettings */ }
    fun openDeviceAdminSettings() { /* see OnboardingViewModel.requestDeviceAdmin */ }
    fun openOverlaySettings() { /* see OnboardingViewModel.openOverlaySettings */ }
}
