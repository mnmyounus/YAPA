package com.mnmyounus.yala.presentation.setup.lock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnmyounus.yala.domain.model.LockType
import com.mnmyounus.yala.util.Constants
import com.mnmyounus.yala.util.tvFocusable

@Composable
fun PinSetupScreen(
    packageName: String,
    onDone: () -> Unit,
    viewModel: LockSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var entered by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onDone() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (uiState.step == SetupStep.ENTER) "Set a 4-digit PIN" else "Confirm your PIN",
            style = MaterialTheme.typography.headlineMedium
        )
        uiState.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))

        PinDots(length = entered.length, total = Constants.PIN_LENGTH)
        Spacer(Modifier.height(32.dp))

        PinKeypad(
            onDigit = { digit ->
                if (entered.length < Constants.PIN_LENGTH) entered += digit
                if (entered.length == Constants.PIN_LENGTH) {
                    if (uiState.step == SetupStep.ENTER) {
                        viewModel.onFirstEntryComplete(entered)
                    } else {
                        viewModel.onConfirmEntry(
                            value = entered,
                            packageName = packageName,
                            label = packageName,
                            type = LockType.Pin,
                            hint = hint.ifBlank { null }
                        )
                    }
                    entered = ""
                }
            },
            onBackspace = { if (entered.isNotEmpty()) entered = entered.dropLast(1) }
        )

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

@Composable
private fun PinDots(length: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(total) { i ->
            val filled = i < length
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(20.dp)
            ) {}
        }
    }
}

@Composable
private fun PinKeypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "back")
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.width(260.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(keys) { key ->
            when (key) {
                "" -> Spacer(Modifier.size(64.dp))
                "back" -> IconButton(
                    modifier = Modifier.size(64.dp).tvFocusable(),
                    onClick = onBackspace
                ) { Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace") }
                else -> OutlinedButton(
                    modifier = Modifier.size(64.dp).tvFocusable(),
                    onClick = { onDigit(key) },
                    shape = MaterialTheme.shapes.large
                ) { Text(key, style = MaterialTheme.typography.titleLarge) }
            }
        }
    }
}
