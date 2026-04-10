package co.id.relay.digitals.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.id.relay.digitals.data.SettingsRepository
import co.id.relay.digitals.domain.CaptureNote
import co.id.relay.digitals.domain.DayRoutineState
import co.id.relay.digitals.domain.DoneTaskEntry
import co.id.relay.digitals.domain.SessionLog
import co.id.relay.digitals.domain.TaskItem
import co.id.relay.digitals.domain.TaskType
import co.id.relay.digitals.domain.formatDoneTaskTimestamp
import co.id.relay.digitals.domain.nowEpochMs
import co.id.relay.digitals.domain.todayLocalDate
import co.id.relay.digitals.platform.Notifier
import co.id.relay.digitals.platform.rememberNotifier
import co.id.relay.digitals.theme.DeepWorkTheme
import kotlin.random.Random

private enum class MainTab(val label: String) {
    Today("Hari ini"),
    Session("Sesi"),
    Plan("Rencana"),
    More("Lainnya"),
}

private enum class SessionPhase {
    Idle,
    DeepRunning,
    DeepPaused,
    BreakRunning,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    DeepWorkTheme {
        val repository = remember { SettingsRepository() }
        val notifier = rememberNotifier()
        val snackbarHostState = remember { SnackbarHostState() }
        var insightMessage by remember { mutableStateOf<String?>(null) }
        var selectedTab by rememberSaveable { mutableStateOf(MainTab.Today) }
        LaunchedEffect(insightMessage) {
            val text = insightMessage ?: return@LaunchedEffect
            snackbarHostState.showSnackbar(text)
            insightMessage = null
        }


        var dayState by remember { mutableStateOf(repository.loadDayRoutine()) }
        val tasks = remember { mutableStateListOf<TaskItem>().apply { addAll(repository.loadTasks()) } }
        val notes = remember { mutableStateListOf<CaptureNote>().apply { addAll(repository.loadCaptureNotes()) } }
        val sessionLogs = remember { mutableStateListOf<SessionLog>().apply { addAll(repository.loadSessionLogs()) } }
        val topThree = remember {
            mutableStateListOf<String>().apply {
                val loaded = repository.loadTopThreeTomorrow()
                repeat(3) { index -> add(loaded.getOrNull(index).orEmpty()) }
            }
        }
        val doneTaskHistory = remember {
            mutableStateListOf<DoneTaskEntry>().apply { addAll(repository.loadDoneTaskHistory()) }
        }
        var deepMinutes by remember { mutableIntStateOf(repository.loadDeepMinutes()) }
        var breakMinutes by remember { mutableIntStateOf(repository.loadBreakMinutes()) }

        Scaffold(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(100))) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            },
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTab) {
                    MainTab.Today -> TodayScreen(
                        state = dayState,
                        onStateChange = {
                            dayState = it
                            repository.saveDayRoutine(it)
                        },
                    )

                    MainTab.Session -> SessionScreen(
                        notifier = notifier,
                        deepMinutes = deepMinutes,
                        breakMinutes = breakMinutes,
                        onSessionFinished = { question, conclusion ->
                            val log = SessionLog(
                                id = nowEpochMs().toString(),
                                date = todayLocalDate().toString(),
                                keyQuestion = question,
                                conclusion = conclusion,
                                deepMinutes = deepMinutes,
                                breakMinutes = breakMinutes,
                            )
                            sessionLogs.add(0, log)
                            repository.saveSessionLogs(sessionLogs.toList())
                        },
                        onInsight = { text ->
                            insightMessage = text
                        },
                    )

                    MainTab.Plan -> PlanScreen(
                        tasks = tasks,
                        topThree = topThree,
                        doneTaskHistory = doneTaskHistory.toList(),
                        onTasksUpdated = {
                            tasks.clear()
                            tasks.addAll(it)
                            repository.saveTasks(tasks.toList())
                        },
                        onTopThreeUpdated = {
                            topThree.clear()
                            topThree.addAll(it)
                            repository.saveTopThreeTomorrow(topThree.toList())
                        },
                        onArchiveCompletedTask = { task ->
                            val entry = DoneTaskEntry(
                                id = "${nowEpochMs()}_${task.id}",
                                title = task.title,
                                type = task.type,
                                completedAtEpochMs = nowEpochMs(),
                            )
                            doneTaskHistory.add(0, entry)
                            while (doneTaskHistory.size > 300) {
                                doneTaskHistory.removeAt(doneTaskHistory.lastIndex)
                            }
                            repository.saveDoneTaskHistory(doneTaskHistory.toList())
                            tasks.removeAll { it.id == task.id }
                            repository.saveTasks(tasks.toList())
                            val cleared = (0 until 3).map { i ->
                                val slot = topThree.getOrNull(i).orEmpty()
                                if (slot == task.title) "" else slot
                            }
                            topThree.clear()
                            topThree.addAll(cleared)
                            repository.saveTopThreeTomorrow(topThree.toList())
                            insightMessage = "Disimpan ke riwayat selesai"
                        },
                    )

                    MainTab.More -> MoreScreen(
                        notes = notes,
                        deepMinutes = deepMinutes,
                        breakMinutes = breakMinutes,
                        sessionLogs = sessionLogs,
                        onAddNote = { text ->
                            val note = CaptureNote(
                                id = nowEpochMs().toString(),
                                text = text,
                                createdAtEpochMs = nowEpochMs(),
                            )
                            notes.add(0, note)
                            repository.saveCaptureNotes(notes.toList())
                        },
                        onDurationsUpdated = { newDeep, newBreak ->
                            deepMinutes = newDeep
                            breakMinutes = newBreak
                            repository.saveDeepMinutes(newDeep)
                            repository.saveBreakMinutes(newBreak)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(
    state: DayRoutineState,
    onStateChange: (DayRoutineState) -> Unit,
) {
    ScrollPage("Morning Setup") {
        Text("Kurangi shallow work, maksimalkan deep work dengan sistem.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        ChecklistRow("Meja bersih (1 task aktif)", state.cleanDesk) { onStateChange(state.copy(cleanDesk = it)) }
        ChecklistRow("Noise control aktif", state.noiseControl) { onStateChange(state.copy(noiseControl = it)) }
        ChecklistRow("1 monitor = 1 task", state.oneTaskOneMonitor) { onStateChange(state.copy(oneTaskOneMonitor = it)) }
        ChecklistRow("Notifikasi HP dimatikan", state.disableNotifications) { onStateChange(state.copy(disableNotifications = it)) }
        ChecklistRow("Slack/email ditutup", state.noChatWindow) { onStateChange(state.copy(noChatWindow = it)) }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Ritual sebelum kerja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        ChecklistRow("Cuci tangan", state.ritualWashHands) { onStateChange(state.copy(ritualWashHands = it)) }
        ChecklistRow("Lap tangan", state.ritualTowel) { onStateChange(state.copy(ritualTowel = it)) }
        ChecklistRow("Tarik napas 5x", state.ritualBreath) { onStateChange(state.copy(ritualBreath = it)) }
        Spacer(Modifier.height(10.dp))
        InfoCard("Golden hours: 08:00-11:00, no meeting, no komunikasi, 1 problem utama.")
    }
}

@Composable
private fun SessionScreen(
    notifier: Notifier,
    deepMinutes: Int,
    breakMinutes: Int,
    onSessionFinished: (String, String) -> Unit,
    onInsight: (String) -> Unit,
) {
    var keyQuestion by rememberSaveable { mutableStateOf("") }
    var conclusion by rememberSaveable { mutableStateOf("") }
    var phase by rememberSaveable { mutableStateOf(SessionPhase.Idle) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(deepMinutes * 60) }
    var focusCover by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(phase, remainingSeconds) {
        if ((phase == SessionPhase.DeepRunning || phase == SessionPhase.BreakRunning) && remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                if (phase == SessionPhase.DeepRunning) {
                    notifier.showNow("Deep Work selesai", "Saatnya break ${breakMinutes} menit.")
                    phase = SessionPhase.BreakRunning
                    remainingSeconds = breakMinutes * 60
                    onInsight("Deep work selesai. Ambil break untuk jaga intensitas.")
                } else {
                    notifier.showNow("Break selesai", "Siap masuk sesi fokus berikutnya.")
                    phase = SessionPhase.Idle
                    onSessionFinished(keyQuestion, conclusion)
                    onInsight("Siklus selesai. Catat hasil lalu lanjut sesi berikutnya.")
                }
            }
        }
    }

    ScrollPage("Deep Session") {
        OutlinedTextField(
            value = keyQuestion,
            onValueChange = { keyQuestion = it },
            label = { Text("Pertanyaan utama") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = conclusion,
            onValueChange = { conclusion = it },
            label = { Text("Kesimpulan/progress") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Text(formatTimer(remainingSeconds), style = MaterialTheme.typography.displaySmall)
        Text("State: ${phase.name}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                phase = SessionPhase.DeepRunning
                remainingSeconds = deepMinutes * 60
                notifier.scheduleSessionEnd("Deep Work selesai", "Saatnya break.", deepMinutes * 60_000L)
                onInsight("Deep work dimulai. Tutup komunikasi dan fokus satu pertanyaan.")
            }) { Text("Mulai Deep") }
            OutlinedButton(onClick = { phase = SessionPhase.DeepPaused }) { Text("Pause") }
            OutlinedButton(onClick = {
                phase = SessionPhase.Idle
                remainingSeconds = deepMinutes * 60
                notifier.cancelSessionAlert()
            }) { Text("Reset") }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = focusCover, onCheckedChange = { focusCover = it })
            Spacer(Modifier.width(8.dp))
            Text("Aktifkan focus cover screen")
        }
        InfoCard("Rule: 90 menit deep + 15-20 menit break. Stop saat waktu habis.")
    }

    if (focusCover && (phase == SessionPhase.DeepRunning || phase == SessionPhase.BreakRunning)) {
        FocusCoverScreen(remainingSeconds = remainingSeconds) { focusCover = false }
    }
}

private fun normalizeTopThree(top: List<String>): List<String> =
    (0..2).map { i -> top.getOrElse(i) { "" } }

private class TopThreeDragSession {
    var buffer: MutableList<String>? = null
    var dragFromIndex: Int? = null
    var accumulatedY = 0f
}

@Composable
private fun PlanScreen(
    tasks: List<TaskItem>,
    topThree: List<String>,
    doneTaskHistory: List<DoneTaskEntry>,
    onTasksUpdated: (List<TaskItem>) -> Unit,
    onTopThreeUpdated: (List<String>) -> Unit,
    onArchiveCompletedTask: (TaskItem) -> Unit,
) {
    var newTaskTitle by rememberSaveable { mutableStateOf("") }
    var deepTask by rememberSaveable { mutableStateOf(true) }
    val taskLimitReached = tasks.size >= 5
    val deepCount = tasks.count { it.type == TaskType.Deep }
    val shallowCount = tasks.count { it.type == TaskType.Shallow }
    val estimatedFocus = deepCount * 90
    val bufferPercent = (100 - ((estimatedFocus / 480f) * 100f)).toInt().coerceIn(0, 100)

    var localTopThree by remember { mutableStateOf(normalizeTopThree(topThree)) }
    LaunchedEffect(topThree) {
        localTopThree = normalizeTopThree(topThree)
    }
    fun commitTopThree(value: List<String>) {
        val n = normalizeTopThree(value)
        localTopThree = n
        onTopThreeUpdated(n)
    }

    ScrollPage("Planning") {
        Text("Maksimal 3-5 task utama per hari", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                label = { Text("Task baru") },
                modifier = Modifier.weight(1f),
                enabled = !taskLimitReached,
            )
            Spacer(Modifier.width(8.dp))
            Text(if (deepTask) "Deep" else "Shallow", modifier = Modifier.clickable { deepTask = !deepTask })
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (newTaskTitle.isNotBlank()) {
                    onTasksUpdated(
                        tasks + TaskItem(
                            id = nowEpochMs().toString(),
                            title = newTaskTitle.trim(),
                            type = if (deepTask) TaskType.Deep else TaskType.Shallow,
                        ),
                    )
                    newTaskTitle = ""
                }
            },
            enabled = !taskLimitReached && newTaskTitle.isNotBlank(),
        ) { Text("Tambah task") }

        Spacer(Modifier.height(12.dp))
        tasks.forEach { item ->
            PlanTaskRow(
                label = "${item.title} (${item.type.name})",
                checked = item.done,
                onCheckedChange = { checked ->
                    onTasksUpdated(tasks.map { if (it.id == item.id) it.copy(done = checked) else it })
                },
                onDelete = {
                    onTasksUpdated(tasks.filter { it.id != item.id })
                    val cleared = (0 until 3).map { i ->
                        val slot = localTopThree.getOrElse(i) { "" }
                        if (slot == item.title) "" else slot
                    }
                    commitTopThree(cleared)
                },
                onAssignToSlot = { slot ->
                    val next = localTopThree.toMutableList()
                    next[slot] = item.title
                    commitTopThree(next)
                },
                onMarkDoneArchive = { onArchiveCompletedTask(item) },
            )
        }
        Spacer(Modifier.height(12.dp))
        InfoCard("Deep: $deepCount | Shallow: $shallowCount | Buffer tersisa: ~$bufferPercent%")
        Spacer(Modifier.height(10.dp))
        Text("Top 3 besok", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                val pool = tasks.filter { !it.done }.ifEmpty { tasks }
                val picked = pool.shuffled(Random.Default).take(3).map { it.title }
                commitTopThree(
                    listOf(
                        picked.getOrElse(0) { "" },
                        picked.getOrElse(1) { "" },
                        picked.getOrElse(2) { "" },
                    ),
                )
            },
            enabled = tasks.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Acak 3 prioritas dari daftar task") }
        Spacer(Modifier.height(4.dp))
        TopThreeReorderableSlots(
            slots = localTopThree,
            onSlotsChange = { commitTopThree(it) },
        )
        Spacer(Modifier.height(16.dp))
        Text("Riwayat selesai", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        if (doneTaskHistory.isEmpty()) {
            Text(
                "Belum ada riwayat. Ketuk Selesai pada task untuk mengarsip (checkbox tetap untuk tandai progres).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            doneTaskHistory.forEachIndexed { index, entry ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                DoneHistoryRow(entry = entry)
            }
        }
    }
}

@Composable
private fun TopThreeReorderableSlots(
    slots: List<String>,
    onSlotsChange: (List<String>) -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 52.dp.toPx() }
    val slotsLatest by rememberUpdatedState(slots)
    val dragSession = remember { TopThreeDragSession() }

    for (idx in 0 until 3) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .pointerInput(idx, thresholdPx) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                dragSession.buffer = normalizeTopThree(slotsLatest).toMutableList()
                                dragSession.dragFromIndex = idx
                                dragSession.accumulatedY = 0f
                            },
                            onDrag = { _, dragAmount ->
                                val buf = dragSession.buffer ?: return@detectDragGesturesAfterLongPress
                                var di = dragSession.dragFromIndex ?: return@detectDragGesturesAfterLongPress
                                dragSession.accumulatedY += dragAmount.y
                                val th = thresholdPx
                                while (dragSession.accumulatedY > th && di < 2) {
                                    val t = buf[di]
                                    buf[di] = buf[di + 1]
                                    buf[di + 1] = t
                                    onSlotsChange(buf.toList())
                                    di += 1
                                    dragSession.dragFromIndex = di
                                    dragSession.accumulatedY -= th
                                }
                                while (dragSession.accumulatedY < -th && di > 0) {
                                    val t = buf[di]
                                    buf[di] = buf[di - 1]
                                    buf[di - 1] = t
                                    onSlotsChange(buf.toList())
                                    di -= 1
                                    dragSession.dragFromIndex = di
                                    dragSession.accumulatedY += th
                                }
                            },
                            onDragEnd = {
                                dragSession.buffer = null
                                dragSession.dragFromIndex = null
                                dragSession.accumulatedY = 0f
                            },
                            onDragCancel = {
                                dragSession.buffer = null
                                dragSession.dragFromIndex = null
                                dragSession.accumulatedY = 0f
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "⋮⋮",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            OutlinedTextField(
                value = slots.getOrElse(idx) { "" },
                onValueChange = { value ->
                    val next = normalizeTopThree(slots).toMutableList()
                    next[idx] = value
                    onSlotsChange(next)
                },
                label = { Text("Prioritas ${idx + 1}") },
                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun MoreScreen(
    notes: List<CaptureNote>,
    deepMinutes: Int,
    breakMinutes: Int,
    sessionLogs: List<SessionLog>,
    onAddNote: (String) -> Unit,
    onDurationsUpdated: (Int, Int) -> Unit,
) {
    var noteInput by rememberSaveable { mutableStateOf("") }
    var silentSeconds by rememberSaveable { mutableIntStateOf(0) }
    var deepInput by rememberSaveable { mutableIntStateOf(deepMinutes) }
    var breakInput by rememberSaveable { mutableIntStateOf(breakMinutes) }

    LaunchedEffect(silentSeconds) {
        if (silentSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            silentSeconds -= 1
        }
    }

    ScrollPage("Tangkap & Pengaturan") {
        Text("Save thoughts for later", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("Catatan cepat") },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (noteInput.isNotBlank()) {
                    onAddNote(noteInput.trim())
                    noteInput = ""
                }
            }) { Text("Simpan") }
        }
        notes.take(5).forEach { Text("- ${it.text}") }
        Spacer(Modifier.height(12.dp))
        Text("Bored silence 5-10 menit", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { silentSeconds = 5 * 60 }) { Text("5 menit") }
            OutlinedButton(onClick = { silentSeconds = 10 * 60 }) { Text("10 menit") }
        }
        Text("Sisa: ${formatTimer(silentSeconds)}")
        Spacer(Modifier.height(12.dp))
        Text("Durasi sesi", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = deepInput.toString(),
                onValueChange = { deepInput = it.toIntOrNull() ?: deepInput },
                label = { Text("Deep (menit)") },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = breakInput.toString(),
                onValueChange = { breakInput = it.toIntOrNull() ?: breakInput },
                label = { Text("Break (menit)") },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onDurationsUpdated(deepInput, breakInput) }) { Text("Simpan durasi") }
        Spacer(Modifier.height(12.dp))
        Text("Log sesi terakhir", style = MaterialTheme.typography.titleMedium)
        sessionLogs.take(3).forEach { log ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(10.dp)) {
                    Text(log.date, fontWeight = FontWeight.SemiBold)
                    Text("Q: ${log.keyQuestion.ifBlank { "-" }}")
                    Text("Hasil: ${log.conclusion.ifBlank { "-" }}")
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun FocusCoverScreen(
    remainingSeconds: Int,
    onExit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("FOCUS MODE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                formatTimer(remainingSeconds),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text("No chat, no scroll, one problem only.")
        }
        OutlinedButton(onClick = onExit, modifier = Modifier.align(Alignment.BottomCenter)) {
            Text("Keluar cover")
        }
    }
}

@Composable
private fun ScrollPage(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            content()
            Spacer(Modifier.height(20.dp))
        },
    )
}

@Composable
private fun ChecklistRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text)
    }
}

@Composable
private fun PlanTaskRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onAssignToSlot: (Int) -> Unit,
    onMarkDoneArchive: () -> Unit,
) {
    var prioritasOpen by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(label, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                TextButton(onClick = { prioritasOpen = true }) { Text("Prioritas") }
                DropdownMenu(
                    expanded = prioritasOpen,
                    onDismissRequest = { prioritasOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Prioritas 1") },
                        onClick = {
                            onAssignToSlot(0)
                            prioritasOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Prioritas 2") },
                        onClick = {
                            onAssignToSlot(1)
                            prioritasOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Prioritas 3") },
                        onClick = {
                            onAssignToSlot(2)
                            prioritasOpen = false
                        },
                    )
                }
            }
            TextButton(onClick = onMarkDoneArchive) { Text("Selesai") }
            TextButton(onClick = onDelete) { Text("Hapus") }
        }
    }
}

@Composable
private fun DoneHistoryRow(entry: DoneTaskEntry) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(entry.title, style = MaterialTheme.typography.bodyLarge)
        Text(
            "${entry.type.name} · ${formatDoneTaskTimestamp(entry.completedAtEpochMs)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(text, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatTimer(seconds: Int): String {
    val mins = (seconds / 60).coerceAtLeast(0)
    val secs = (seconds % 60).coerceAtLeast(0)
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
