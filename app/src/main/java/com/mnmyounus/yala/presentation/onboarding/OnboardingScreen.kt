package com.mnmyounus.yala.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mnmyounus.yala.util.tvFocusable

private enum class OnboardingStep {
    WELCOME, PERMISSION_ACCESSIBILITY, PERMISSION_DEVICE_ADMIN,
    PERMISSION_OVERLAY, PERMISSION_CAMERA, FEATURE_TOUR, PRIVACY_DISCLAIMER
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }
    val steps = OnboardingStep.entries
    val index = steps.indexOf(step)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { (index + 1) / steps.size.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))

        when (step) {
            OnboardingStep.WELCOME -> OnboardingCard(
                title = "Welcome to YALA",
                body = "Your App Lock and Analyzer. 100% offline — no ads, no internet permission, no data ever leaves this device."
            )
            OnboardingStep.PERMISSION_ACCESSIBILITY -> OnboardingCard(
                title = "Enable Accessibility Access",
                body = "YALA uses this to instantly detect when a locked app opens, so it can show the lock screen before you see any content inside it.",
                actionLabel = "Open Accessibility Settings",
                onAction = viewModel::openAccessibilitySettings
            )
            OnboardingStep.PERMISSION_DEVICE_ADMIN -> OnboardingCard(
                title = "Enable Device Admin",
                body = "This stops someone from simply force-stopping or uninstalling YALA to bypass your locks.",
                actionLabel = "Activate Device Admin",
                onAction = viewModel::requestDeviceAdmin
            )
            OnboardingStep.PERMISSION_OVERLAY -> OnboardingCard(
                title = "Allow Display Over Other Apps",
                body = "Needed so the lock screen can appear on top of a locked app the instant it opens.",
                actionLabel = "Grant Overlay Permission",
                onAction = viewModel::openOverlaySettings
            )
            OnboardingStep.PERMISSION_CAMERA -> OnboardingCard(
                title = "Enable Camera (Optional)",
                body = "Lets YALA quietly take a photo on unlock attempts, sorted into Successful and Failed folders inside the app. Photos never leave your device. You can skip this and enable it later."
            )
            OnboardingStep.FEATURE_TOUR -> OnboardingCard(
                title = "How YALA Works",
                body = "Lock individual apps with their own PIN, password, pattern, or secret image sequence — or apply one lock to everything at once. Works for system apps too, like Settings."
            )
            OnboardingStep.PRIVACY_DISCLAIMER -> OnboardingCard(
                title = "Your Privacy",
                body = "YALA stores everything — credentials, recovery keys, and intruder photos — encrypted, locally, on this device only. There are no analytics, no ads, and no network permission in this app. By continuing you agree this is a security tool for your own device."
            )
        }

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (index > 0) {
                OutlinedButton(
                    modifier = Modifier.tvFocusable(),
                    onClick = { step = steps[index - 1] }
                ) { Text("Back") }
            }
            Button(
                modifier = Modifier.tvFocusable(),
                onClick = {
                    if (index == steps.lastIndex) {
                        viewModel.completeOnboarding()
                        onFinished()
                    } else {
                        step = steps[index + 1]
                    }
                }
            ) {
                Text(if (index == steps.lastIndex) "Get Started" else "Next")
            }
        }
    }
}

@Composable
private fun OnboardingCard(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Text(body, style = MaterialTheme.typography.bodyLarge)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(20.dp))
                Button(modifier = Modifier.tvFocusable(), onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}
