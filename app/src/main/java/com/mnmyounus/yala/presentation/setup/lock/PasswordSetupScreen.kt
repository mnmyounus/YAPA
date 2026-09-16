package com.mnmyounus.yala.presentation.setup.lock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnmyounus.yala.domain.model.LockType
import com.mnmyounus.yala.util.Constants
import com.mnmyounus.yala.util.tvFocusable

@Composable
fun PasswordSetupScreen(
    packageName: String,
    onDone: () -> Unit,
    viewModel: LockSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onDone() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (uiState.step == SetupStep.ENTER) "Set an 8-character password" else "Confirm your password",
            style = MaterialTheme.typography.headlineMedium
        )
        uiState.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { if (it.length <= Constants.PASSWORD_LENGTH) input = it },
            label = { Text("Password (${input.length}/${Constants.PASSWORD_LENGTH})") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.tvFocusable()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = input.length == Constants.PASSWORD_LENGTH,
            modifier = Modifier.tvFocusable(),
            onClick = {
                if (uiState.step == SetupStep.ENTER) {
                    viewModel.onFirstEntryComplete(input)
                } else {
                    viewModel.onConfirmEntry(
                        value = input,
                        packageName = packageName,
                        label = packageName,
                        type = LockType.Password,
                        hint = hint.ifBlank { null }
                    )
                }
                input = ""
            }
        ) { Text(if (uiState.step == SetupStep.ENTER) "Next" else "Confirm") }

        if (uiState.step == SetupStep.ENTER) {
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = hint,
                onValueChange = { hint = it },
                label = { Text("Optional hint for this app") },
                modifier = Modifier.tvFocusable()
            )
        }
    }
}
