package com.gamebot.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gamebot.app.data.GameModel
import com.gamebot.app.data.api.RawgApi
import com.gamebot.app.data.api.RawgGame
import com.gamebot.app.ui.theme.*
import com.gamebot.app.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    onGameClick: () -> Unit = {},
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingGame by remember { mutableStateOf<GameModel?>(null) }
    val tabs = listOf("All games", "Completed", "Playing", "Backlog")

    val filteredGames = when (selectedTab) {
        0 -> uiState.games
        1 -> uiState.games.filter { it.category == "completed" }
        2 -> uiState.games.filter { it.category == "playing" }
        3 -> uiState.games.filter { it.category == "backlog" }
        else -> uiState.games
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
                text = "My library",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .clickable { showAddDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add game",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

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

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (filteredGames.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = TextHint,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No games yet - add your first one!", fontSize = 14.sp, color = TextSecondary)
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
                items(filteredGames) { game ->
                    GameItem(
                        game = game,
                        onClick = onGameClick,
                        onEdit = { editingGame = game },
                        onDelete = { viewModel.deleteGame(game.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddGameDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { game ->
                viewModel.addGame(game)
                showAddDialog = false
            }
        )
    }

    editingGame?.let { game ->
        EditGameDialog(
            game = game,
            onDismiss = { editingGame = null },
            onSave = { updatedGame ->
                viewModel.updateGame(updatedGame)
                editingGame = null
            }
        )
    }
}

@Composable
fun GameItem(
    game: GameModel,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (game.coverUrl.isNotEmpty()) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = game.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.title.firstOrNull()?.toString() ?: "?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = game.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = listOf(game.genre, game.studio).filter { it.isNotBlank() }.joinToString(" - "),
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
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

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${(game.progress * 100).toInt()}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = TextSecondary,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onEdit() }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete",
                tint = ErrorRed.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
fun EditGameDialog(
    game: GameModel,
    onDismiss: () -> Unit,
    onSave: (GameModel) -> Unit
) {
    var title by remember(game.id) { mutableStateOf(game.title) }
    var genre by remember(game.id) { mutableStateOf(game.genre) }
    var studio by remember(game.id) { mutableStateOf(game.studio) }
    var coverUrl by remember(game.id) { mutableStateOf(game.coverUrl) }
    var progress by remember(game.id) { mutableStateOf((game.progress * 100).toInt().toString()) }
    var selectedCategory by remember(game.id) { mutableStateOf(game.category) }
    val categories = listOf("backlog", "playing", "completed")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(text = "Edit game", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameTextField(value = title, onValueChange = { title = it }, label = "Game title")
                GameTextField(value = genre, onValueChange = { genre = it }, label = "Genre")
                GameTextField(value = studio, onValueChange = { studio = it }, label = "Studio")
                GameTextField(value = coverUrl, onValueChange = { coverUrl = it }, label = "Cover URL")
                GameTextField(
                    value = progress,
                    onValueChange = { progress = it },
                    label = "Progress (0-100)",
                    keyboardType = KeyboardType.Number
                )
                CategoryPicker(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    categories = categories
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        game.copy(
                            title = title.trim(),
                            genre = genre.trim(),
                            studio = studio.trim(),
                            coverUrl = coverUrl.trim(),
                            progress = progress.percentToProgress(),
                            category = selectedCategory
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp),
                enabled = title.isNotBlank()
            ) {
                Text("Save", color = TextPrimary)
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
fun AddGameDialog(
    onDismiss: () -> Unit,
    onAdd: (GameModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RawgGame>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<RawgGame?>(null) }
    var progress by remember { mutableStateOf("0") }
    var selectedCategory by remember { mutableStateOf("backlog") }
    val categories = listOf("backlog", "playing", "completed")
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(text = "Add game", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        selectedGame = null
                        if (query.length >= 3) {
                            scope.launch {
                                isSearching = true
                                try {
                                    val response = RawgApi.service.searchGames(
                                        apiKey = RawgApi.API_KEY,
                                        query = query
                                    )
                                    searchResults = response.results
                                } catch (e: Exception) {
                                    android.util.Log.e("RAWG", "Error: ${e.message}")
                                }
                                isSearching = false
                            }
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    label = { Text("Search game...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = gameTextFieldColors(),
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = PrimaryPurple,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    singleLine = true
                )

                if (searchResults.isNotEmpty() && selectedGame == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariant)
                    ) {
                        searchResults.take(5).forEach { game ->
                            SearchResultItem(
                                game = game,
                                onSelect = {
                                    selectedGame = game
                                    searchQuery = game.name
                                    searchResults = emptyList()
                                }
                            )
                        }
                    }
                }

                selectedGame?.let { game ->
                    SelectedGamePreview(
                        game = game,
                        onRemove = {
                            selectedGame = null
                            searchQuery = ""
                        }
                    )
                }

                GameTextField(
                    value = progress,
                    onValueChange = { progress = it },
                    label = "Progress (0-100)",
                    keyboardType = KeyboardType.Number
                )
                CategoryPicker(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    categories = categories
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pickedGame = selectedGame
                    if (pickedGame != null) {
                        onAdd(
                            GameModel(
                                title = pickedGame.name,
                                genre = pickedGame.genres?.take(2)?.joinToString(", ") { it.name } ?: "",
                                studio = pickedGame.developers?.firstOrNull()?.name ?: "",
                                progress = progress.percentToProgress(),
                                category = selectedCategory,
                                coverUrl = pickedGame.background_image ?: ""
                            )
                        )
                    } else if (searchQuery.isNotBlank()) {
                        onAdd(
                            GameModel(
                                title = searchQuery.trim(),
                                progress = progress.percentToProgress(),
                                category = selectedCategory
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp),
                enabled = searchQuery.isNotBlank()
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
private fun SearchResultItem(game: RawgGame, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (game.background_image != null) {
            AsyncImage(
                model = game.background_image,
                contentDescription = game.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.name.firstOrNull()?.toString() ?: "?",
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = game.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = game.genres?.take(2)?.joinToString(", ") { it.name } ?: "Unknown",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SelectedGamePreview(game: RawgGame, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariant)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (game.background_image != null) {
            AsyncImage(
                model = game.background_image,
                contentDescription = game.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = game.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                text = game.genres?.take(2)?.joinToString(", ") { it.name } ?: "",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                text = game.developers?.firstOrNull()?.name ?: "",
                fontSize = 11.sp,
                color = PrimaryPurple
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CategoryPicker(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categories: List<String>
) {
    Text(text = "Category", fontSize = 13.sp, color = TextSecondary)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = when (category) {
                            "backlog" -> "Backlog"
                            "playing" -> "Playing"
                            "completed" -> "Completed"
                            else -> category
                        },
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryPurple,
                    selectedLabelColor = TextPrimary,
                    containerColor = SurfaceVariant,
                    labelColor = TextSecondary
                )
            )
        }
    }
}

@Composable
private fun GameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = gameTextFieldColors(),
        singleLine = true
    )
}

@Composable
private fun gameTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryPurple,
    unfocusedBorderColor = SurfaceVariant,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = PrimaryPurple,
    focusedContainerColor = SurfaceVariant,
    unfocusedContainerColor = SurfaceVariant
)

private fun String.percentToProgress(): Float {
    return ((toFloatOrNull() ?: 0f) / 100f).coerceIn(0f, 1f)
}
