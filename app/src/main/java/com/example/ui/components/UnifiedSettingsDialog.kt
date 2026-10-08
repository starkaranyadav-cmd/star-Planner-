package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.RoutinePreset
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

/**
 * Settings Sections for Strict Click-To-Show / Hide Accordion
 */
enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val optionsCount: String
) {
    ROUTINE("Routing", "Timetable, presets & persona", Icons.Default.Widgets, EmeraldAccent, "6 options"),
    MODE("Mode", "Dark/Light & color palettes", Icons.Default.Palette, PurpleAccent, "5 themes"),
    ALERTS("Alerte", "Alarms, tones & vibration", Icons.Default.NotificationsActive, AmberAccent, "6 options"),
    STUDENT("Student Life", "Goals, attendance & exam prep", Icons.Default.School, EmeraldAccent, "6 features"),
    MAINTENANCE("Maintenance", "Reset checks, notes & schedule", Icons.Default.Tune, RoseAccent, "3 tools")
}

@Composable
fun UnifiedSettingsDialog(
    themeMode: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    currentPreset: RoutinePreset,
    onSelectPreset: (RoutinePreset) -> Unit,
    is24HourFormat: Boolean,
    onToggleTimeFormat: () -> Unit,
    preTaskReminderMinutes: Int,
    onSetPreTaskReminder: (Int) -> Unit,
    isVibrationEnabled: Boolean,
    onToggleVibration: () -> Unit,
    soundToneName: String,
    onSelectSoundTone: (String) -> Unit,
    isNotificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    isAlarmMuted: Boolean,
    onToggleAlarmMute: () -> Unit,
    onTestAlarm: () -> Unit,
    onTriggerAiAnalyze: () -> Unit,
    onOpenExportImport: () -> Unit,
    onResetSchedule: () -> Unit,
    userName: String = "My Daily Routine",
    onUpdateUserName: (String) -> Unit = {},
    isCompactView: Boolean = false,
    onToggleCompactView: () -> Unit = {},
    alarmAutoDismissSeconds: Int = 60,
    onSelectAlarmAutoDismissSeconds: (Int) -> Unit = {},
    onPreviewTone: (String) -> Unit = {},
    onClearTodayCompleted: () -> Unit = {},
    onClearAllNotes: () -> Unit = {},
    // Student Life Features
    studentStudyTargetHours: Int = 6,
    onSelectStudyTargetHours: (Int) -> Unit = {},
    studentAttendanceTargetPercent: Int = 75,
    onSelectAttendanceTargetPercent: (Int) -> Unit = {},
    isExamModeActive: Boolean = false,
    onToggleExamMode: () -> Unit = {},
    isLectureSilentModeEnabled: Boolean = true,
    onToggleLectureSilentMode: () -> Unit = {},
    pomodoroStudyMinutes: Int = 25,
    pomodoroBreakMinutes: Int = 5,
    onSelectPomodoroInterval: (Int, Int) -> Unit = { _, _ -> },
    isAssignmentAlertEnabled: Boolean = true,
    onToggleAssignmentAlert: () -> Unit = {},
    onDismiss: () -> Unit
) {
    // Only the clicked section is expanded; all others are strictly hidden!
    // Start with ROUTINE section open or null
    var activeExpandedSection by remember { mutableStateOf<SettingsSection?>(SettingsSection.ROUTINE) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showClearNotesConfirmation by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    fun toggleSection(section: SettingsSection) {
        activeExpandedSection = if (activeExpandedSection == section) null else section
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("unified_settings_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldAccent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Settings & Controls",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap any section to view & customize its options",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // QUICK JUMP CHIPS: Click to open/expand that section immediately
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SettingsSection.entries.forEach { section ->
                        val isExpanded = activeExpandedSection == section
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { toggleSection(section) }
                                .background(
                                    if (isExpanded) section.accentColor.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isExpanded) 1.5.dp else 0.5.dp,
                                    color = if (isExpanded) section.accentColor else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 11.dp, vertical = 7.dp)
                                .testTag("jump_tab_${section.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = null,
                                    tint = if (isExpanded) section.accentColor else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = section.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isExpanded) section.accentColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Vertical List of Expandable Section Cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // =========================================================
                    // 1. ROUTING SECTION CARD (Click to show / hide)
                    // =========================================================
                    ExpandableSectionCard(
                        section = SettingsSection.ROUTINE,
                        isExpanded = activeExpandedSection == SettingsSection.ROUTINE,
                        onToggle = { toggleSection(SettingsSection.ROUTINE) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Option: Persona & Title
                            Text("Routine Persona & Title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = {
                                    editedName = it
                                    onUpdateUserName(it)
                                },
                                singleLine = true,
                                placeholder = { Text("e.g. Student Mission 2026", fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldAccent,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            // Quick title chips
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Student Mission 2026", "Alex's Master Schedule", "Exam Sprint Prep", "Deep Work Mode").forEach { suggestion ->
                                    FilterChip(
                                        selected = editedName == suggestion,
                                        onClick = {
                                            editedName = suggestion
                                            onUpdateUserName(suggestion)
                                        },
                                        label = { Text(suggestion, fontSize = 10.sp) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldAccent.copy(alpha = 0.2f),
                                            selectedLabelColor = EmeraldAccent
                                        )
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Option: Routine Archetype Presets
                            Text("Switch Routine Preset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            RoutinePreset.entries.forEach { preset ->
                                val isSelected = currentPreset == preset
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectPreset(preset) }
                                        .background(if (isSelected) EmeraldAccent.copy(alpha = 0.12f) else Color.Transparent)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) EmeraldAccent else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(preset.subtitle, fontSize = 10.sp, color = TextSecondary)
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelectPreset(preset) },
                                        colors = RadioButtonDefaults.colors(selectedColor = EmeraldAccent)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Option: 24h Time Format
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("24-Hour Time Format", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Switch(
                                    checked = is24HourFormat,
                                    onCheckedChange = { onToggleTimeFormat() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent, checkedTrackColor = EmeraldAccent.copy(alpha = 0.4f))
                                )
                            }

                            // Option: Density
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (isCompactView) Icons.Default.ViewHeadline else Icons.Default.ViewAgenda,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compact Card Density", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Switch(
                                    checked = isCompactView,
                                    onCheckedChange = { onToggleCompactView() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent, checkedTrackColor = EmeraldAccent.copy(alpha = 0.4f))
                                )
                            }

                            // Option: JSON Export / Share
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onOpenExportImport()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export, Import & Share Routine (JSON)", fontSize = 12.sp)
                            }
                        }
                    }

                    // =========================================================
                    // 2. MODE SECTION CARD (Dark/Light & Palettes - NO BLUES)
                    // =========================================================
                    ExpandableSectionCard(
                        section = SettingsSection.MODE,
                        isExpanded = activeExpandedSection == SettingsSection.MODE,
                        onToggle = { toggleSection(SettingsSection.MODE) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Choose Dark or Light Palette (Zero Blue):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            listOf(
                                AppThemeMode.DARK_CYBER to ("Dark Emerald" to "AI & Tech (#121212 black • Radium Emerald • Neon Purple)"),
                                AppThemeMode.SOFT_TECH to ("Soft Sage & Peach" to "Wellness & SaaS (#F4F6F9 soft white • Light Peach • Mint)"),
                                AppThemeMode.ORGANIC_EARTH to ("Organic Earth" to "Lifestyle (#F9F6F0 cream • Terracotta Clay • Forest Olive)"),
                                AppThemeMode.NEON_POP to ("Neon Emerald & Violet" to "Fintech (#000000 OLED black • Emerald • Deep Violet)"),
                                AppThemeMode.SYSTEM to ("System Default" to "Auto-match Android device dark/light theme")
                            ).forEach { (mode, details) ->
                                val (name, desc) = details
                                val isSelected = themeMode == mode || (mode == AppThemeMode.DARK_CYBER && themeMode == AppThemeMode.DARK) || (mode == AppThemeMode.SOFT_TECH && themeMode == AppThemeMode.LIGHT)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onSelectTheme(mode) }
                                        .background(if (isSelected) PurpleAccent.copy(alpha = 0.12f) else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 1.dp else 0.dp,
                                            color = if (isSelected) PurpleAccent.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        // Palette preview dots
                                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(mode.bgColorPreview).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(mode.primaryColorPreview))
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(mode.secondaryColorPreview))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = name,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) PurpleAccent else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(desc, fontSize = 10.sp, color = TextSecondary, lineHeight = 12.sp)
                                        }
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelectTheme(mode) },
                                        colors = RadioButtonDefaults.colors(selectedColor = PurpleAccent)
                                    )
                                }
                            }
                        }
                    }

                    // =========================================================
                    // 3. ALERTE SECTION CARD (Alarms, Audio Tones, Vibrations)
                    // =========================================================
                    ExpandableSectionCard(
                        section = SettingsSection.ALERTS,
                        isExpanded = activeExpandedSection == SettingsSection.ALERTS,
                        onToggle = { toggleSection(SettingsSection.ALERTS) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Notifications toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Lock-Screen Notifications", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Heads-up banner alerts", fontSize = 10.sp, color = TextSecondary)
                                }
                                Switch(
                                    checked = isNotificationsEnabled,
                                    onCheckedChange = onToggleNotifications,
                                    colors = SwitchDefaults.colors(checkedThumbColor = AmberAccent, checkedTrackColor = AmberAccent.copy(alpha = 0.4f))
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Mute Audio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Mute All Audio Alarms", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Silent mode for routines", fontSize = 10.sp, color = TextSecondary)
                                }
                                Switch(
                                    checked = isAlarmMuted,
                                    onCheckedChange = { onToggleAlarmMute() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = RoseAccent, checkedTrackColor = RoseAccent.copy(alpha = 0.4f))
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Vibration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Vibration, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Haptic Vibration", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Switch(
                                    checked = isVibrationEnabled,
                                    onCheckedChange = { onToggleVibration() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent, checkedTrackColor = EmeraldAccent.copy(alpha = 0.4f))
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Pre-Task early warning
                            Text("Pre-Task Warning Minutes:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0 to "Off", 5 to "5 Min", 10 to "10 Min", 15 to "15 Min").forEach { (min, label) ->
                                    val isSelected = preTaskReminderMinutes == min
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSetPreTaskReminder(min) },
                                        label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberAccent.copy(alpha = 0.22f),
                                            selectedLabelColor = AmberAccent
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Audio Tone Picker with Instant Preview
                            Text("Alarm Tone (Tap Play to test):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            listOf("Double Beep", "Radar Pulse", "Zen Chime", "Urgent Warning", "Gentle Bell").forEach { tone ->
                                val isToneSelected = soundToneName == tone
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onSelectSoundTone(tone)
                                            onPreviewTone(tone)
                                        }
                                        .background(if (isToneSelected) AmberAccent.copy(alpha = 0.12f) else Color.Transparent)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { onPreviewTone(tone) }, modifier = Modifier.size(22.dp)) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Test tone", tint = AmberAccent, modifier = Modifier.size(15.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(tone, fontSize = 12.sp, fontWeight = if (isToneSelected) FontWeight.Bold else FontWeight.Normal, color = if (isToneSelected) AmberAccent else MaterialTheme.colorScheme.onSurface)
                                    }
                                    RadioButton(
                                        selected = isToneSelected,
                                        onClick = {
                                            onSelectSoundTone(tone)
                                            onPreviewTone(tone)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                                    )
                                }
                            }

                            // Auto dismiss
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Auto Turn-Off Alarm Timer:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(30 to "30s", 60 to "1 min", 120 to "2 min", 0 to "Manual").forEach { (sec, label) ->
                                    val isSelected = alarmAutoDismissSeconds == sec
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSelectAlarmAutoDismissSeconds(sec) },
                                        label = { Text(label, fontSize = 10.sp) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberAccent.copy(alpha = 0.22f),
                                            selectedLabelColor = AmberAccent
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = onTestAlarm,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trigger Test Alarm & Alert", fontSize = 12.sp)
                            }
                        }
                    }

                    // =========================================================
                    // 4. STUDENT LIFE SECTION CARD (Goals, Attendance, Prep)
                    // =========================================================
                    ExpandableSectionCard(
                        section = SettingsSection.STUDENT,
                        isExpanded = activeExpandedSection == SettingsSection.STUDENT,
                        onToggle = { toggleSection(SettingsSection.STUDENT) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Target Study Hours
                            Text("Daily Target Study Hours", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(2 to "2h", 4 to "4h", 6 to "6h", 8 to "8h", 10 to "10h").forEach { (h, label) ->
                                    val isSelected = studentStudyTargetHours == h
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSelectStudyTargetHours(h) },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldAccent.copy(alpha = 0.22f),
                                            selectedLabelColor = EmeraldAccent
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Text("Goal: $studentStudyTargetHours hours daily focus sprints.", fontSize = 10.sp, color = EmeraldAccent)

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // 75% Attendance Guard
                            Text("Attendance Target (75% Minimum Criteria)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(70 to "70%", 75 to "75% (Min)", 80 to "80%", 85 to "85%").forEach { (p, label) ->
                                    val isSelected = studentAttendanceTargetPercent == p
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSelectAttendanceTargetPercent(p) },
                                        label = { Text(label, fontSize = 10.sp) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AmberAccent.copy(alpha = 0.22f),
                                            selectedLabelColor = AmberAccent
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Exam Prep Mode Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Exam Prep / Revision Sprint Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Prioritizes study revision & suppresses casual alerts", fontSize = 10.sp, color = TextSecondary)
                                }
                                Switch(
                                    checked = isExamModeActive,
                                    onCheckedChange = { onToggleExamMode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = RoseAccent, checkedTrackColor = RoseAccent.copy(alpha = 0.4f))
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Lecture Silent Guard
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Lecture Silent Guard", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Auto-mutes during academic classes & lectures", fontSize = 10.sp, color = TextSecondary)
                                }
                                Switch(
                                    checked = isLectureSilentModeEnabled,
                                    onCheckedChange = { onToggleLectureSilentMode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent, checkedTrackColor = EmeraldAccent.copy(alpha = 0.4f))
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Pomodoro Study Intervals
                            Text("Pomodoro Study Sprints:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            listOf(
                                Triple(25, 5, "25m Study / 5m Rest (Classic)"),
                                Triple(45, 15, "45m Deep Study / 15m Rest (Exam Prep)"),
                                Triple(50, 10, "50m Mastery / 10m Walk")
                            ).forEach { (study, rest, label) ->
                                val isSelected = pomodoroStudyMinutes == study && pomodoroBreakMinutes == rest
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectPomodoroInterval(study, rest) }
                                        .background(if (isSelected) PurpleAccent.copy(alpha = 0.12f) else Color.Transparent)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(label, fontSize = 12.sp, color = if (isSelected) PurpleAccent else MaterialTheme.colorScheme.onSurface)
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelectPomodoroInterval(study, rest) },
                                        colors = RadioButtonDefaults.colors(selectedColor = PurpleAccent)
                                    )
                                }
                            }

                            // AI Coach
                            Button(
                                onClick = {
                                    onDismiss()
                                    onTriggerAiAnalyze()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyze Study Routine with AI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // =========================================================
                    // 5. MAINTENANCE & RESET SECTION CARD
                    // =========================================================
                    ExpandableSectionCard(
                        section = SettingsSection.MAINTENANCE,
                        isExpanded = activeExpandedSection == SettingsSection.MAINTENANCE,
                        onToggle = { toggleSection(SettingsSection.MAINTENANCE) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onClearTodayCompleted,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset Today's Completed Checks", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showClearNotesConfirmation = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Notes Across All Activities", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showResetConfirmation = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore Default Routine Schedule", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    // Confirmation dialog for Reset
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Restore Default Routine?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset all customized activity times and notes back to the default schedule.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSchedule()
                        showResetConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAccent)
                ) {
                    Text("Yes, Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog for Clear Notes
    if (showClearNotesConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearNotesConfirmation = false },
            title = { Text("Clear All Notes?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to erase all user notes and checklists attached to your activities?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllNotes()
                        showClearNotesConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAccent)
                ) {
                    Text("Clear Notes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearNotesConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Expandable Accordion Card:
 * ONLY displays its internal options when clicked (expanded).
 * Stays completely hidden when collapsed!
 */
@Composable
private fun ExpandableSectionCard(
    section: SettingsSection,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow_rotate")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isExpanded) 1.5.dp else 0.5.dp,
                color = if (isExpanded) section.accentColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Clickable Header Bar (Tap to reveal / hide options)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(section.accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = section.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = section.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpanded) section.accentColor else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(section.accentColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isExpanded) "OPEN" else section.optionsCount,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = section.accentColor
                                )
                            }
                        }
                        Text(
                            text = section.subtitle,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Rotating Arrow indicator
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isExpanded) section.accentColor else TextSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotationAngle)
                )
            }

            // INTERNAL OPTIONS: ONLY SHOWN WHEN CLICKED, OTHERWISE STRICTLY HIDDEN!
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    HorizontalDivider(
                        color = section.accentColor.copy(alpha = 0.25f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    content()
                }
            }
        }
    }
}
