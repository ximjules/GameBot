package com.gamebot.app.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gamebot.app.R
import com.gamebot.app.data.EventModel
import com.gamebot.app.ui.theme.*
import com.gamebot.app.viewmodel.HomeUiState
import com.gamebot.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onEventClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onCommunityClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = context.getSharedPreferences("gamebot_prefs", Context.MODE_PRIVATE)
    val username = prefs.getString("username", "PlayerOne") ?: "PlayerOne"

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadHomeData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "GameBot Logo",
                    modifier = Modifier.height(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GAMEBOT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifikacije",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome back,",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "$username!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Image(
                painter = painterResource(id = R.drawable.astrobot),
                contentDescription = "GameBot maskota",
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> LoadingHomeCard()
            uiState.currentlyPlaying != null -> CurrentlyPlayingCard(game = uiState.currentlyPlaying!!)
            else -> EmptyCurrentGameCard()
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(value = uiState.totalGames.toString(), label = "Games")
            VerticalDivider(modifier = Modifier.height(30.dp), color = SurfaceVariant)
            StatItem(value = uiState.backlogCount.toString(), label = "Backlog")
            VerticalDivider(modifier = Modifier.height(30.dp), color = SurfaceVariant)
            StatItem(value = uiState.completedCount.toString(), label = "Completed")
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Next steps")
        Spacer(modifier = Modifier.height(12.dp))
        HomeNextSteps(
            uiState = uiState,
            onLibraryClick = onLibraryClick,
            onEventClick = onEventClick,
            onCommunityClick = onCommunityClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Upcoming events")
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.upcomingEvents.isEmpty()) {
            EmptyEventsCard(onClick = onEventClick)
        } else {
            uiState.upcomingEvents.forEach { event ->
                EventItem(event = event, onClick = onEventClick)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LoadingHomeCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun CurrentlyPlayingCard(game: com.gamebot.app.data.GameModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (game.coverUrl.isNotEmpty()) {
                AsyncImage(
                    model = game.coverUrl,
                    contentDescription = "Cover art",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.title.firstOrNull()?.toString() ?: "?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Latest added game", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                if (game.genre.isNotEmpty()) {
                    Text(text = game.genre, fontSize = 11.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { game.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryPurple,
                    trackColor = SurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(game.progress * 100).toInt()}% completed",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyCurrentGameCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "No games added yet", fontSize = 14.sp, color = TextSecondary)
            Text(text = "Add your first game in Library", fontSize = 12.sp, color = PrimaryPurple)
        }
    }
}

@Composable
private fun HomeNextSteps(
    uiState: HomeUiState,
    onLibraryClick: () -> Unit,
    onEventClick: () -> Unit,
    onCommunityClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NextStepCard(
            icon = Icons.Default.VideoLibrary,
            title = if (uiState.totalGames == 0) "Add your first game" else "Edit library",
            description = if (uiState.totalGames == 0) {
                "Search for a game through the RAWG API and save it to your profile."
            } else {
                "Update progress, category, or details for saved games."
            },
            value = uiState.totalGames.toString(),
            label = "GAMES",
            onClick = onLibraryClick
        )
        NextStepCard(
            icon = Icons.Default.EventAvailable,
            title = if (uiState.upcomingEventCount == 0) "Plan a gaming event" else "Open calendar",
            description = if (uiState.upcomingEventCount == 0) {
                "Add the date, time, and title for your schedule."
            } else {
                "You have ${uiState.upcomingEventCount} upcoming events in the calendar."
            },
            value = uiState.upcomingEventCount.toString(),
            label = "EVENT",
            onClick = onEventClick
        )
        NextStepCard(
            icon = Icons.Default.People,
            title = "Share an update",
            description = "Share an opinion, gaming moment, or setup in the Community feed.",
            value = uiState.communityPostCount.toString(),
            label = "POST",
            onClick = onCommunityClick
        )
    }
}

@Composable
private fun NextStepCard(
    icon: ImageVector,
    title: String,
    description: String,
    value: String,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = description, fontSize = 11.sp, color = TextSecondary, maxLines = 2)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(PrimaryPurple.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun EmptyEventsCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "No upcoming events", fontSize = 14.sp, color = TextSecondary)
            Text(text = "Add an event in Calendar", fontSize = 12.sp, color = PrimaryPurple)
        }
    }
}

@Composable
fun EventItem(
    event: EventModel,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = listOf(event.date, event.time).filter { it.isNotBlank() }.joinToString(" • "),
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryPurple.copy(alpha = 0.2f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "+${event.participants}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
        }
    }
}
