package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class LapData(
    val lapIndex: Int,
    val lapTimeMs: Long,
    val totalTimeMs: Long
)

@Composable
fun StopwatchView(
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<LapData>() }
    var lastLapTotalMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - elapsedTimeMs
            while (isActive && isRunning) {
                elapsedTimeMs = System.currentTimeMillis() - startTime
                delay(30)
            }
        }
    }

    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        val centis = (ms % 1000) / 10
        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, centis)
        } else {
            String.format("%02d:%02d.%02d", minutes, seconds, centis)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stopwatch_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PurpleAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Workout & Focus Stopwatch",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (laps.isNotEmpty()) {
                    Text(
                        text = "${laps.size} Laps",
                        fontSize = 12.sp,
                        color = PurpleAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Digital Stopwatch Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceVariantDark)
                    .border(
                        width = 1.dp,
                        color = if (isRunning) CyanAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatTime(elapsedTimeMs),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRunning) CyanAccent else TextPrimary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.testTag("stopwatch_timer_display")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Stopwatch Controls: Start / Pause, Lap, Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Reset Button
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        elapsedTimeMs = 0L
                        lastLapTotalMs = 0L
                        laps.clear()
                    },
                    enabled = elapsedTimeMs > 0L && !isRunning,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stopwatch_reset_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RoseAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 12.sp)
                }

                // Lap Button
                Button(
                    onClick = {
                        val currentMs = elapsedTimeMs
                        val lapDuration = currentMs - lastLapTotalMs
                        lastLapTotalMs = currentMs
                        laps.add(0, LapData(laps.size + 1, lapDuration, currentMs))
                    },
                    enabled = isRunning,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stopwatch_lap_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleAccent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lap", fontSize = 12.sp)
                }

                // Play / Pause Button
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("stopwatch_toggle_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) AmberAccent else CyanAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Pause" else "Start",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Laps History Table
            AnimatedVisibility(visible = laps.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(color = SurfaceVariantDark, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("LAP TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("TOTAL TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    }

                    val fastest = if (laps.size > 1) laps.minByOrNull { it.lapTimeMs } else null
                    val slowest = if (laps.size > 1) laps.maxByOrNull { it.lapTimeMs } else null

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                    ) {
                        laps.take(5).forEach { lap ->
                            val isFastest = fastest != null && lap.lapIndex == fastest.lapIndex
                            val isSlowest = slowest != null && lap.lapIndex == slowest.lapIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lap ${lap.lapIndex}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        isFastest -> EmeraldAccent
                                        isSlowest -> RoseAccent
                                        else -> TextPrimary
                                    }
                                )
                                Text(
                                    text = formatTime(lap.lapTimeMs),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = when {
                                        isFastest -> EmeraldAccent
                                        isSlowest -> RoseAccent
                                        else -> CyanAccent
                                    }
                                )
                                Text(
                                    text = formatTime(lap.totalTimeMs),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
