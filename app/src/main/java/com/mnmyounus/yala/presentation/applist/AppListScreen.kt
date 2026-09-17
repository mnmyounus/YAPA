package com.mnmyounus.yala.presentation.applist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnmyounus.yala.domain.model.InstalledApp
import com.mnmyounus.yala.util.isTvDevice
import com.mnmyounus.yala.util.tvFocusable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    onLockWithPin: (String) -> Unit,
    onLockWithPassword: (String) -> Unit,
    onLockWithPattern: (String) -> Unit,
    onLockWithImageSequence: (String) -> Unit,
    onOpenRecoveryKey: () -> Unit,
    onOpenIntruderGallery: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: AppListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YALA") },
                actions = {
                    IconButton(modifier = Modifier.tvFocusable(), onClick = onOpenIntruderGallery) {
                        Icon(Icons.Filled.Photo, contentDescription = "Intruder gallery")
                    }
                    IconButton(modifier = Modifier.tvFocusable(), onClick = onOpenRecoveryKey) {
                        Icon(Icons.Filled.VpnKey, contentDescription = "Recovery key")
                    }
                    IconButton(modifier = Modifier.tvFocusable(), onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Search apps") },
                modifier = Modifier.fillMaxWidth().padding(16.dp).tvFocusable()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show system apps")
                Switch(
                    checked = uiState.showSystemApps,
                    onCheckedChange = { viewModel.toggleShowSystemApps() },
                    modifier = Modifier.tvFocusable()
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.apps, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            onLockWithPin = onLockWithPin,
                            onLockWithPassword = onLockWithPassword,
                            onLockWithPattern = onLockWithPattern,
                            onLockWithImageSequence = onLockWithImageSequence,
                            onUnlock = viewModel::unlockApp
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    onLockWithPin: (String) -> Unit,
    onLockWithPassword: (String) -> Unit,
    onLockWithPattern: (String) -> Unit,
    onLockWithImageSequence: (String) -> Unit,
    onUnlock: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    // Most Android TV boxes have no gallery/photos app to fulfil the image
    // picker Image Sequence lock needs, which throws ActivityNotFoundException
    // and crashes the app. Hide that option there rather than offer something
    // that will fail.
    val isTv = LocalContext.current.isTvDevice()

    ListItem(
        headlineContent = { Text(app.label) },
        supportingContent = { if (app.isSystemApp) Text("System app", style = MaterialTheme.typography.labelLarge) },
        leadingContent = {
            Icon(
                if (app.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = null
            )
        },
        trailingContent = {
            Box {
                IconButton(modifier = Modifier.tvFocusable(), onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Lock options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    if (app.isLocked) {
                        DropdownMenuItem(text = { Text("Remove lock") }, onClick = {
                            onUnlock(app.packageName); menuExpanded = false
                        })
                    } else {
                        DropdownMenuItem(text = { Text("Lock with PIN") }, onClick = {
                            onLockWithPin(app.packageName); menuExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Lock with Password") }, onClick = {
                            onLockWithPassword(app.packageName); menuExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Lock with Pattern") }, onClick = {
                            onLockWithPattern(app.packageName); menuExpanded = false
                        })
                        if (!isTv) {
                            DropdownMenuItem(text = { Text("Lock with Image Sequence") }, onClick = {
                                onLockWithImageSequence(app.packageName); menuExpanded = false
                            })
                        }
                    }
                }
            }
        }
    )
}
