package com.mnmyounus.yala.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mnmyounus.yala.presentation.applist.AppListScreen
import com.mnmyounus.yala.presentation.intruder.IntruderGalleryScreen
import com.mnmyounus.yala.presentation.onboarding.OnboardingScreen
import com.mnmyounus.yala.presentation.recovery.RecoveryKeyScreen
import com.mnmyounus.yala.presentation.settings.SettingsScreen
import com.mnmyounus.yala.presentation.setup.lock.ImageSequenceSetupScreen
import com.mnmyounus.yala.presentation.setup.lock.PasswordSetupScreen
import com.mnmyounus.yala.presentation.setup.lock.PatternSetupScreen
import com.mnmyounus.yala.presentation.setup.lock.PinSetupScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val APP_LIST = "app_list"
    const val SETUP_PIN = "setup_pin/{packageName}"
    const val SETUP_PASSWORD = "setup_password/{packageName}"
    const val SETUP_PATTERN = "setup_pattern/{packageName}"
    const val SETUP_IMAGE_SEQUENCE = "setup_image_sequence/{packageName}"
    const val RECOVERY_KEY = "recovery_key"
    const val INTRUDER_GALLERY = "intruder_gallery"
    const val SETTINGS = "settings"

    fun setupPin(pkg: String) = "setup_pin/$pkg"
    fun setupPassword(pkg: String) = "setup_password/$pkg"
    fun setupPattern(pkg: String) = "setup_pattern/$pkg"
    fun setupImageSequence(pkg: String) = "setup_image_sequence/$pkg"
}

@Composable
fun YalaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.ONBOARDING
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinished = {
                navController.navigate(Routes.APP_LIST) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.APP_LIST) {
            AppListScreen(
                onLockWithPin = { pkg -> navController.navigate(Routes.setupPin(pkg)) },
                onLockWithPassword = { pkg -> navController.navigate(Routes.setupPassword(pkg)) },
                onLockWithPattern = { pkg -> navController.navigate(Routes.setupPattern(pkg)) },
                onLockWithImageSequence = { pkg -> navController.navigate(Routes.setupImageSequence(pkg)) },
                onOpenRecoveryKey = { navController.navigate(Routes.RECOVERY_KEY) },
                onOpenIntruderGallery = { navController.navigate(Routes.INTRUDER_GALLERY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETUP_PIN) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString("packageName").orEmpty()
            PinSetupScreen(packageName = pkg, onDone = { navController.popBackStack() })
        }
        composable(Routes.SETUP_PASSWORD) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString("packageName").orEmpty()
            PasswordSetupScreen(packageName = pkg, onDone = { navController.popBackStack() })
        }
        composable(Routes.SETUP_PATTERN) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString("packageName").orEmpty()
            PatternSetupScreen(packageName = pkg, onDone = { navController.popBackStack() })
        }
        composable(Routes.SETUP_IMAGE_SEQUENCE) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString("packageName").orEmpty()
            ImageSequenceSetupScreen(packageName = pkg, onDone = { navController.popBackStack() })
        }
        composable(Routes.RECOVERY_KEY) {
            RecoveryKeyScreen(onDone = { navController.popBackStack() })
        }
        composable(Routes.INTRUDER_GALLERY) {
            IntruderGalleryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
