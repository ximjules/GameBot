package com.gamebot.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamebot.app.ui.theme.*

data class GameEvent(
    val id: Int,
    val title: String,
    val date: String,
    val time: String,
    val description: String,
    val participants: Int,
    val day: Int
)

val dummyEvents = listOf(
    GameEvent(1, "Apex Legends sesija", "15.5.2024", "20:00 - 23:00", "Večernja casual sesija, svi dobrodošli! 🎮", 4, 15),
    GameEvent(2, "Destiny 2 Raid", "17.5.2024", "18:00 - 22:00", "Vault of Glass raid, trebamo 6 igrača", 3, 17),
    GameEvent(3, "FIFA 24 turnir", "20.5.2024", "19:00 - 23:00", "Mini turnir, prijavi se do petka!", 6, 20)
)

@Composable
fun CalendarScreen() {
    var selectedDay by remember { mutableStateOf(15) }
    val currentMonth = "Svibanj 2024."

    val selectedEvents = dummyEvents.filter { it.day == selectedDay }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Događaji",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Dodaj događaj",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Kalendar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            // Mjesec navigacija
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Prethodni",
                    tint = TextSecondary
                )
                Text(
                    text = currentMonth,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Sljedeći",
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dani u tjednu
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

            // Dani u mjesecu
            val days = (1..31).toList()
            val eventDays = dummyEvents.map { it.day }

            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
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
                    // Popuni ostatak tjedna ako je kraći
                    repeat(7 - week.size) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nadolazeći događaji naslov
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedEvents.isEmpty()) "Nadolazeći događaji" else "Događaji — ${selectedDay}.5.2024.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedEvents.isEmpty()) {
            // Nema događaja
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = "Nema događaja",
                        tint = TextHint,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nema događaja ovaj dan",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(selectedEvents) { event ->
                    EventDetailItem(event = event)
                }
            }
        }
    }
}

@Composable
fun EventDetailItem(event: GameEvent) {
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
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Vrijeme",
                tint = PrimaryPurple,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = event.time, fontSize = 12.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = event.description,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
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
                    text = "+${event.participants} sudionika",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Izbriši", fontSize = 12.sp, color = ErrorRed)
                }
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Uredi", fontSize = 12.sp, color = TextPrimary)
                }
            }
        }
    }
}

