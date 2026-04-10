package co.id.relay.digitals.data

import co.id.relay.digitals.domain.CaptureNote
import co.id.relay.digitals.domain.DayRoutineState
import co.id.relay.digitals.domain.DoneTaskEntry
import co.id.relay.digitals.domain.SessionLog
import co.id.relay.digitals.domain.TaskItem
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepository(
    private val settings: Settings = Settings(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadDayRoutine(): DayRoutineState = decodeOrDefault(KEY_DAY_ROUTINE, DayRoutineState())
    fun saveDayRoutine(state: DayRoutineState) = settings.putString(KEY_DAY_ROUTINE, json.encodeToString(state))

    fun loadTasks(): List<TaskItem> = decodeOrDefault(KEY_TASKS, emptyList())
    fun saveTasks(items: List<TaskItem>) = settings.putString(KEY_TASKS, json.encodeToString(items))

    fun loadDoneTaskHistory(): List<DoneTaskEntry> = decodeOrDefault(KEY_DONE_TASK_HISTORY, emptyList())
    fun saveDoneTaskHistory(items: List<DoneTaskEntry>) {
        val capped = items.take(DONE_HISTORY_MAX)
        settings.putString(KEY_DONE_TASK_HISTORY, json.encodeToString(capped))
    }

    fun loadCaptureNotes(): List<CaptureNote> = decodeOrDefault(KEY_CAPTURE_NOTES, emptyList())
    fun saveCaptureNotes(notes: List<CaptureNote>) = settings.putString(KEY_CAPTURE_NOTES, json.encodeToString(notes))

    fun loadSessionLogs(): List<SessionLog> = decodeOrDefault(KEY_SESSION_LOGS, emptyList())
    fun saveSessionLogs(logs: List<SessionLog>) = settings.putString(KEY_SESSION_LOGS, json.encodeToString(logs))

    fun loadTopThreeTomorrow(): List<String> = decodeOrDefault(KEY_TOP_THREE, emptyList())
    fun saveTopThreeTomorrow(values: List<String>) = settings.putString(KEY_TOP_THREE, json.encodeToString(values))

    fun loadDeepMinutes(): Int = settings.getInt(KEY_DEEP_MINUTES, 90)
    fun saveDeepMinutes(value: Int) = settings.putInt(KEY_DEEP_MINUTES, value.coerceIn(25, 180))

    fun loadBreakMinutes(): Int = settings.getInt(KEY_BREAK_MINUTES, 20)
    fun saveBreakMinutes(value: Int) = settings.putInt(KEY_BREAK_MINUTES, value.coerceIn(5, 45))

    @Suppress("TooGenericExceptionCaught")
    private inline fun <reified T> decodeOrDefault(key: String, fallback: T): T {
        val raw = settings.getStringOrNull(key) ?: return fallback
        return try {
            json.decodeFromString<T>(raw)
        } catch (_: Exception) {
            fallback
        }
    }

    private companion object {
        const val KEY_DAY_ROUTINE = "day_routine"
        const val KEY_TASKS = "tasks"
        const val KEY_DONE_TASK_HISTORY = "done_task_history"
        const val DONE_HISTORY_MAX = 300
        const val KEY_CAPTURE_NOTES = "capture_notes"
        const val KEY_SESSION_LOGS = "session_logs"
        const val KEY_TOP_THREE = "top_three_tomorrow"
        const val KEY_DEEP_MINUTES = "deep_minutes"
        const val KEY_BREAK_MINUTES = "break_minutes"
    }
}
