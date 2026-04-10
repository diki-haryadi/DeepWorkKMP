package co.id.relay.digitals.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.TrayIcon
import java.util.Timer
import kotlin.concurrent.schedule

private class JvmNotifier : Notifier {
    private var timer: Timer? = null

    override fun scheduleSessionEnd(title: String, message: String, delayMs: Long) {
        cancelSessionAlert()
        timer = Timer("deep-work-notifier", false).also { t ->
            t.schedule(delayMs.coerceAtLeast(0L)) {
                showNow(title, message)
            }
        }
    }

    override fun cancelSessionAlert() {
        timer?.cancel()
        timer = null
    }

    override fun showNow(title: String, message: String) {
        runCatching {
            val icon = TrayIcon(java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB))
            icon.displayMessage(title, message, TrayIcon.MessageType.INFO)
        }.onFailure {
            println("DeepWork alert: $title - $message")
        }
    }
}

@Composable
actual fun rememberNotifier(): Notifier = remember { JvmNotifier() }
