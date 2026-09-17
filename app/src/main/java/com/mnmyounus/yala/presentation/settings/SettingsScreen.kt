package com.mnmyounus.yala.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnmyounus.yala.BuildConfig
import com.mnmyounus.yala.presentation.theme.AppThemeMode
import com.mnmyounus.yala.util.tvFocusable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val permissionStatus by viewModel.permissionStatus.collectAsStateWithLifecycle()

    // Permissions are granted in system Settings, outside this screen, so
    // re-check every time this screen comes back into view.
    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissionStatus()
        onPauseOrDispose { }
    }

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
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection(title = "Appearance") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AppThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, AppThemeMode.entries.size),
                            modifier = Modifier.tvFocusable()
                        ) { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    }
                }
            }

            SettingsSection(title = "Permissions") {
                PermissionRow(
                    icon = Icons.Filled.Accessibility,
                    title = "Accessibility Service",
                    description = "Required for real-time app lock detection",
                    granted = permissionStatus.accessibilityEnabled,
                    onOpen = viewModel::openAccessibilitySettings
                )
                PermissionRow(
                    icon = Icons.Filled.AdminPanelSettings,
                    title = "Device Admin",
                    description = "Prevents tampering and forced uninstall",
                    granted = permissionStatus.deviceAdminActive,
                    onOpen = viewModel::requestDeviceAdmin
                )
                PermissionRow(
                    icon = Icons.Filled.Layers,
                    title = "Display over other apps",
                    description = "Required to show the lock screen instantly",
                    granted = permissionStatus.overlayGranted,
                    onOpen = viewModel::openOverlaySettings
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                "YALA v${BuildConfig.VERSION_NAME}\nDeveloped by MNM YOUNUS\n100% Offline · Zero Ads · Zero Internet Permissions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card {
            Column(Modifier.padding(4.dp), content = content)
        }
    }
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onOpen: () -> Unit
) {
    ListItem(
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusDot(granted)
                if (!granted) {
                    TextButton(modifier = Modifier.tvFocusable(), onClick = onOpen) { Text("Enable") }
                } else {
                    Text("On", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}

@Composable
private fun StatusDot(granted: Boolean) {
    val color = if (granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}
