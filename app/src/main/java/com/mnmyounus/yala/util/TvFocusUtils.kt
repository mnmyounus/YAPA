package com.mnmyounus.yala.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp

fun Context.isTvDevice(): Boolean =
    packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
        packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)

/**
 * Draws a visible focus ring when this composable is focused via D-pad, and
 * is a no-op visually on touch devices where focus state rarely changes.
 * Applied to every actionable element (grid apps, keypad digits, pattern
 * nodes, image tiles) so TV navigation is always legible.
 */
@Composable
fun Modifier.tvFocusable(): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val color = MaterialTheme.colorScheme.primary
    return this
        .onFocusChanged { isFocused = it.isFocused }
        .then(
            if (isFocused) Modifier.border(3.dp, color, RoundedCornerShape(12.dp))
            else Modifier
        )
}
