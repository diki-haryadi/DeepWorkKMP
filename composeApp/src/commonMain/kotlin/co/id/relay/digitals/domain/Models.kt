package co.id.relay.digitals.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class TaskItem(
    val id: String,
    val title: String,
    val type: TaskType,
    val done: Boolean = false,
)

@Serializable
enum class TaskType {
    Deep,
    Shallow,
}

@Serializable
data class CaptureNote(
    val id: String,
    val text: String,
    val createdAtEpochMs: Long,
)

@Serializable
data class SessionLog(
    val id: String,
    val date: String,
    val keyQuestion: String,
    val conclusion: String,
    val deepMinutes: Int,
    val breakMinutes: Int,
)

@Serializable
data class DayRoutineState(
    val cleanDesk: Boolean = false,
    val noiseControl: Boolean = false,
    val oneTaskOneMonitor: Boolean = false,
    val disableNotifications: Boolean = false,
    val noChatWindow: Boolean = false,
    val ritualWashHands: Boolean = false,
    val ritualTowel: Boolean = false,
    val ritualBreath: Boolean = false,
)

fun todayLocalDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun nowEpochMs(): Long = Clock.System.now().toEpochMilliseconds()
