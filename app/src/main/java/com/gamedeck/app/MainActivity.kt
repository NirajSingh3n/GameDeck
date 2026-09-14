package com.gamedeck.app

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gamedeck.app.data.ProfileStore
import com.gamedeck.app.service.OverlayService
import com.gamedeck.app.service.ScreenRecorderService
import com.gamedeck.app.ui.ConsoleScreen
import com.gamedeck.app.ui.LibraryScreen
import com.gamedeck.app.ui.ProfileScreen
import com.gamedeck.app.ui.SettingsScreen
import com.gamedeck.app.ui.theme.GameDeckTheme

class MainActivity : ComponentActivity() {

    private lateinit var store: ProfileStore

    private val projectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val i = Intent(this, ScreenRecorderService::class.java).apply {
                    putExtra("code", result.resultCode)
                    putExtra("data", result.data)
                }
                ContextCompat.startForegroundService(this, i)
            }
        }

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private val recordRequestReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            requestRecording()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ProfileStore(this)

        ContextCompat.registerReceiver(
            this,
            recordRequestReceiver,
            android.content.IntentFilter("com.gamedeck.START_RECORDING"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        if (Build.VERSION.SDK_INT >= 33) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }

        setContent {
            GameDeckTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(
                        store = store,
                        onLaunchPUBG = { launchPUBG() },
                        onStartOverlay = { OverlayService.start(this) },
                        onStartRecording = { requestRecording() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(recordRequestReceiver) } catch (e: Exception) {}
    }

    private fun requestRecording() {
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projectionLauncher.launch(mpm.createScreenCaptureIntent())
    }

    private fun launchPUBG() {
        val pkgs = listOf(
            "com.pubg.imobile", "com.tencent.ig", "com.pubg.krmobile",
            "com.vng.pubgmobile", "com.pubgmobile"
        )
        for (p in pkgs) {
            val i = packageManager.getLaunchIntentForPackage(p)
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                applyLaunchBoost()
                return
            }
        }
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.pubg.imobile")
                )
            )
        } catch (e: Exception) {}
    }

    private fun applyLaunchBoost() {
        if (store.overlayOnLaunch || store.triggersEnabled) {
            OverlayService.start(this)
        }
        if (store.dndOnLaunch) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (nm.isNotificationPolicyAccessGranted) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        }
    }
}

private data class NavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun AppRoot(
    store: ProfileStore,
    onLaunchPUBG: () -> Unit,
    onStartOverlay: () -> Unit,
    onStartRecording: () -> Unit
) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val items = listOf(
        NavItem("Console", Icons.Default.Home, "console"),
        NavItem("Games", Icons.Default.PlayArrow, "games"),
        NavItem("Profile", Icons.Default.Star, "profile"),
        NavItem("Settings", Icons.Default.Settings, "settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { nav.navigate(item.route) { launchSingleTop = true } },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "console",
            modifier = Modifier.padding(padding)
        ) {
            composable("console") {
                ConsoleScreen(
                    store = store,
                    onLaunchPUBG = onLaunchPUBG,
                    onStartOverlay = onStartOverlay,
                    onStartRecording = onStartRecording
                )
            }
            composable("games") {
                val context = androidx.compose.ui.platform.LocalContext.current
                LibraryScreen(onLaunch = { pkg ->
                    val i = context.packageManager.getLaunchIntentForPackage(pkg)
                    if (i != null) {
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(i)
                    }
                })
            }
            composable("profile") { ProfileScreen(store) }
            composable("settings") {
                val context = androidx.compose.ui.platform.LocalContext.current
                SettingsScreen(
                    onGrantOverlay = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.packageName)
                            )
                        )
                    },
                    onGrantDnd = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    },
                    onGrantWrite = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + context.packageName)
                            )
                        )
                    },
                    onGrantAccessibility = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }
        }
    }
}
