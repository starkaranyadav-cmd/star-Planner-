package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScheduleItem
import com.example.ui.theme.ActiveCardBg
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoLight
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SecondaryAzureLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RoutineTimelineCard(
    item: ScheduleItem,
    isActive: Boolean,
    isNext: Boolean,
    isCompleted: Boolean,
    currentMinutes: Int,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    isCompact: Boolean = false,
    onToggleSubTask: ((subTaskId: String) -> Unit)? = null,
    onAddSubTask: ((text: String) -> Unit)? = null,
    onDeleteSubTask: ((subTaskId: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isEditingNotesInline by remember { mutableStateOf(false) }
    var currentNotesInput by remember(item.notes) { mutableStateOf(item.notes) }
    var isAddingSubTask by remember { mutableStateOf(false) }
    var newSubTaskInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "activeCardPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val progress = item.getProgress(currentMinutes)
    val remaining = item.getRemainingMinutes(currentMinutes)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_item_${item.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Timeline Node & Connecting Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(58.dp)
        ) {
            Text(
                text = item.start,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = ScheduleItem.format12h(item.start).split(" ").getOrNull(1) ?: "",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Timeline Node Circle
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isCompleted -> EmeraldAccent
                            isNext -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            isActive -> MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
                            isCompleted -> EmeraldAccent
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
            )

            // Timeline Connector Vertical Line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (isCompact) 48.dp else if (item.checklist.isNotEmpty() || item.notes.isNotBlank() || isEditingNotesInline) 120.dp else 75.dp)
                    .then(
                        if (isActive) {
                            Modifier.background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.outline)))
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.outlineVariant)
                        }
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Column: Main Schedule / Todo Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isActive) {
                        Modifier.border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    isCompleted -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(if (isActive) 4.dp else 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompact) 10.dp else 14.dp)
            ) {
                // Header Row: Category Badge + Priority Pill + Duration + Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(item.category.accentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = item.category.icon,
                                contentDescription = null,
                                tint = item.category.accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.category.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = item.category.accentColor
                            )
                        }

                        // Priority Tag
                        Text(
                            text = item.priority.title.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.priority.color,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(item.priority.color.copy(alpha = 0.12f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )

                        // Duration pill
                        Text(
                            text = item.formatDuration(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Checkbox & Edit Action
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Routine",
                                tint = TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleCompleted,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("checkbox_${item.id}")
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Mark Complete",
                                tint = if (isCompleted) EmeraldAccent else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Activity Title
                Text(
                    text = item.activity,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isCompleted) TextMuted else MaterialTheme.colorScheme.onSurface
                )

                // Description
                Text(
                    text = item.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCompleted) TextMuted else TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Time window & status tags
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.formattedTimeRange12h(),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    if (isActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(PrimaryIndigoLight.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = PrimaryIndigoLight,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "ACTIVE NOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigoLight,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else if (isNext) {
                        Text(
                            text = "UPCOMING NEXT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryAzureLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SecondaryAzureLight.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // If active: Progress bar & countdown
                if (isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}% Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryIndigoLight
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$remaining mins left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PrimaryIndigoLight,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // =====================================================================
                // SUB-TASKS / CHECKLIST SECTION (To-Do Application features)
                // =====================================================================
                if (item.checklist.isNotEmpty() || onAddSubTask != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SUB-TASKS (${item.checklist.count { it.isDone }}/${item.checklist.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigoLight,
                                letterSpacing = 0.5.sp
                            )
                            if (onAddSubTask != null && !isAddingSubTask) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { isAddingSubTask = true }
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryIndigoLight, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Add item", fontSize = 10.sp, color = PrimaryIndigoLight, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Checklist items
                        item.checklist.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSubTask?.invoke(sub.id) }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (sub.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (sub.isDone) EmeraldAccent else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = sub.text,
                                        fontSize = 12.sp,
                                        color = if (sub.isDone) TextMuted else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (sub.isDone) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }

                                if (onDeleteSubTask != null) {
                                    IconButton(
                                        onClick = { onDeleteSubTask(sub.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }

                        // Inline add sub-task field
                        if (isAddingSubTask) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newSubTaskInput,
                                    onValueChange = { newSubTaskInput = it },
                                    placeholder = { Text("New sub-task...", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryIndigoLight,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        if (newSubTaskInput.isNotBlank()) {
                                            onAddSubTask?.invoke(newSubTaskInput)
                                            newSubTaskInput = ""
                                            isAddingSubTask = false
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Add", tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        newSubTaskInput = ""
                                        isAddingSubTask = false
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Editable Quick Notes / Checklist Section
                if (!isEditingNotesInline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .clickable { isEditingNotesInline = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = if (item.notes.isNotBlank()) PrimaryIndigoLight else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (item.notes.isNotBlank()) item.notes else "+ Add quick notes or checklists...",
                                    fontSize = 12.sp,
                                    color = if (item.notes.isNotBlank()) MaterialTheme.colorScheme.onSurface else TextMuted,
                                    lineHeight = 16.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Note",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    // Inline Note Editor
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = currentNotesInput,
                            onValueChange = { currentNotesInput = it },
                            placeholder = { Text("Write quick thoughts, checklist...", fontSize = 12.sp, color = TextMuted) },
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigoLight,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    currentNotesInput = item.notes
                                    isEditingNotesInline = false
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onUpdateNotes(currentNotesInput)
                                    isEditingNotesInline = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigoLight),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
