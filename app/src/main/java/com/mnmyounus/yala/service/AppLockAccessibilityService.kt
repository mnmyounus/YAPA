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
 * foreground. When it's a locked app that hasn't yet been unlocked in this
 * "session" (tracked by [unlockedThisSession]), it launches the lock overlay
 * activity on top of it.
 *
 * This never inspects window *content* for locked apps beyond the package
 * name of the event — it does not read text, screenshots, or keystrokes from
 * other apps.
 */
@AndroidEntryPoint
class AppLockAccessibilityService : AccessibilityService() {

    @Inject lateinit var appRepository: AppRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    // Packages the user has already unlocked since their last home/screen-off event.
    private val unlockedThisSession = mutableSetOf<String>()
    private var lastPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (pkg == packageName) return // ignore YALA's own UI
        if (pkg == lastPackage) return
        lastPackage = pkg

        if (pkg == LAUNCHER_PACKAGE_HINT) {
            unlockedThisSession.clear()
            return
        }

        if (pkg in unlockedThisSession) return

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

    fun markUnlocked(packageName: String) {
        unlockedThisSession.add(packageName)
    }

    override fun onInterrupt() { /* no-op */ }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private companion object {
        // Best-effort default launcher hint; real implementation resolves this
        // dynamically via PackageManager.resolveActivity(ACTION_MAIN/CATEGORY_HOME).
        const val LAUNCHER_PACKAGE_HINT = "com.android.launcher3"
    }
}
