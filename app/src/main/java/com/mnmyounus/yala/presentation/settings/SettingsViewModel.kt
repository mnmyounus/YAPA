package com.mnmyounus.yala.presentation.settings

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mnmyounus.yala.domain.repository.LockRepository
import com.mnmyounus.yala.presentation.theme.AppThemeMode
import com.mnmyounus.yala.service.AppLockAccessibilityService
import com.mnmyounus.yala.service.YalaDeviceAdminReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionStatus(
    val accessibilityEnabled: Boolean = false,
    val deviceAdminActive: Boolean = false,
    val overlayGranted: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockRepository: LockRepository
) : ViewModel() {

    val themeMode: StateFlow<AppThemeMode> = lockRepository.observeThemeMode()
        .map { ordinal -> AppThemeMode.entries.getOrElse(ordinal) { AppThemeMode.SYSTEM } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppThemeMode.SYSTEM)

    private val _permissionStatus = MutableStateFlow(PermissionStatus())
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus

    init {
        refreshPermissionStatus()
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { lockRepository.setThemeMode(mode.ordinal) }
    }

    /** Re-reads live permission state from the OS - call when the screen resumes,
     *  since the user grants these in system Settings and comes back. */
    fun refreshPermissionStatus() {
        _permissionStatus.value = PermissionStatus(
            accessibilityEnabled = isAccessibilityServiceEnabled(),
            deviceAdminActive = isDeviceAdminActive(),
            overlayGranted = Settings.canDrawOverlays(context)
        )
    }

    fun openAccessibilitySettings() {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun requestDeviceAdmin() {
        val componentName = ComponentName(context, YalaDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Prevents YALA from being force-stopped or uninstalled without your unlock credential."
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = "${context.packageName}/${AppLockAccessibilityService::class.java.name}"
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    private fun isDeviceAdminActive(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return false
        return dpm.isAdminActive(ComponentName(context, YalaDeviceAdminReceiver::class.java))
    }
}
