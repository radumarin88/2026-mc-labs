package mobile.posts

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * The runtime-permission touch (Lab 7). WorkManager syncs in the background; here we tell the user it
 * happened with a notification. On Android 13 (API 33)+ posting a notification needs the runtime
 * permission POST_NOTIFICATIONS, so this file also holds the request. Provided.
 */
private const val SYNC_CHANNEL_ID = "posts-sync"

/** Create the low-importance sync channel. Safe to call repeatedly (required since API 26). */
private fun ensureSyncChannel(context: Context) {
    val channel = NotificationChannel(SYNC_CHANNEL_ID, "Sync", NotificationManager.IMPORTANCE_LOW)
        .apply { description = "Posts sync status" }
    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}

/**
 * Post a "posts synced" notification. On API 33+ this is guarded by the POST_NOTIFICATIONS grant: if the
 * user has not granted it, we return quietly (and `notify` would no-op anyway). Called from the worker.
 */
fun showSyncNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    ensureSyncChannel(context)
    val notification = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_notify_sync)
        .setContentTitle("Posts synced")
        .setContentText("Your posts are up to date.")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
    NotificationManagerCompat.from(context).notify(1, notification)
}

/**
 * Ask for POST_NOTIFICATIONS once, from the UI (a worker has no Activity to prompt from). A no-op below
 * Android 13, where the permission is granted at install time. Call this from the screen.
 */
@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
