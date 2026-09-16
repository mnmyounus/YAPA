package com.mnmyounus.yala.service

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mnmyounus.yala.domain.model.UnlockOutcome
import com.mnmyounus.yala.presentation.intruder.IntruderCameraCapture
import com.mnmyounus.yala.presentation.lockscreen.LockScreen
import com.mnmyounus.yala.presentation.theme.YalaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import javax.inject.Inject

/**
 * Shown full-screen, on top of whatever locked app just opened. Deliberately
 * NOT declared as a system alert-window overlay (which is fragile across OEM
 * skins) but as a normal Activity launched with FLAG_ACTIVITY_NEW_TASK from
 * the accessibility service, using window flags to appear above the lock
 * screen / keyguard if needed and to prevent screenshots of the entry UI.
 */
@AndroidEntryPoint
class LockOverlayActivity : ComponentActivity() {

    @Inject lateinit var intruderCameraCapture: IntruderCameraCapture

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SECURE // blocks screenshots/screen-recording of the lock entry UI
        )

        val targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE).orEmpty()

        setContent {
            YalaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LockScreen(
                        packageName = targetPackage,
                        onUnlocked = {
                            intruderCameraCapture.captureSilently(
                                context = this,
                                lifecycleOwner = this,
                                packageNameAttempted = targetPackage,
                                outcome = UnlockOutcome.SUCCESS,
                                scope = scope
                            )
                            finish()
                        },
                        onFailedAttempt = {
                            intruderCameraCapture.captureSilently(
                                context = this,
                                lifecycleOwner = this,
                                packageNameAttempted = targetPackage,
                                outcome = UnlockOutcome.FAILURE,
                                scope = scope
                            )
                        },
                        onRequestRecovery = {
                            // Real implementation navigates to a recovery-key challenge screen
                            // that, on success, calls finish() the same as onUnlocked above.
                        }
                    )
                }
            }
        }
    }

    /** Back button must never dismiss the lock screen and reveal the app underneath. */
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }
}
