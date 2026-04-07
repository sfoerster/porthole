package com.stevenfoerster.porthole.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stevenfoerster.porthole.notification.SessionForegroundService
import com.stevenfoerster.porthole.session.SessionState
import com.stevenfoerster.porthole.ui.navigation.PortholeRoutes
import com.stevenfoerster.porthole.ui.theme.PortholeTheme
import com.stevenfoerster.porthole.ui.viewmodel.MainViewModel
import com.stevenfoerster.porthole.ui.viewmodel.PortalViewModel
import com.stevenfoerster.porthole.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the Porthole application.
 *
 * Sets up Jetpack Compose navigation between the first-run screen, main screen,
 * portal screen, and settings screen. Manages the foreground service lifecycle
 * for the session notification.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permission result handled — notification will show or not based on grant
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            PortholeTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = hiltViewModel()
                val firstRunCompleted by mainViewModel.firstRunCompleted.collectAsState()

                val startDestination = if (firstRunCompleted) {
                    PortholeRoutes.MAIN
                } else {
                    PortholeRoutes.FIRST_RUN
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                ) {
                    composable(PortholeRoutes.FIRST_RUN) {
                        FirstRunScreen(
                            onConfirm = {
                                mainViewModel.completeFirstRun()
                                navController.navigate(PortholeRoutes.MAIN) {
                                    popUpTo(PortholeRoutes.FIRST_RUN) { inclusive = true }
                                }
                            },
                        )
                    }

                    composable(PortholeRoutes.MAIN) {
                        val sessionState by mainViewModel.sessionState.collectAsState()
                        val remainingSeconds by mainViewModel.remainingSeconds.collectAsState()
                        val config by mainViewModel.sessionConfig.collectAsState()

                        MainScreen(
                            viewModel = mainViewModel,
                            onLaunchSession = { gateway ->
                                startForegroundService(
                                    SessionForegroundService.startIntent(
                                        this@MainActivity,
                                        config.effectiveTimeoutSeconds,
                                    ),
                                )
                                navController.navigate(
                                    "${PortholeRoutes.PORTAL}/$gateway" +
                                        "/${config.jsEnabled}" +
                                        "/${config.strictMode}",
                                )
                            },
                            onNavigateToSettings = {
                                navController.navigate(PortholeRoutes.SETTINGS)
                            },
                        )
                    }

                    composable(
                        route = "${PortholeRoutes.PORTAL}/{gateway}/{jsEnabled}/{strictMode}",
                        arguments = listOf(
                            navArgument("gateway") { type = NavType.StringType },
                            navArgument("jsEnabled") { type = NavType.BoolType },
                            navArgument("strictMode") { type = NavType.BoolType },
                        ),
                    ) { backStackEntry ->
                        val gateway = backStackEntry.arguments?.getString("gateway") ?: return@composable
                        val jsEnabled = backStackEntry.arguments?.getBoolean("jsEnabled") ?: false
                        val strictMode = backStackEntry.arguments?.getBoolean("strictMode") ?: true
                        val portalViewModel: PortalViewModel = hiltViewModel()

                        PortalScreen(
                            viewModel = portalViewModel,
                            gatewayIp = gateway,
                            jsEnabled = jsEnabled,
                            strictMode = strictMode,
                            connectivityCheckUrl = com.stevenfoerster.porthole.network
                                .ConnectivityChecker.DEFAULT_CHECK_URL,
                            onSessionEnded = {
                                startService(
                                    SessionForegroundService.stopIntent(this@MainActivity),
                                )
                                navController.popBackStack(PortholeRoutes.MAIN, inclusive = false)
                            },
                        )
                    }

                    composable(PortholeRoutes.SETTINGS) {
                        val settingsViewModel: SettingsViewModel = hiltViewModel()
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
