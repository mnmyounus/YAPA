package com.mnmyounus.yala.presentation.setup.lock

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mnmyounus.yala.data.local.crypto.CryptoManager
import com.mnmyounus.yala.domain.model.LockType
import com.mnmyounus.yala.util.Constants
import com.mnmyounus.yala.util.tvFocusable
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private enum class ImageSetupPhase { PICK_POOL, CHOOSE_SEQUENCE }

@Composable
fun ImageSequenceSetupScreen(
    packageName: String,
    onDone: () -> Unit,
    viewModel: LockSetupViewModel = hiltViewModel(),
    cryptoHolder: ImageCryptoHolder = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var phase by remember { mutableStateOf(ImageSetupPhase.PICK_POOL) }
    var pool by remember { mutableStateOf(listOf<String>()) }
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var sequenceLength by remember { mutableIntStateOf(Constants.IMAGE_SEQUENCE_LENGTH_OPTIONS.first()) }
    var hint by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onDone() }

    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(Constants.IMAGE_POOL_SIZE)
    ) { uris ->
        if (uris.isNotEmpty()) {
            pool = uris.map { it.toString() }
            phase = ImageSetupPhase.CHOOSE_SEQUENCE
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (phase) {
            ImageSetupPhase.PICK_POOL -> {
                Text("Choose ${Constants.IMAGE_POOL_SIZE} images", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "These become your private image pool. Only you know the secret order.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    modifier = Modifier.tvFocusable(),
                    onClick = {
                        pickImages.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) { Text("Pick from gallery") }
            }

            ImageSetupPhase.CHOOSE_SEQUENCE -> {
                Text(
                    if (uiState.step == SetupStep.ENTER) "Tap your secret sequence" else "Confirm the same sequence",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))

                if (uiState.step == SetupStep.ENTER) {
                    SingleChoiceSegmentedButtonRow {
                        Constants.IMAGE_SEQUENCE_LENGTH_OPTIONS.forEachIndexed { index, len ->
                            SegmentedButton(
                                selected = sequenceLength == len,
                                onClick = { sequenceLength = len },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = Constants.IMAGE_SEQUENCE_LENGTH_OPTIONS.size
                                )
                            ) { Text("$len images") }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Text("Selected: ${sequence.size} / $sequenceLength", style = MaterialTheme.typography.bodyLarge)
                uiState.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pool) { uri ->
                        val poolIndex = pool.indexOf(uri)
                        val orderPosition = sequence.indexOf(poolIndex).takeIf { it >= 0 }?.plus(1)
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (orderPosition != null) 3.dp else 1.dp,
                                    color = if (orderPosition != null) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .tvFocusable()
                                .clickable {
                                    if (sequence.size >= sequenceLength) return@clickable
                                    sequence = sequence + poolIndex
                                    if (sequence.size == sequenceLength) {
                                        val serialized = cryptoHolder.crypto.serializeImageSequence(sequence)
                                        if (uiState.step == SetupStep.ENTER) {
                                            viewModel.onFirstEntryComplete(serialized)
                                        } else {
                                            viewModel.onConfirmEntry(
                                                value = serialized,
                                                packageName = packageName,
                                                label = packageName,
                                                type = LockType.ImageSequence,
                                                hint = hint.ifBlank { null },
                                                imagePoolUris = pool,
                                                secretSequenceLength = sequenceLength
                                            )
                                        }
                                        sequence = emptyList()
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                            orderPosition?.let {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        "$it",
                                        color = Color.White,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.step == SetupStep.ENTER) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = hint,
                        onValueChange = { hint = it },
                        label = { Text("Optional hint for this app") },
                        modifier = Modifier.tvFocusable()
                    )
                }
            }
        }
    }
}

@HiltViewModel
class ImageCryptoHolder @Inject constructor(val crypto: CryptoManager) : androidx.lifecycle.ViewModel()
