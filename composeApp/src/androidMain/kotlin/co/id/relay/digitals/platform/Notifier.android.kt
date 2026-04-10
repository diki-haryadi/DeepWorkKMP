package co.id.relay.digitals.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import co.id.relay.digitals.R

private const val CHANNEL_ID = "deep_work_channel"
private const val NOTIFICATION_ID = 101

private class AndroidNotifier(private val context: Context) : Notifier {
    private val handler = Handler(Looper.getMainLooper())
    private var scheduledRunnable: Runnable? = null

    init {
        ensureChannel(context)
    }

    override fun scheduleSessionEnd(title: String, message: String, delayMs: Long) {
        cancelSessionAlert()
        val runnable = Runnable { showNow(title, message) }
        scheduledRunnable = runnable
        handler.postDelayed(runnable, delayMs)
    }

    override fun cancelSessionAlert() {
        scheduledRunnable?.let(handler::removeCallbacks)
        scheduledRunnable = null
    }

    override fun showNow(title: String, message: String) {
        if (!canNotify(context)) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

@Composable
actual fun rememberNotifier(): Notifier {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidNotifier(context) }
}

private fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Deep Work Alerts",
        NotificationManager.IMPORTANCE_DEFAULT,
    )
    manager.createNotificationChannel(channel)
}

private fun canNotify(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}
