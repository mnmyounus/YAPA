package com.mnmyounus.yala.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Activating Device Admin lets YALA intercept the OS's own "deactivate device
 * admin" step, which is required by Android before this app can be
 * force-stopped or uninstalled — closing the most common way an intruder
 * would try to disable app protection instead of just unlocking it.
 *
 * YALA does NOT use Device Admin to wipe data, lock the whole device, or
 * change the system lock screen — only [android:disable-keyguard-features]
 * and [android:watch-login], declared in device_admin_policies.xml.
 */
class YalaDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "YALA anti-tamper protection enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        // Shown by the OS before admin is deactivated - the user still has full control,
        // this is purely a "are you sure?" prompt the OS itself renders.
        return "Disabling this will allow YALA to be uninstalled or force-stopped without your unlock credential."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "YALA anti-tamper protection disabled", Toast.LENGTH_SHORT).show()
    }
}
