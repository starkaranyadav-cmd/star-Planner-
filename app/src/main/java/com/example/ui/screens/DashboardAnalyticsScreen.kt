package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RoutineCategory
import com.example.data.ScheduleItem
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.min

@Composable
fun DashboardAnalyticsScreen(
    schedule: List<ScheduleItem>,
    completedTaskIds: Set<Int>,
    currentMinutes: Int,
    onRunAiAnalyze: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChartFilter by remember { mutableStateOf("All") }
    var selectedBarItem by remember { mutableStateOf<ScheduleItem?>(null) }

    val totalPlannedMinutes = remember(schedule) {
        schedule.sumOf { it.getDurationMinutes() }
    }

    val totalCompletedMinutes = remember(schedule, completedTaskIds) {
        schedule.filter { completedTaskIds.contains(it.id) }.sumOf { it.getDurationMinutes() }
    }

    val deepFocusMinutes = remember(schedule) {
        schedule.filter { it.category == RoutineCategory.DEEP_FOCUS || it.category == RoutineCategory.STUDY }
            .sumOf { it.getDurationMinutes() }
    }

    val fitnessMinutes = remember(schedule) {
        schedule.filter { it.category == RoutineCategory.WORKOUT || it.category == RoutineCategory.WALK }
            .sumOf { it.getDurationMinutes() }
    }

    val efficiencyRatio = if (totalPlannedMinutes > 0) {
        (totalCompletedMinutes.toFloat() / totalPlannedMinutes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(schedule, completedTaskIds) {
        animProgress.animateTo(
            targetValue = efficiencyRatio,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
    ) {
        // 1. Efficiency Score & Circular Gauge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("efficiency_score_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Productivity Efficiency",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Planned vs. Completed routine performance",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        efficiencyRatio >= 0.75f -> EmeraldAccent.copy(alpha = 0.18f)
                                        efficiencyRatio >= 0.4f -> CyanAccent.copy(alpha = 0.18f)
                                        else -> AmberAccent.copy(alpha = 0.18f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when {
                                    efficiencyRatio >= 0.75f -> "Elite Discipline"
                                    efficiencyRatio >= 0.4f -> "Consistent"
                                    else -> "In Progress"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    efficiencyRatio >= 0.75f -> EmeraldAccent
                                    efficiencyRatio >= 0.4f -> CyanAccent
                                    else -> AmberAccent
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Gauge & Highlights Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Circular Gauge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            val trackColor = MaterialTheme.colorScheme.surfaceVariant
                            Canvas(modifier = Modifier.size(105.dp)) {
                                drawArc(
                                    color = trackColor,
                                    startAngle = 135f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(listOf(CyanAccent, EmeraldAccent, PurpleAccent)),
                                    startAngle = 135f,
                                    sweepAngle = 270f * animProgress.value,
                                    useCenter = false,
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(animProgress.value * 100).toInt()}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = "Completed",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Stat Summary Pills
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatMiniBadge(
                                label = "Total Planned",
                                value = "${totalPlannedMinutes / 60}h ${totalPlannedMinutes % 60}m",
                                color = CyanAccent
                            )
                            StatMiniBadge(
                                label = "Completed Time",
                                value = "${totalCompletedMinutes / 60}h ${totalCompletedMinutes % 60}m",
                                color = EmeraldAccent
                            )
                            StatMiniBadge(
                                label = "Deep Focus Time",
                                value = "${deepFocusMinutes / 60}h ${deepFocusMinutes % 60}m",
                                color = PurpleAccent
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Time Spent on Tasks vs. Planned Time Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("planned_vs_actual_chart_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Task Duration: Planned vs. Spent",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Chart Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CyanAccent))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Planned Time", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(EmeraldAccent))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Completed Time", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Horizontal Bars for each task
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val maxMinutes = schedule.maxOfOrNull { it.getDurationMinutes() } ?: 1
                        schedule.forEach { item ->
                            val isCompleted = completedTaskIds.contains(item.id)
                            val plannedDuration = item.getDurationMinutes()
                            val actualDuration = if (isCompleted) plannedDuration else if (item.isActive(currentMinutes)) {
                                (plannedDuration * item.getProgress(currentMinutes)).toInt()
                            } else 0

                            TaskComparisonBarRow(
                                item = item,
                                plannedMinutes = plannedDuration,
                                actualMinutes = actualDuration,
                                maxMinutes = maxMinutes,
                                isCompleted = isCompleted,
                                isSelected = selectedBarItem?.id == item.id,
                                onClick = {
                                    selectedBarItem = if (selectedBarItem?.id == item.id) null else item
                                }
                            )
                        }
                    }

                    // Detail Tooltip if bar selected
                    selectedBarItem?.let { selected ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = selected.activity,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${selected.start} - ${selected.end} (${selected.formatDuration()})",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = if (completedTaskIds.contains(selected.id)) "100% Completed" else "Pending Completion",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (completedTaskIds.contains(selected.id)) EmeraldAccent else AmberAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Category Time Distribution Donut / Ring Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_distribution_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Time by Category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Group schedule by category
                    val categoryGroup = remember(schedule) {
                        schedule.groupBy { it.category }
                            .mapValues { entry -> entry.value.sumOf { it.getDurationMinutes() } }
                            .toList()
                            .sortedByDescending { it.second }
                    }

                    // Donut Chart Graphic & Category Breakdown Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Donut Visual
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                var currentAngle = -90f
                                categoryGroup.forEach { (category, minutes) ->
                                    val sweep = if (totalPlannedMinutes > 0) {
                                        (minutes.toFloat() / totalPlannedMinutes.toFloat()) * 360f
                                    } else 0f
                                    drawArc(
                                        color = category.accentColor,
                                        startAngle = currentAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = 16.dp.toPx())
                                    )
                                    currentAngle += sweep
                                }
                            }
                            Text(
                                text = "24h",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Category List
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categoryGroup.take(5).forEach { (cat, minutes) ->
                                val hrs = minutes / 60
                                val mins = minutes % 60
                                val timeText = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                                val percentage = if (totalPlannedMinutes > 0) (minutes * 100) / totalPlannedMinutes else 0

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(cat.accentColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cat.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "$timeText ($percentage%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. 24-Hour Visual Daily Timeline Ribbon
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("24h_timeline_ribbon_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "24-Hour Day Distribution",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 24h Ribbon Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            schedule.forEach { item ->
                                val startM = item.getStartMinutes()
                                val endM = item.getEndMinutes()
                                if (startM < endM) {
                                    val left = (startM.toFloat() / 1440f) * size.width
                                    val width = ((endM - startM).toFloat() / 1440f) * size.width
                                    drawRect(
                                        color = item.category.accentColor,
                                        topLeft = Offset(left, 0f),
                                        size = Size(width, size.height)
                                    )
                                } else {
                                    // Crosses midnight
                                    val left1 = (startM.toFloat() / 1440f) * size.width
                                    val width1 = ((1440 - startM).toFloat() / 1440f) * size.width
                                    drawRect(
                                        color = item.category.accentColor,
                                        topLeft = Offset(left1, 0f),
                                        size = Size(width1, size.height)
                                    )
                                    val width2 = (endM.toFloat() / 1440f) * size.width
                                    drawRect(
                                        color = item.category.accentColor,
                                        topLeft = Offset(0f, 0f),
                                        size = Size(width2, size.height)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEach { mark ->
                            Text(text = mark, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskComparisonBarRow(
    item: ScheduleItem,
    plannedMinutes: Int,
    actualMinutes: Int,
    maxMinutes: Int,
    isCompleted: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val plannedRatio = (plannedMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.05f, 1f)
    val actualRatio = (actualMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(item.category.accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.activity,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "${plannedMinutes}m planned",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dual stacked bar: Planned (cyan) & Actual (emerald)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Planned bar background
            Box(
                modifier = Modifier
                    .fillMaxWidth(plannedRatio)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyanAccent.copy(alpha = 0.35f))
            )
            // Actual/Completed bar foreground
            if (actualRatio > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(actualRatio)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCompleted) EmeraldAccent else AmberAccent)
                )
            }
        }
    }
}

@Composable
fun StatMiniBadge(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(text = label, fontSize = 10.sp, color = TextSecondary)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
