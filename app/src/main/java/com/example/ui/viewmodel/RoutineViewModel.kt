package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RoutineCategory
import com.example.data.RoutinePreset
import com.example.data.ScheduleItem
import com.example.service.AiRoutineAnalyzer
import com.example.service.DailyAnalysisResult
import com.example.service.FocusModeManager
import com.example.service.NotificationHelper
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TodoFilterTab(val title: String) {
    ALL("All Tasks"),
    PENDING("To-Do"),
    COMPLETED("Done")
}

data class RoutineUiState(
    val timeFormatted: String = "00:00:00",
    val dateFormatted: String = "",
    val dayOfWeek: String = "",
    val currentMinutesOfDay: Int = 0,
    val isSimulatedMode: Boolean = false,
    val simulatedMinutesOfDay: Int = 0,
    val schedule: List<ScheduleItem> = ScheduleItem.DEFAULT_SCHEDULE,
    val activeItem: ScheduleItem? = null,
    val nextItem: ScheduleItem? = null,
    val isNotificationsEnabled: Boolean = true,
    val isAlarmMuted: Boolean = false,
    val completedTaskIds: Set<Int> = emptySet(),
    val selectedCategory: RoutineCategory? = null,
    val todoFilter: TodoFilterTab = TodoFilterTab.ALL,
    val lastAlertMessage: String? = null,
    // Alarm ringing state & off/snooze controls
    val isAlarmRinging: Boolean = false,
    val ringingItem: ScheduleItem? = null,
    // Focus Mode (DND)
    val isFocusModeActive: Boolean = false,
    val focusMinutesRemaining: Int = 45,
    val hasDndPermission: Boolean = false,
    // AI Daily Analysis
    val isAiAnalyzing: Boolean = false,
    val aiAnalysisResult: DailyAnalysisResult? = null,
    val showAiDialog: Boolean = false,
    // Theme & Settings Dialog
    val themeMode: AppThemeMode = AppThemeMode.DARK_CYBER,
    val showThemeDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    // Navigation Tab (0: Routine, 1: Analytics Dashboard, 2: Focus Shield, 3: Stopwatch)
    val currentTab: Int = 0,
    // User Routine Customization & Presets
    val currentPreset: RoutinePreset = RoutinePreset.DEVELOPER,
    val showAddActivityDialog: Boolean = false,
    val showExportImportDialog: Boolean = false,
    // Advanced Settings
    val is24HourFormat: Boolean = false,
    val soundToneName: String = "Double Beep",
    val isVibrationEnabled: Boolean = true,
    val preTaskReminderMinutes: Int = 0, // 0 = disabled, 5 = 5 min before
    val userName: String = "My Daily Routine",
    val isCompactView: Boolean = false,
    val alarmAutoDismissSeconds: Int = 60,
    val autoResetDaily: Boolean = true,
    // Student Life & Academic Features
    val studentStudyTargetHours: Int = 6,
    val studentAttendanceTargetPercent: Int = 75,
    val isExamModeActive: Boolean = false,
    val isLectureSilentModeEnabled: Boolean = true,
    val pomodoroStudyMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val isAssignmentAlertEnabled: Boolean = true
)

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationHelper = NotificationHelper(application)
    private val focusModeManager = FocusModeManager(application)
    private val aiAnalyzer = AiRoutineAnalyzer()

    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

    private var lastTriggeredMinute: Int = -1

    init {
        checkDndPermission()
        updateTimeTicker()
        startClockLoop()
    }

    fun checkDndPermission() {
        val granted = focusModeManager.hasDndPermission()
        _uiState.update { it.copy(hasDndPermission = granted) }
    }

    fun getDndSettingsIntent() = focusModeManager.getDndSettingsIntent()

    private fun startClockLoop() {
        viewModelScope.launch {
            while (isActive) {
                updateTimeTicker()
                delay(1000)
            }
        }
    }

    private fun updateTimeTicker() {
        val now = Calendar.getInstance()
        val realHour = now.get(Calendar.HOUR_OF_DAY)
        val realMin = now.get(Calendar.MINUTE)
        val realSec = now.get(Calendar.SECOND)
        val realMinutesOfDay = realHour * 60 + realMin

        val timePattern = if (_uiState.value.is24HourFormat) "HH:mm:ss" else "hh:mm:ss a"
        val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val currentDate = Date()

        _uiState.update { current ->
            val effectiveMinutes = if (current.isSimulatedMode) {
                current.simulatedMinutesOfDay
            } else {
                realMinutesOfDay
            }

            val active = current.schedule.find { it.isActive(effectiveMinutes) }
            val next = findNextItem(current.schedule, effectiveMinutes)

            // Trigger alarm on start of routine minute
            if (!current.isSimulatedMode && realSec <= 2 && realMinutesOfDay != lastTriggeredMinute) {
                active?.let { activeItem ->
                    if (activeItem.getStartMinutes() == realMinutesOfDay) {
                        lastTriggeredMinute = realMinutesOfDay
                        triggerAlarmAndNotification(activeItem)
                    }
                }
            }

            // Decrement Focus mode minutes if active
            val updatedFocusRemaining = if (current.isFocusModeActive && realSec == 0 && current.focusMinutesRemaining > 0) {
                current.focusMinutesRemaining - 1
            } else {
                current.focusMinutesRemaining
            }

            val shouldAutoTurnOffFocus = current.isFocusModeActive && updatedFocusRemaining <= 0
            if (shouldAutoTurnOffFocus) {
                focusModeManager.setFocusModeDnd(false)
            }

            current.copy(
                timeFormatted = timeFormat.format(currentDate),
                dateFormatted = dateFormat.format(currentDate),
                dayOfWeek = dayFormat.format(currentDate),
                currentMinutesOfDay = effectiveMinutes,
                activeItem = active,
                nextItem = next,
                focusMinutesRemaining = updatedFocusRemaining,
                isFocusModeActive = if (shouldAutoTurnOffFocus) false else current.isFocusModeActive
            )
        }
    }

    private fun findNextItem(schedule: List<ScheduleItem>, currentMinutes: Int): ScheduleItem? {
        val sorted = schedule.sortedBy { it.getStartMinutes() }
        val nextToday = sorted.firstOrNull { it.getStartMinutes() > currentMinutes }
        return nextToday ?: sorted.firstOrNull()
    }

    private fun triggerAlarmAndNotification(item: ScheduleItem) {
        val state = _uiState.value
        if (state.isNotificationsEnabled) {
            notificationHelper.sendRoutineNotification(
                activityName = item.activity,
                description = item.desc,
                notificationId = item.id
            )
        }
        if (!state.isAlarmMuted) {
            notificationHelper.playAlarmBeep(
                pulsing = true,
                toneName = state.soundToneName,
                vibrationEnabled = state.isVibrationEnabled
            )
        }
        _uiState.update {
            it.copy(
                isAlarmRinging = true,
                ringingItem = item,
                lastAlertMessage = "⏰ Alarm: ${item.activity} (${item.start})"
            )
        }
    }

    fun turnOffAlarm() {
        NotificationHelper.stopActiveAlarmSound()
        _uiState.update {
            it.copy(
                isAlarmRinging = false,
                lastAlertMessage = "Alarm turned off"
            )
        }
    }

    fun snoozeAlarm(minutes: Int = 5) {
        val item = _uiState.value.ringingItem ?: _uiState.value.activeItem
        NotificationHelper.stopActiveAlarmSound()
        notificationHelper.scheduleSnooze(item?.activity ?: "Routine", minutes)
        _uiState.update {
            it.copy(
                isAlarmRinging = false,
                lastAlertMessage = "Alarm snoozed for $minutes minutes"
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationsEnabled = enabled) }
    }

    fun toggleAlarmMute() {
        _uiState.update { it.copy(isAlarmMuted = !it.isAlarmMuted) }
    }

    fun testAlarmAndNotification() {
        val currentActive = _uiState.value.activeItem ?: _uiState.value.schedule.firstOrNull()
        val activityTitle = currentActive?.activity ?: "Practice Block 1"
        val desc = currentActive?.desc ?: "Test Alarm & Lock-screen Notification"

        if (_uiState.value.isNotificationsEnabled) {
            notificationHelper.sendRoutineNotification(
                activityName = activityTitle,
                description = desc,
                notificationId = 999
            )
        }
        if (!_uiState.value.isAlarmMuted) {
            notificationHelper.playAlarmBeep(
                pulsing = true,
                toneName = _uiState.value.soundToneName,
                vibrationEnabled = _uiState.value.isVibrationEnabled
            )
        }
        _uiState.update {
            it.copy(
                isAlarmRinging = true,
                ringingItem = currentActive,
                lastAlertMessage = "Alarm triggered! Tap 'Off' to dismiss."
            )
        }
    }

    fun toggleFocusMode(enabled: Boolean) {
        val hasPerm = focusModeManager.hasDndPermission()
        if (enabled && hasPerm) {
            focusModeManager.setFocusModeDnd(true)
        } else if (!enabled) {
            focusModeManager.setFocusModeDnd(false)
        }
        _uiState.update {
            it.copy(
                isFocusModeActive = enabled,
                hasDndPermission = hasPerm,
                lastAlertMessage = if (enabled) "Focus Shield ON: Notifications silenced." else "Focus Mode ended."
            )
        }
    }

    fun setFocusDurationMinutes(minutes: Int) {
        _uiState.update { it.copy(focusMinutesRemaining = minutes) }
    }

    fun requestAiDailyAnalysis() {
        _uiState.update { it.copy(isAiAnalyzing = true, showAiDialog = true) }
        viewModelScope.launch {
            val state = _uiState.value
            val result = aiAnalyzer.analyzeDailyRoutine(
                schedule = state.schedule,
                completedIds = state.completedTaskIds,
                activeItem = state.activeItem,
                currentTimeStr = state.timeFormatted
            )
            _uiState.update {
                it.copy(
                    isAiAnalyzing = false,
                    aiAnalysisResult = result
                )
            }
        }
    }

    fun dismissAiDialog() {
        _uiState.update { it.copy(showAiDialog = false) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode, showThemeDialog = false) }
    }

    fun setShowThemeDialog(show: Boolean) {
        _uiState.update { it.copy(showThemeDialog = show) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
    }

    fun toggleTaskCompleted(id: Int) {
        _uiState.update { state ->
            val updated = if (state.completedTaskIds.contains(id)) {
                state.completedTaskIds - id
            } else {
                state.completedTaskIds + id
            }
            state.copy(completedTaskIds = updated)
        }
    }

    fun setSimulationMode(enabled: Boolean, simulatedMinutes: Int = 420) {
        _uiState.update {
            it.copy(
                isSimulatedMode = enabled,
                simulatedMinutesOfDay = if (enabled) simulatedMinutes else 0
            )
        }
        updateTimeTicker()
    }

    fun setSimulatedMinutes(minutes: Int) {
        _uiState.update {
            it.copy(
                isSimulatedMode = true,
                simulatedMinutesOfDay = minutes.coerceIn(0, 1439)
            )
        }
        updateTimeTicker()
    }

    fun selectCategory(category: RoutineCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateScheduleItem(updated: ScheduleItem) {
        _uiState.update { state ->
            val updatedList = state.schedule.map { if (it.id == updated.id) updated else it }
            state.copy(schedule = updatedList)
        }
        updateTimeTicker()
    }

    fun addActivity(item: ScheduleItem) {
        _uiState.update { state ->
            val updatedList = (state.schedule + item).sortedBy { it.getStartMinutes() }
            state.copy(
                schedule = updatedList,
                showAddActivityDialog = false,
                lastAlertMessage = "Added '${item.activity}' to routine"
            )
        }
        updateTimeTicker()
    }

    fun deleteActivity(id: Int) {
        _uiState.update { state ->
            val updatedList = state.schedule.filter { it.id != id }
            state.copy(
                schedule = updatedList,
                completedTaskIds = state.completedTaskIds - id,
                lastAlertMessage = "Activity removed"
            )
        }
        updateTimeTicker()
    }

    fun loadPreset(preset: RoutinePreset) {
        val newSchedule = ScheduleItem.getScheduleForPreset(preset)
        _uiState.update { state ->
            state.copy(
                schedule = newSchedule,
                currentPreset = preset,
                completedTaskIds = emptySet(),
                lastAlertMessage = "Loaded preset: ${preset.displayName}"
            )
        }
        updateTimeTicker()
    }

    fun exportScheduleJson(): String {
        return ScheduleItem.exportToJsonString(_uiState.value.schedule)
    }

    fun importScheduleJson(jsonString: String): Boolean {
        val imported = ScheduleItem.importFromJsonString(jsonString)
        return if (imported != null) {
            _uiState.update { state ->
                state.copy(
                    schedule = imported.sortedBy { it.getStartMinutes() },
                    currentPreset = RoutinePreset.CUSTOM,
                    completedTaskIds = emptySet(),
                    showExportImportDialog = false,
                    lastAlertMessage = "Successfully imported ${imported.size} routine activities!"
                )
            }
            updateTimeTicker()
            true
        } else {
            false
        }
    }

    fun toggleTimeFormat() {
        _uiState.update { it.copy(is24HourFormat = !it.is24HourFormat) }
        updateTimeTicker()
    }

    fun setSoundTone(name: String) {
        _uiState.update { it.copy(soundToneName = name) }
    }

    fun toggleVibration() {
        _uiState.update { it.copy(isVibrationEnabled = !it.isVibrationEnabled) }
    }

    fun setPreTaskReminder(minutes: Int) {
        _uiState.update { it.copy(preTaskReminderMinutes = minutes) }
    }

    fun setShowAddActivityDialog(show: Boolean) {
        _uiState.update { it.copy(showAddActivityDialog = show) }
    }

    fun setShowExportImportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportImportDialog = show) }
    }

    fun updateNotes(itemId: Int, notes: String) {
        _uiState.update { state ->
            val updatedList = state.schedule.map {
                if (it.id == itemId) it.copy(notes = notes) else it
            }
            state.copy(
                schedule = updatedList,
                lastAlertMessage = "Notes saved"
            )
        }
        updateTimeTicker()
    }

    fun setShowSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun resetToDefaults() {
        _uiState.update {
            it.copy(
                schedule = ScheduleItem.DEFAULT_SCHEDULE,
                currentPreset = RoutinePreset.DEVELOPER,
                completedTaskIds = emptySet(),
                lastAlertMessage = "Routine restored to defaults"
            )
        }
        updateTimeTicker()
    }

    fun previewSoundTone(toneName: String) {
        setSoundTone(toneName)
        notificationHelper.previewSampleTone(toneName, _uiState.value.isVibrationEnabled)
    }

    fun setUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun toggleCompactView() {
        _uiState.update { it.copy(isCompactView = !it.isCompactView) }
    }

    fun setAlarmAutoDismissSeconds(seconds: Int) {
        _uiState.update { it.copy(alarmAutoDismissSeconds = seconds) }
    }

    fun toggleAutoResetDaily() {
        _uiState.update { it.copy(autoResetDaily = !it.autoResetDaily) }
    }

    fun clearTodayCompleted() {
        _uiState.update {
            it.copy(
                completedTaskIds = emptySet(),
                lastAlertMessage = "Cleared completed checks for today"
            )
        }
    }

    fun clearAllNotes() {
        _uiState.update { state ->
            val updated = state.schedule.map { it.copy(notes = "") }
            state.copy(
                schedule = updated,
                lastAlertMessage = "All activity notes cleared"
            )
        }
        updateTimeTicker()
    }

    fun setTodoFilter(filter: TodoFilterTab) {
        _uiState.update { it.copy(todoFilter = filter) }
    }

    fun toggleSubTask(itemId: Int, subTaskId: String) {
        _uiState.update { state ->
            val updated = state.schedule.map { item ->
                if (item.id == itemId) {
                    val newChecklist = item.checklist.map { sub ->
                        if (sub.id == subTaskId) sub.copy(isDone = !sub.isDone) else sub
                    }
                    item.copy(checklist = newChecklist)
                } else item
            }
            state.copy(schedule = updated)
        }
    }

    fun addSubTask(itemId: Int, text: String) {
        if (text.isBlank()) return
        _uiState.update { state ->
            val updated = state.schedule.map { item ->
                if (item.id == itemId) {
                    val newItem = com.example.data.TodoCheckItem(text = text.trim())
                    item.copy(checklist = item.checklist + newItem)
                } else item
            }
            state.copy(
                schedule = updated,
                lastAlertMessage = "Added sub-task: '${text.trim()}'"
            )
        }
    }

    fun deleteSubTask(itemId: Int, subTaskId: String) {
        _uiState.update { state ->
            val updated = state.schedule.map { item ->
                if (item.id == itemId) {
                    item.copy(checklist = item.checklist.filter { it.id != subTaskId })
                } else item
            }
            state.copy(schedule = updated)
        }
    }

    // Student Life & Academic Settings Methods
    fun setStudentStudyTargetHours(hours: Int) {
        _uiState.update { it.copy(studentStudyTargetHours = hours, lastAlertMessage = "Daily Study Target set to ${hours}h") }
    }

    fun setStudentAttendanceTargetPercent(percent: Int) {
        _uiState.update { it.copy(studentAttendanceTargetPercent = percent, lastAlertMessage = "Attendance Target set to $percent%") }
    }

    fun toggleExamMode() {
        _uiState.update {
            val nextState = !it.isExamModeActive
            it.copy(
                isExamModeActive = nextState,
                lastAlertMessage = if (nextState) "Exam Prep Mode Active: Revision & Study prioritized!" else "Exam Prep Mode turned off"
            )
        }
    }

    fun toggleLectureSilentMode() {
        _uiState.update {
            val next = !it.isLectureSilentModeEnabled
            it.copy(
                isLectureSilentModeEnabled = next,
                lastAlertMessage = if (next) "Lecture Silent Guard Enabled (Lectures auto-muted)" else "Lecture Silent Guard Disabled"
            )
        }
    }

    fun setPomodoroInterval(studyMin: Int, breakMin: Int) {
        _uiState.update {
            it.copy(
                pomodoroStudyMinutes = studyMin,
                pomodoroBreakMinutes = breakMin,
                lastAlertMessage = "Pomodoro set to ${studyMin}m study / ${breakMin}m break"
            )
        }
    }

    fun toggleAssignmentAlert() {
        _uiState.update { it.copy(isAssignmentAlertEnabled = !it.isAssignmentAlertEnabled) }
    }

    fun dismissLastAlert() {
        _uiState.update { it.copy(lastAlertMessage = null) }
    }
}
