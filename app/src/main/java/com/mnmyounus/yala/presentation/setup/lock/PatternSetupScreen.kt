package com.mnmyounus.yala.presentation.setup.lock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mnmyounus.yala.data.local.crypto.CryptoManager
import com.mnmyounus.yala.domain.model.LockType
import com.mnmyounus.yala.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.math.sqrt
import javax.inject.Inject

@Composable
fun PatternSetupScreen(
    packageName: String,
    onDone: () -> Unit,
    viewModel: LockSetupViewModel = hiltViewModel(),
    cryptoHolder: PatternCryptoHolder = hiltViewModel()
) {
    val crypto = cryptoHolder.crypto
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedNodes by remember { mutableStateOf(listOf<Int>()) }
    var hint by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onDone() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (uiState.step == SetupStep.ENTER) "Draw your pattern" else "Confirm your pattern",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Connect at least ${Constants.PATTERN_MIN_NODES} dots",
            style = MaterialTheme.typography.bodyLarge
        )
        uiState.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))

        PatternGrid(
            gridSize = Constants.PATTERN_GRID_SIZE,
            onPatternComplete = { nodes ->
                selectedNodes = nodes
                if (nodes.size < Constants.PATTERN_MIN_NODES) return@PatternGrid
                val serialized = crypto.serializePattern(nodes)
                if (uiState.step == SetupStep.ENTER) {
                    viewModel.onFirstEntryComplete(serialized)
                } else {
                    viewModel.onConfirmEntry(
                        value = serialized,
                        packageName = packageName,
                        label = packageName,
                        type = LockType.Pattern,
                        hint = hint.ifBlank { null }
                    )
                }
            }
        )

        if (uiState.step == SetupStep.ENTER) {
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = hint,
                onValueChange = { hint = it },
                label = { Text("Optional hint for this app") }
            )
        }
    }
}

/**
 * NxN dot grid. Emits the ordered list of node indices (row-major, 0-based)
 * once the user lifts their finger, via [onPatternComplete].
 */
@Composable
private fun PatternGrid(gridSize: Int, onPatternComplete: (List<Int>) -> Unit) {
    val nodeCount = gridSize * gridSize
    var nodePositions by remember { mutableStateOf(listOf<Offset>()) }
    var selected by remember { mutableStateOf(listOf<Int>()) }
    val nodeRadiusPx = 24f
    val touchRadiusPx = 48f

    Canvas(
        modifier = Modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selected = nearestNode(offset, nodePositions, touchRadiusPx)?.let { listOf(it) } ?: emptyList()
                    },
                    onDrag = { change, _ ->
                        val node = nearestNode(change.position, nodePositions, touchRadiusPx)
                        if (node != null && node !in selected) selected = selected + node
                    },
                    onDragEnd = {
                        onPatternComplete(selected)
                        selected = emptyList()
                    }
                )
            }
    ) {
        val spacing = size.minDimension / (gridSize + 1)
        nodePositions = (0 until nodeCount).map { i ->
            val row = i / gridSize
            val col = i % gridSize
            Offset(spacing * (col + 1), spacing * (row + 1))
        }

        // Draw connecting lines
        for (i in 0 until selected.size - 1) {
            drawLine(
                color = Color(0xFF2962FF),
                start = nodePositions[selected[i]],
                end = nodePositions[selected[i + 1]],
                strokeWidth = 8f
            )
        }
        // Draw nodes
        nodePositions.forEachIndexed { index, pos ->
            drawCircle(
                color = if (index in selected) Color(0xFF2962FF) else Color(0xFF9E9E9E),
                radius = nodeRadiusPx,
                center = pos
            )
        }
    }
}

private fun nearestNode(point: Offset, positions: List<Offset>, maxDistance: Float): Int? {
    var closestIndex: Int? = null
    var closestDist = Float.MAX_VALUE
    positions.forEachIndexed { index, pos ->
        val dx = point.x - pos.x
        val dy = point.y - pos.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < maxDistance && dist < closestDist) {
            closestDist = dist
            closestIndex = index
        }
    }
    return closestIndex
}

// Small Hilt-injected holder so this stateless Composable file can reach CryptoManager
// without threading it through every caller manually.
@HiltViewModel
class PatternCryptoHolder @Inject constructor(val crypto: CryptoManager) : ViewModel()
