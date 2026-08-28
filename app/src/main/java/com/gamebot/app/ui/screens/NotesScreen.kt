package com.gamebot.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamebot.app.data.NoteModel
import com.gamebot.app.ui.theme.*
import com.gamebot.app.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(viewModel: NotesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<NoteModel?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val tabs = listOf("My notes", "Guides")

    val filteredNotes = uiState.notes.filter { note ->
        val matchesTab = if (selectedTab == 0) !note.isGuide else note.isGuide
        val matchesSearch = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.game.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch
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
                text = "Notes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = { viewModel.loadNotes() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Osvjezi",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New note",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundDark,
            contentColor = PrimaryPurple,
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
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) PrimaryPurple else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = {
                Text("Search notes...", color = TextHint, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Ocisti",
                            tint = TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = SurfaceVariant,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = PrimaryPurple
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
            filteredNotes.isEmpty() -> {
                EmptyNotesState(
                    selectedTab = selectedTab,
                    onAddClick = { showAddDialog = true }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteItem(
                            note = note,
                            onEdit = { editingNote = note },
                            onDelete = { viewModel.deleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        NoteDialog(
            initialNote = null,
            onDismiss = { showAddDialog = false },
            onSave = { note ->
                viewModel.addNote(note)
                showAddDialog = false
            }
        )
    }

    editingNote?.let { note ->
        NoteDialog(
            initialNote = note,
            onDismiss = { editingNote = null },
            onSave = { updatedNote ->
                viewModel.updateNote(updatedNote)
                editingNote = null
            }
        )
    }
}

@Composable
fun EmptyNotesState(selectedTab: Int, onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (selectedTab == 0) Icons.AutoMirrored.Filled.StickyNote2 else Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (selectedTab == 0) "No notes yet" else "No guides yet",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add", color = TextPrimary)
            }
        }
    }
}

@Composable
fun NoteItem(
    note: NoteModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onEdit() }
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
                imageVector = if (note.isGuide) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Filled.StickyNote2,
                contentDescription = "Note",
                tint = PrimaryPurple,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1
            )
            if (note.game.isNotBlank()) {
                Text(
                    text = note.game,
                    fontSize = 11.sp,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            Text(
                text = note.content.ifBlank { "Bez dodatnog teksta" },
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatNoteDate(note.createdAt),
                fontSize = 10.sp,
                color = TextHint
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onEdit() }
                )
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = ErrorRed.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
fun NoteDialog(
    initialNote: NoteModel?,
    onDismiss: () -> Unit,
    onSave: (NoteModel) -> Unit
) {
    var title by remember(initialNote) { mutableStateOf(initialNote?.title ?: "") }
    var game by remember(initialNote) { mutableStateOf(initialNote?.game ?: "") }
    var content by remember(initialNote) { mutableStateOf(initialNote?.content ?: "") }
    var isGuide by remember(initialNote) { mutableStateOf(initialNote?.isGuide ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = if (initialNote == null) "New note" else "Edit note",
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
                    label = { Text("Naslov", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = noteTextFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = game,
                    onValueChange = { game = it },
                    label = { Text("Igra", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = noteTextFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Sadrzaj", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = noteTextFieldColors()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariant)
                        .clickable { isGuide = !isGuide }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isGuide,
                        onCheckedChange = { isGuide = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save as guide", fontSize = 13.sp, color = TextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        NoteModel(
                            id = initialNote?.id ?: "",
                            title = title,
                            game = game,
                            content = content,
                            isGuide = isGuide,
                            createdAt = initialNote?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp)
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
private fun noteTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryPurple,
    unfocusedBorderColor = SurfaceVariant,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = PrimaryPurple,
    focusedContainerColor = SurfaceVariant,
    unfocusedContainerColor = SurfaceVariant
)

private fun formatNoteDate(createdAt: Long): String {
    if (createdAt <= 0L) return ""
    return SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(Date(createdAt))
}
