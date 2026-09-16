package com.mnmyounus.yala.presentation.recovery

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnmyounus.yala.domain.usecase.GenerateRecoveryKeyUseCase
import com.mnmyounus.yala.util.tvFocusable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@Composable
fun RecoveryKeyScreen(
    onDone: () -> Unit,
    viewModel: RecoveryKeyViewModel = hiltViewModel()
) {
    val recoveryKey by viewModel.recoveryKey.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.generateIfNeeded() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your Emergency Recovery Key", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            "Save this somewhere safe. It's the only way back in if you forget your lock and it never leaves this device unless you export it.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))

        recoveryKey?.let { key ->
            Card {
                Text(
                    key,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(24.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(modifier = Modifier.tvFocusable(), onClick = { copyToClipboard(context, key) }) {
                    Text("Copy")
                }
                OutlinedButton(modifier = Modifier.tvFocusable(), onClick = { viewModel.saveToFile(context, key) }) {
                    Text("Save as .txt")
                }
            }
        } ?: CircularProgressIndicator()

        Spacer(Modifier.height(32.dp))
        Button(modifier = Modifier.tvFocusable(), onClick = onDone) { Text("Done") }
    }
}

private fun copyToClipboard(context: Context, key: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("YALA recovery key", key))
}

@HiltViewModel
class RecoveryKeyViewModel @Inject constructor(
    private val generateRecoveryKeyUseCase: GenerateRecoveryKeyUseCase
) : ViewModel() {

    private val _recoveryKey = MutableStateFlow<String?>(null)
    val recoveryKey: StateFlow<String?> = _recoveryKey.asStateFlow()

    fun generateIfNeeded() {
        if (_recoveryKey.value != null) return
        viewModelScope.launch { _recoveryKey.value = generateRecoveryKeyUseCase() }
    }

    fun saveToFile(context: Context, key: String) {
        val dir = File(context.filesDir, "recovery").apply { if (!exists()) mkdirs() }
        File(dir, "yala_recovery_key.txt").writeText(
            "YALA Recovery Key: $key\n\nKeep this private. Anyone with this key can bypass your app locks."
        )
    }
}
