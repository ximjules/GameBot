package com.gamebot.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamebot.app.data.EventModel
import com.gamebot.app.notifications.NotificationHelper
import com.gamebot.app.ui.theme.*
import com.gamebot.app.viewmodel.EventViewModel
import java.time.YearMonth

@Composable
fun CalendarScreen(viewModel: EventViewModel = viewModel()) {
    val today = remember { java.time.LocalDate.now() }
    var selectedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDay by remember { mutableStateOf(today.dayOfMonth.coerceAtMost(selectedMonth.lengthOfMonth())) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val selectedEvents = uiState.events.filter { event ->
        event.day == selectedDay && event.belongsToMonth(selectedMonth)
    }
    val eventDays = uiState.events
        .filter { it.belongsToMonth(selectedMonth) }
        .map { it.day }
        .toSet()
    val monthNames = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Events",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add event",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newMonth = selectedMonth.minusMonths(1)
                        selectedMonth = newMonth
                        selectedDay = selectedDay.coerceAtMost(newMonth.lengthOfMonth())
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Prethodni mjesec",
                        tint = TextSecondary
                    )
                }
                Text(
                    text = "${monthNames[selectedMonth.monthValue - 1]} ${selectedMonth.year}.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = {
                        val newMonth = selectedMonth.plusMonths(1)
                        selectedMonth = newMonth
                        selectedDay = selectedDay.coerceAtMost(newMonth.lengthOfMonth())
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Sljedeći mjesec",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val dayNames = listOf("Pon", "Uto", "Sri", "Čet", "Pet", "Sub", "Ned")
            Row(modifier = Modifier.fillMaxWidth()) {
                dayNames.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val leadingEmptyDays = selectedMonth.atDay(1).dayOfWeek.value - 1
            val calendarCells = List(leadingEmptyDays) { 0 } + (1..selectedMonth.lengthOfMonth()).toList()
            calendarCells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        if (day == 0) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            day == selectedDay -> PrimaryPurple
                                            day in eventDays -> PrimaryPurple.copy(alpha = 0.2f)
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        }
                                    )
                                    .clickable { selectedDay = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 12.sp,
                                    color = when {
                                        day == selectedDay -> TextPrimary
                                        day in eventDays -> PrimaryPurple
                                        else -> TextSecondary
                                    },
                                    fontWeight = if (day == selectedDay) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    repeat(7 - week.size) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedEvents.isEmpty()) {
                    "Events for ${selectedDay}.${selectedMonth.monthValue}.${selectedMonth.year}."
                } else {
                    "${selectedEvents.size} event(s) for ${selectedDay}.${selectedMonth.monthValue}.${selectedMonth.year}."
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        uiState.error?.let { error ->
            Text(
                text = error,
                fontSize = 12.sp,
                color = ErrorRed,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
            selectedEvents.isEmpty() -> EmptyEventDayCard(onAddClick = { showAddDialog = true })
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(selectedEvents, key = { it.id }) { event ->
                        EventDetailItem(
                            event = event,
                            context = context,
                            currentUserId = viewModel.currentUserId,
                            onJoin = { viewModel.joinPublicEvent(event.id) },
                            onDelete = { viewModel.deleteEvent(event) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            selectedDay = selectedDay,
            selectedMonth = selectedMonth,
            onDismiss = { showAddDialog = false },
            onAdd = { event ->
                viewModel.addEvent(event)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyEventDayCard(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onAddClick() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = "No events",
                tint = TextHint,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "No events for this day", fontSize = 14.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Tap to add one", fontSize = 12.sp, color = PrimaryPurple)
        }
    }
}

@Composable
fun AddEventDialog(
    selectedDay: Int,
    selectedMonth: YearMonth,
    onDismiss: () -> Unit,
    onAdd: (EventModel) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("1") }
    var isPublic by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "Add event",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event title", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = eventTextFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 20:00 - 23:00)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = eventTextFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = eventTextFieldColors()
                )
                OutlinedTextField(
                    value = participants,
                    onValueChange = { participants = it },
                    label = { Text("Participant limit", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = eventTextFieldColors(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Public event", fontSize = 13.sp, color = TextPrimary)
                        Text(
                            text = "Other users can see it and join",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            EventModel(
                                title = title.trim(),
                                date = "${selectedDay}.${selectedMonth.monthValue}.${selectedMonth.year}.",
                                time = time.trim(),
                                description = description.trim(),
                                participants = participants.toIntOrNull() ?: 1,
                                day = selectedDay,
                                month = selectedMonth.monthValue,
                                year = selectedMonth.year,
                                isPublic = isPublic
                            )
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun EventDetailItem(
    event: EventModel,
    context: android.content.Context,
    currentUserId: String,
    onJoin: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isOwner = event.userId == currentUserId
    val hasJoined = currentUserId in event.joinedBy

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(14.dp)
    ) {
        Text(
            text = event.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (event.isPublic) PrimaryPurple.copy(alpha = 0.16f) else SurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (event.isPublic) "Public event" else "Private event",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (event.isPublic) PrimaryPurple else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Datum",
                tint = PrimaryPurple,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = event.date, fontSize = 12.sp, color = TextSecondary)
            if (event.time.isNotBlank()) {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = event.time, fontSize = 12.sp, color = TextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (event.description.isNotEmpty()) {
            Text(text = event.description, fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Sudionici",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (event.isPublic) {
                        "${event.joinedBy.size} joined / ${event.participants} spots"
                    } else {
                        "${event.participants} participants"
                    },
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isOwner) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                        border = BorderStroke(1.dp, ErrorRed),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Delete", fontSize = 12.sp, color = ErrorRed)
                    }
                }
                if (event.isPublic && !isOwner && !hasJoined) {
                    Button(
                        onClick = onJoin,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Join", fontSize = 12.sp, color = TextPrimary)
                    }
                }
                Button(
                    onClick = {
                        NotificationHelper.showNewEventNotification(
                            context = context,
                            eventName = event.title,
                            time = event.time
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Reminder", fontSize = 12.sp, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun eventTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryPurple,
    unfocusedBorderColor = SurfaceVariant,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = PrimaryPurple,
    focusedContainerColor = SurfaceVariant,
    unfocusedContainerColor = SurfaceVariant
)

private fun EventModel.belongsToMonth(month: YearMonth): Boolean {
    if (this.month > 0 && this.year > 0) {
        return this.month == month.monthValue && this.year == month.year
    }
    return this.date.endsWith(".${month.monthValue}.${month.year}.")
}
