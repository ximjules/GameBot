package com.gamebot.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamebot.app.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

data class Game(
    val title: String,
    val genre: String,
    val studio: String,
    val progress: Float,
    val category: String
)

val dummyGames = listOf(
    Game("Elden Ring", "RPG", "FromSoftware", 0.45f, "backlog"),
    Game("Red Dead Redemption 2", "Action", "Rockstar", 0.80f, "playing"),
    Game("Horizon Forbidden West", "Action RPG", "Guerrilla", 1.0f, "completed"),
    Game("Cyberpunk 2077", "RPG", "CD Projekt Red", 0.60f, "playing"),
    Game("Ghost of Tsushima", "Action", "Sucker Punch", 1.0f, "completed"),
    Game("God of War Ragnarök", "Action RPG", "Santa Monica", 0.68f, "playing"),
    Game("The Witcher 3", "RPG", "CD Projekt Red", 1.0f, "completed"),
    Game("Starfield", "RPG", "Bethesda", 0.20f, "backlog"),
    Game("Spider-Man 2", "Action", "Insomniac", 0.90f, "playing"),
    Game("Baldur's Gate 3", "RPG", "Larian Studios", 0.35f, "backlog")
)

@Composable
fun LibraryScreen(onGameClick: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Sve igre", "Odigrane", "Trenutno igram", "Backlog")

    val filteredGames = when (selectedTab) {
        0 -> dummyGames
        1 -> dummyGames.filter { it.category == "completed" }
        2 -> dummyGames.filter { it.category == "playing" }
        3 -> dummyGames.filter { it.category == "backlog" }
        else -> dummyGames
    }

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
                text = "Moja biblioteka",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row {
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
        }

        // Tabovi
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundDark,
            contentColor = PrimaryPurple,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryPurple
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) PrimaryPurple else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lista igara
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredGames) { game ->
                GameItem(game = game, onClick = onGameClick)
            }
        }
    }
}

@Composable
fun GameItem(game: Game, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder za cover art
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = game.title.first().toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${game.genre} • ${game.studio}",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { game.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = PrimaryPurple,
                trackColor = SurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "${(game.progress * 100).toInt()}%",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurple
        )
    }
}