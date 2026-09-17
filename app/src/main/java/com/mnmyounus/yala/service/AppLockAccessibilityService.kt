package com.mnmyounus.yala.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.mnmyounus.yala.domain.repository.AppRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Watches window-state-change events to detect which app just came to the
 * foreground. Every time the foreground app changes to a *different* app
 * (not just a different window within the same app), it checks whether that
 * app is locked and, if so, shows the lock overlay - deliberately re-checking
 * on every switch rather than remembering "already unlocked" across app
 * switches, since that's the secure behavior an app locker needs.
 *
 * This never inspects window *content* for locked apps beyond the package
 * name of the event - it does not read text, screenshots, or keystrokes from
 * other apps.
 */
@AndroidEntryPoint
class AppLockAccessibilityService : AccessibilityService() {

    @Inject lateinit var appRepository: AppRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    // Dedupes the flood of events Android can fire for the same still-
    // foregrounded app, so we don't re-check/re-show the lock screen on
    // every single one of them.
    private var lastCheckedPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (pkg == packageName) return // ignore YALA's own UI, including the lock screen itself
        if (pkg == lastCheckedPackage) return
        lastCheckedPackage = pkg

        serviceScope.launch {
            if (appRepository.isAppLocked(pkg)) {
                showLockOverlay(pkg)
            }
        }
    }

    private fun showLockOverlay(packageName: String) {
        val intent = Intent(this, LockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(LockOverlayActivity.EXTRA_TARGET_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() { /* no-op */ }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
