package com.mnmyounus.yala.presentation.intruder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.mnmyounus.yala.domain.model.IntruderCapture
import com.mnmyounus.yala.domain.model.UnlockOutcome
import com.mnmyounus.yala.domain.repository.IntruderRepository
import com.mnmyounus.yala.util.tvFocusable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntruderGalleryScreen(
    onBack: () -> Unit,
    viewModel: IntruderGalleryViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val successCaptures by viewModel.successCaptures.collectAsState()
    val failureCaptures by viewModel.failureCaptures.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intruder Captures") },
                navigationIcon = {
                    IconButton(modifier = Modifier.tvFocusable(), onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Successful Unlocks (${successCaptures.size})") },
                    modifier = Modifier.tvFocusable()
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Failed Attempts (${failureCaptures.size})") },
                    modifier = Modifier.tvFocusable()
                )
            }

            val captures = if (selectedTab == 0) successCaptures else failureCaptures
            if (captures.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No captures yet",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(120.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(captures, key = { it.id }) { capture ->
                        IntruderThumb(capture)
                    }
                }
            }
        }
    }
}

@Composable
private fun IntruderThumb(capture: IntruderCapture) {
    Column {
        AsyncImage(
            model = capture.filePath,
            contentDescription = null,
            modifier = Modifier.height(140.dp).clip(MaterialTheme.shapes.medium)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(capture.timestampEpochMillis)),
            style = MaterialTheme.typography.labelLarge
        )
        Text(capture.packageNameAttempted, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@HiltViewModel
class IntruderGalleryViewModel @Inject constructor(
    intruderRepository: IntruderRepository
) : ViewModel() {

    val successCaptures: StateFlow<List<IntruderCapture>> =
        intruderRepository.observeCaptures(UnlockOutcome.SUCCESS)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val failureCaptures: StateFlow<List<IntruderCapture>> =
        intruderRepository.observeCaptures(UnlockOutcome.FAILURE)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
