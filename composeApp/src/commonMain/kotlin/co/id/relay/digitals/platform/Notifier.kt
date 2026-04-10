package co.id.relay.digitals.platform

import androidx.compose.runtime.Composable

interface Notifier {
    fun scheduleSessionEnd(title: String, message: String, delayMs: Long)
    fun cancelSessionAlert()
    fun showNow(title: String, message: String)
}

@Composable
expect fun rememberNotifier(): Notifier
