package com.mnmyounus.yala.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Android does not let apps silently re-enable an AccessibilityService after
 * boot (the user must have it toggled on in system settings, which persists
 * across reboots by default). This receiver exists to run any lightweight
 * local state checks YALA needs at boot - e.g. verifying the locked-apps
 * database is intact - without ever contacting a network.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // No-op placeholder: real implementation may post a notification
            // reminding the user to confirm Accessibility + Device Admin are
            // still active if the OS ever revoked them.
        }
    }
}
