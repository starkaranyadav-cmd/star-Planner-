package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ScheduleItem
import com.example.ui.components.AddActivityDialog
import com.example.ui.components.AiAnalysisDialog
import com.example.ui.components.AlarmRingingBanner
import com.example.ui.components.ControlsHeader
import com.example.ui.components.CurrentAndNextHeroCard
import com.example.ui.components.DigitalClockHeader
import com.example.ui.components.EditRoutineDialog
import com.example.ui.components.ExportImportDialog
import com.example.ui.components.FocusModeCard
import com.example.ui.components.RoutineTimelineCard
import com.example.ui.components.StopwatchView
import com.example.ui.components.ThemeSettingDialog
import com.example.ui.components.UnifiedSettingsDialog
import com.example.ui.screens.DashboardAnalyticsScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SecondaryAzure
import com.example.ui.theme.SecondaryAzureLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RoutineViewModel
import com.example.ui.viewmodel.TodoFilterTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRoutineScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var editingItem by remember { mutableStateOf<ScheduleItem?>(null) }

    // BackHandler for secondary tabs
    BackHandler(enabled = uiState.currentTab != 0) {
        viewModel.setTab(0)
    }

    // Permission launcher for Android 13+ Notifications
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.toggleNotifications(true)
        }
    }

    // Snackbar notifications
    LaunchedEffect(uiState.lastAlertMessage) {
        uiState.lastAlertMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissLastAlert()
        }
    }

    // Filter displayed schedule by category AND Todo filter (ALL, PENDING, COMPLETED)
    val displayedSchedule = remember(
        uiState.schedule,
        uiState.selectedCategory,
        uiState.todoFilter,
        uiState.completedTaskIds
    ) {
        val categoryFiltered = if (uiState.selectedCategory == null) {
            uiState.schedule
        } else {
            uiState.schedule.filter { it.category == uiState.selectedCategory }
        }

        when (uiState.todoFilter) {
            TodoFilterTab.ALL -> categoryFiltered
            TodoFilterTab.PENDING -> categoryFiltered.filter { !uiState.completedTaskIds.contains(it.id) }
            TodoFilterTab.COMPLETED -> categoryFiltered.filter { uiState.completedTaskIds.contains(it.id) }
        }
    }

    val totalCount = uiState.schedule.size
    val completedCount = uiState.completedTaskIds.size
    val pendingCount = (totalCount - completedCount).coerceAtLeast(0)
    val completionPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_routine_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.currentTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.setShowAddActivityDialog(true) },
                    containerColor = PrimaryIndigoLight,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Task")
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigoLight.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = PrimaryIndigoLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.userName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = uiState.currentPreset.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryIndigoLight
                            )
                        }
                    }
                },
                navigationIcon = {
                    // Quick "+ Add Task" button
                    IconButton(
                        onClick = { viewModel.setShowAddActivityDialog(true) },
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .testTag("add_activity_top_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigoLight.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Custom Activity",
                                tint = PrimaryIndigoLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Quick Palette / Color Theme Switcher
                    IconButton(
                        onClick = { viewModel.setShowThemeDialog(true) },
                        modifier = Modifier.testTag("theme_selector_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme Palette",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Unified Settings Button
                    IconButton(
                        onClick = { viewModel.setShowSettingsDialog(true) },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "App Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // Tab 0: Routine / To-Do Timeline
                NavigationBarItem(
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Routine") },
                    label = { Text("Routine", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryIndigoLight,
                        selectedTextColor = PrimaryIndigoLight,
                        indicatorColor = PrimaryIndigoLight.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_routine")
                )

                // Tab 1: Analytics / Dashboard
                NavigationBarItem(
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SecondaryAzureLight,
                        selectedTextColor = SecondaryAzureLight,
                        indicatorColor = SecondaryAzureLight.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_analytics")
                )

                // Tab 2: Focus Shield
                NavigationBarItem(
                    selected = uiState.currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Focus Shield") },
                    label = { Text("Focus Shield", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurpleAccent,
                        selectedTextColor = PurpleAccent,
                        indicatorColor = PurpleAccent.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_focus")
                )

                // Tab 3: Stopwatch
                NavigationBarItem(
                    selected = uiState.currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Stopwatch") },
                    label = { Text("Stopwatch", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberAccent,
                        selectedTextColor = AmberAccent,
                        indicatorColor = AmberAccent.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_stopwatch")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (uiState.currentTab) {
                // =============================================================
                // TAB 0: ROUTINE TIMELINE & TO-DO APPLICATION HUB
                // =============================================================
                0 -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // High Priority Alarm Ringing Banner (if active)
                        if (uiState.isAlarmRinging) {
                            item {
                                AlarmRingingBanner(
                                    isRinging = uiState.isAlarmRinging,
                                    item = uiState.ringingItem ?: uiState.activeItem,
                                    onTurnOff = { viewModel.turnOffAlarm() },
                                    onSnooze = { viewModel.snoozeAlarm(5) }
                                )
                            }
                        }

                        // Notification Permission Request Banner if not granted
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = AmberAccent.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = AmberAccent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Notification Permission Needed",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AmberAccent
                                                )
                                                Text(
                                                    text = "Tap to show alarms on lock screen and status bar",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        Text(
                                            text = "ALLOW",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AmberAccent,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(AmberAccent.copy(alpha = 0.25f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Digital Clock Live Header
                        item {
                            DigitalClockHeader(
                                timeString = uiState.timeFormatted,
                                dateString = uiState.dateFormatted,
                                dayOfWeek = uiState.dayOfWeek,
                                currentMinutes = uiState.currentMinutesOfDay,
                                isSimulated = uiState.isSimulatedMode
                            )
                        }

                        // HERO COMPONENT: Current Activity & Next Up What to Do
                        item {
                            val active = uiState.activeItem
                            val isCompleted = active?.let { uiState.completedTaskIds.contains(it.id) } ?: false

                            CurrentAndNextHeroCard(
                                activeItem = active,
                                nextItem = uiState.nextItem,
                                currentMinutes = uiState.currentMinutesOfDay,
                                isCompleted = isCompleted,
                                onToggleComplete = {
                                    active?.let { viewModel.toggleTaskCompleted(it.id) }
                                },
                                onToggleSubTask = { subId ->
                                    active?.let { viewModel.toggleSubTask(it.id, subId) }
                                }
                            )
                        }

                        // Controls Header (Toggles, Category filters, simulation)
                        item {
                            ControlsHeader(
                                isNotificationsEnabled = uiState.isNotificationsEnabled,
                                onToggleNotifications = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.toggleNotifications(enabled)
                                    }
                                },
                                isAlarmMuted = uiState.isAlarmMuted,
                                onToggleAlarmMute = { viewModel.toggleAlarmMute() },
                                onTestAlert = { viewModel.testAlarmAndNotification() },
                                isSimulatedMode = uiState.isSimulatedMode,
                                simulatedMinutes = uiState.simulatedMinutesOfDay,
                                onToggleSimulation = { enabled ->
                                    viewModel.setSimulationMode(
                                        enabled,
                                        if (enabled) uiState.currentMinutesOfDay else 0
                                    )
                                },
                                onSimulateMinuteChange = { viewModel.setSimulatedMinutes(it) },
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = { viewModel.selectCategory(it) },
                                completedCount = uiState.completedTaskIds.size,
                                totalCount = uiState.schedule.size,
                                onResetSchedule = { viewModel.resetToDefaults() }
                            )
                        }

                        // =====================================================
                        // TO-DO APPLICATION FEATURES: KPI & FILTER TABS
                        // =====================================================
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Progress Bar & KPI
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "TODAY'S TO-DO PROGRESS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryIndigoLight,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "$completedCount of $totalCount Done ($completionPercent%)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EmeraldAccent
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = EmeraldAccent,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Filter Tabs: All Tasks | To-Do | Done
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            TodoFilterTab.ALL to "All ($totalCount)",
                                            TodoFilterTab.PENDING to "To-Do ($pendingCount)",
                                            TodoFilterTab.COMPLETED to "Done ($completedCount)"
                                        ).forEach { (tab, label) ->
                                            val isSelected = uiState.todoFilter == tab
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.setTodoFilter(tab) },
                                                label = {
                                                    Text(
                                                        text = label,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = PrimaryIndigoLight.copy(alpha = 0.2f),
                                                    selectedLabelColor = PrimaryIndigoLight,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                    labelColor = MaterialTheme.colorScheme.onSurface
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section Title & Add Task Button
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TASKS & TIMELINE (${displayedSchedule.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setShowAddActivityDialog(true) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryIndigoLight, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+ Add Task", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigoLight)
                                }
                            }
                        }

                        // Schedule Items with checklist & notes support
                        items(
                            items = displayedSchedule,
                            key = { it.id }
                        ) { item ->
                            val isActive = uiState.activeItem?.id == item.id
                            val isNext = uiState.nextItem?.id == item.id
                            val isCompleted = uiState.completedTaskIds.contains(item.id)

                            RoutineTimelineCard(
                                item = item,
                                isActive = isActive,
                                isNext = isNext,
                                isCompleted = isCompleted,
                                currentMinutes = uiState.currentMinutesOfDay,
                                onToggleCompleted = { viewModel.toggleTaskCompleted(item.id) },
                                onEdit = { editingItem = item },
                                onUpdateNotes = { newNotes -> viewModel.updateNotes(item.id, newNotes) },
                                isCompact = uiState.isCompactView,
                                onToggleSubTask = { subId -> viewModel.toggleSubTask(item.id, subId) },
                                onAddSubTask = { text -> viewModel.addSubTask(item.id, text) },
                                onDeleteSubTask = { subId -> viewModel.deleteSubTask(item.id, subId) }
                            )
                        }
                    }
                }

                // TAB 1: ANALYTICS DASHBOARD
                1 -> {
                    DashboardAnalyticsScreen(
                        schedule = uiState.schedule,
                        completedTaskIds = uiState.completedTaskIds,
                        currentMinutes = uiState.currentMinutesOfDay,
                        onRunAiAnalyze = { viewModel.requestAiDailyAnalysis() },
                        modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                    )
                }

                // TAB 2: FOCUS SHIELD (Calls, SMS, Notifications blocker)
                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            FocusModeCard(
                                isFocusModeActive = uiState.isFocusModeActive,
                                focusMinutesRemaining = uiState.focusMinutesRemaining,
                                hasDndPermission = uiState.hasDndPermission,
                                onToggleFocusMode = { enabled ->
                                    if (enabled && !uiState.hasDndPermission) {
                                        context.startActivity(viewModel.getDndSettingsIntent())
                                    } else {
                                        viewModel.toggleFocusMode(enabled)
                                    }
                                },
                                onRequestDndPermission = {
                                    context.startActivity(viewModel.getDndSettingsIntent())
                                },
                                onSelectDurationMinutes = { viewModel.setFocusDurationMinutes(it) }
                            )
                        }

                        // Focus Tips & Guidelines Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Deep Focus Shield Guidelines",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "• In Focus Mode, Android Do Not Disturb silences all incoming phone calls, SMS, WhatsApp, and social notifications.\n• Only emergency alarms remain active so your schedule remains respected.\n• Ideal for Practice Block 1 (Deep Coding) and Practice Block 2.",
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 3: STOPWATCH
                3 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                    ) {
                        item {
                            StopwatchView()
                        }
                    }
                }
            }
        }
    }

    // Add Activity Dialog
    if (uiState.showAddActivityDialog) {
        AddActivityDialog(
            onDismiss = { viewModel.setShowAddActivityDialog(false) },
            onAddActivity = { newActivity -> viewModel.addActivity(newActivity) }
        )
    }

    // Export & Import / Share Dialog
    if (uiState.showExportImportDialog) {
        ExportImportDialog(
            schedule = uiState.schedule,
            onImport = { json -> viewModel.importScheduleJson(json) },
            onDismiss = { viewModel.setShowExportImportDialog(false) }
        )
    }

    // Unified Settings Dialog (AI Analyze, Preset switch, Theme, Alarms, Restore defaults)
    if (uiState.showSettingsDialog) {
        UnifiedSettingsDialog(
            themeMode = uiState.themeMode,
            onSelectTheme = { viewModel.setThemeMode(it) },
            currentPreset = uiState.currentPreset,
            onSelectPreset = { viewModel.loadPreset(it) },
            is24HourFormat = uiState.is24HourFormat,
            onToggleTimeFormat = { viewModel.toggleTimeFormat() },
            preTaskReminderMinutes = uiState.preTaskReminderMinutes,
            onSetPreTaskReminder = { viewModel.setPreTaskReminder(it) },
            isVibrationEnabled = uiState.isVibrationEnabled,
            onToggleVibration = { viewModel.toggleVibration() },
            soundToneName = uiState.soundToneName,
            onSelectSoundTone = { viewModel.setSoundTone(it) },
            isNotificationsEnabled = uiState.isNotificationsEnabled,
            onToggleNotifications = { viewModel.toggleNotifications(it) },
            isAlarmMuted = uiState.isAlarmMuted,
            onToggleAlarmMute = { viewModel.toggleAlarmMute() },
            onTestAlarm = { viewModel.testAlarmAndNotification() },
            onTriggerAiAnalyze = { viewModel.requestAiDailyAnalysis() },
            onOpenExportImport = { viewModel.setShowExportImportDialog(true) },
            onResetSchedule = { viewModel.resetToDefaults() },
            userName = uiState.userName,
            onUpdateUserName = { viewModel.setUserName(it) },
            isCompactView = uiState.isCompactView,
            onToggleCompactView = { viewModel.toggleCompactView() },
            alarmAutoDismissSeconds = uiState.alarmAutoDismissSeconds,
            onSelectAlarmAutoDismissSeconds = { viewModel.setAlarmAutoDismissSeconds(it) },
            onPreviewTone = { viewModel.previewSoundTone(it) },
            onClearTodayCompleted = { viewModel.clearTodayCompleted() },
            onClearAllNotes = { viewModel.clearAllNotes() },
            // Student Life & Academic Parameters
            studentStudyTargetHours = uiState.studentStudyTargetHours,
            onSelectStudyTargetHours = { viewModel.setStudentStudyTargetHours(it) },
            studentAttendanceTargetPercent = uiState.studentAttendanceTargetPercent,
            onSelectAttendanceTargetPercent = { viewModel.setStudentAttendanceTargetPercent(it) },
            isExamModeActive = uiState.isExamModeActive,
            onToggleExamMode = { viewModel.toggleExamMode() },
            isLectureSilentModeEnabled = uiState.isLectureSilentModeEnabled,
            onToggleLectureSilentMode = { viewModel.toggleLectureSilentMode() },
            pomodoroStudyMinutes = uiState.pomodoroStudyMinutes,
            pomodoroBreakMinutes = uiState.pomodoroBreakMinutes,
            onSelectPomodoroInterval = { study, rest -> viewModel.setPomodoroInterval(study, rest) },
            isAssignmentAlertEnabled = uiState.isAssignmentAlertEnabled,
            onToggleAssignmentAlert = { viewModel.toggleAssignmentAlert() },
            onDismiss = { viewModel.setShowSettingsDialog(false) }
        )
    }

    // Theme Palette Picker Modal Dialog
    if (uiState.showThemeDialog) {
        ThemeSettingDialog(
            currentTheme = uiState.themeMode,
            onSelectTheme = {
                viewModel.setThemeMode(it)
                viewModel.setShowThemeDialog(false)
            },
            onDismiss = { viewModel.setShowThemeDialog(false) }
        )
    }

    // AI Analysis Modal Dialog
    if (uiState.showAiDialog) {
        AiAnalysisDialog(
            isLoading = uiState.isAiAnalyzing,
            analysisResult = uiState.aiAnalysisResult,
            onRefresh = { viewModel.requestAiDailyAnalysis() },
            onDismiss = { viewModel.dismissAiDialog() }
        )
    }

    // Edit Routine Modal Dialog
    editingItem?.let { item ->
        EditRoutineDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = { updated ->
                viewModel.updateScheduleItem(updated)
                editingItem = null
            },
            onDelete = {
                viewModel.deleteActivity(item.id)
                editingItem = null
            }
        )
    }
}
