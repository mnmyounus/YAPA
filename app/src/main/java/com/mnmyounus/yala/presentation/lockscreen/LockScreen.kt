package com.mnmyounus.yala.presentation.lockscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnmyounus.yala.domain.model.LockType
import com.mnmyounus.yala.util.tvFocusable

/**
 * The single screen shown over a locked app. Renders a different input widget
 * depending on the app's configured [LockType], but always funnels through
 * [LockScreenViewModel.attemptUnlock] so success/failure handling (including
 * intruder capture, decided by the caller) stays in one place.
 */
@Composable
fun LockScreen(
    packageName: String,
    onUnlocked: () -> Unit,
    onFailedAttempt: () -> Unit,
    onRequestRecovery: () -> Unit,
    viewModel: LockScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(packageName) { viewModel.loadCredential(packageName) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val credential = uiState.credential
        if (credential == null) {
            CircularProgressIndicator()
            return@Column
        }

        Text("Locked", style = MaterialTheme.typography.headlineMedium)
        credential.hintText?.let {
            Spacer(Modifier.height(4.dp))
            Text("Hint: $it", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(24.dp))

        var input by remember { mutableStateOf("") }

        fun submit(value: String) {
            viewModel.attemptUnlock(packageName, value) { success ->
                if (success) onUnlocked() else onFailedAttempt()
            }
            input = ""
        }

        when (credential.type) {
            LockType.Pin -> LockPinEntry(onComplete = ::submit)
            LockType.Password -> LockPasswordEntry(onSubmit = ::submit)
            LockType.Pattern -> LockPatternEntry(onComplete = ::submit)
            LockType.ImageSequence -> LockImageSequenceEntry(
                pool = credential.imagePoolUris,
                sequenceLength = credential.secretSequenceLength,
                onComplete = ::submit
            )
        }

        if (uiState.showRecoveryOption) {
            Spacer(Modifier.height(24.dp))
            TextButton(modifier = Modifier.tvFocusable(), onClick = onRequestRecovery) {
                Text("Forgot your credential? Use recovery key")
            }
        }
    }
}
