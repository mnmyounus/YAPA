package com.mnmyounus.yala.presentation.lockscreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mnmyounus.yala.data.local.crypto.CryptoManager
import com.mnmyounus.yala.util.Constants
import com.mnmyounus.yala.util.tvFocusable
import kotlin.math.sqrt

@Composable
fun LockPinEntry(onComplete: (String) -> Unit) {
    var entered by remember { mutableStateOf("") }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(Constants.PIN_LENGTH) { i ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (i < entered.length) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(20.dp)
            ) {}
        }
    }
    Spacer(Modifier.height(32.dp))
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
                    onClick = { if (entered.isNotEmpty()) entered = entered.dropLast(1) }
                ) { Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace") }
                else -> OutlinedButton(
                    modifier = Modifier.size(64.dp).tvFocusable(),
                    onClick = {
                        if (entered.length < Constants.PIN_LENGTH) entered += key
                        if (entered.length == Constants.PIN_LENGTH) {
                            onComplete(entered)
                            entered = ""
                        }
                    },
                    shape = MaterialTheme.shapes.large
                ) { Text(key, style = MaterialTheme.typography.titleLarge) }
            }
        }
    }
}

@Composable
fun LockPasswordEntry(onSubmit: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    OutlinedTextField(
        value = input,
        onValueChange = { if (it.length <= Constants.PASSWORD_LENGTH) input = it },
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.tvFocusable()
    )
    Spacer(Modifier.height(16.dp))
    Button(
        enabled = input.isNotEmpty(),
        modifier = Modifier.tvFocusable(),
        onClick = { onSubmit(input) }
    ) { Text("Unlock") }
}

@Composable
fun LockPatternEntry(onComplete: (String) -> Unit) {
    val crypto = remember { CryptoManager() }
    var nodePositions by remember { mutableStateOf(listOf<Offset>()) }
    var selected by remember { mutableStateOf(listOf<Int>()) }
    val gridSize = Constants.PATTERN_GRID_SIZE
    val nodeCount = gridSize * gridSize
    val touchRadiusPx = 48f

    Canvas(
        modifier = Modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selected = nearestNodeIndex(offset, nodePositions, touchRadiusPx)?.let { listOf(it) } ?: emptyList()
                    },
                    onDrag = { change, _ ->
                        val node = nearestNodeIndex(change.position, nodePositions, touchRadiusPx)
                        if (node != null && node !in selected) selected = selected + node
                    },
                    onDragEnd = {
                        if (selected.size >= Constants.PATTERN_MIN_NODES) {
                            onComplete(crypto.serializePattern(selected))
                        }
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
        for (i in 0 until selected.size - 1) {
            drawLine(Color(0xFF2962FF), nodePositions[selected[i]], nodePositions[selected[i + 1]], strokeWidth = 8f)
        }
        nodePositions.forEachIndexed { index, pos ->
            drawCircle(
                color = if (index in selected) Color(0xFF2962FF) else Color(0xFF9E9E9E),
                radius = 24f,
                center = pos
            )
        }
    }
}

@Composable
fun LockImageSequenceEntry(pool: List<String>, sequenceLength: Int, onComplete: (String) -> Unit) {
    val crypto = remember { CryptoManager() }
    var selected by remember { mutableStateOf(listOf<Int>()) }

    Text("Tap the secret order (${selected.size}/$sequenceLength)", style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(16.dp))
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pool) { uri ->
            val poolIndex = pool.indexOf(uri)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (poolIndex in selected) 3.dp else 1.dp,
                        color = if (poolIndex in selected) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .tvFocusable()
                    .clickable {
                        if (selected.size >= sequenceLength) return@clickable
                        selected = selected + poolIndex
                        if (selected.size == sequenceLength) {
                            onComplete(crypto.serializeImageSequence(selected))
                            selected = emptyList()
                        }
                    }
            ) {
                AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

private fun nearestNodeIndex(point: Offset, positions: List<Offset>, maxDistance: Float): Int? {
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
