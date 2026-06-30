package com.gamebot.app.ui.screens

import android.content.Context
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gamebot.app.data.CommunityPostModel
import com.gamebot.app.ui.theme.*
import com.gamebot.app.viewmodel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityScreen(
    onAddSetupClick: () -> Unit = {},
    viewModel: CommunityViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("gamebot_prefs", Context.MODE_PRIVATE)
    val username = prefs.getString("username", "Player") ?: "Player"
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showNewPostDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Objave", "Moje")
    val visiblePosts = if (selectedTab == 0) {
        uiState.posts
    } else {
        uiState.posts.filter { it.userId == viewModel.currentUserId }
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
                text = "Community",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = { viewModel.loadPosts() },
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
                    onClick = { showNewPostDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Nova objava",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onAddSetupClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Dodaj setup",
                        tint = TextSecondary,
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

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
            visiblePosts.isEmpty() -> {
                EmptyCommunityState(
                    text = if (selectedTab == 0) "Jos nema objava" else "Jos nemas svojih objava",
                    onNewPostClick = { showNewPostDialog = true }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(visiblePosts, key = { it.id }) { post ->
                        CommunityPostItem(
                            post = post,
                            currentUserId = viewModel.currentUserId,
                            onLikeClick = { viewModel.toggleLike(post) },
                            onDeleteClick = { viewModel.deletePost(post.id) },
                            onCommentSend = { comment -> viewModel.addComment(post, username, comment) }
                        )
                    }
                }
            }
        }
    }

    if (showNewPostDialog) {
        AddCommunityPostDialog(
            onDismiss = { showNewPostDialog = false },
            onPost = { content ->
                viewModel.addPost(content, username)
                showNewPostDialog = false
            }
        )
    }
}

@Composable
fun EmptyCommunityState(text: String, onNewPostClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = text, fontSize = 14.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNewPostClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Napravi objavu", color = TextPrimary)
            }
        }
    }
}

@Composable
fun CommunityPostItem(
    post: CommunityPostModel,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCommentSend: (String) -> Unit
) {
    val liked = currentUserId in post.likedBy
    val isOwner = currentUserId == post.userId
    var showCommentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.username.firstOrNull()?.uppercaseChar()?.toString() ?: "P",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPurple
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.username,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = formatPostTime(post.createdAt),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            if (isOwner) {
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Obrisi objavu",
                        tint = ErrorRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = post.content, fontSize = 14.sp, color = TextPrimary)

        if (post.imageUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Slika objave",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (liked) PrimaryPurple else TextSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onLikeClick() }
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "${post.likes}", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Komentari",
                tint = TextSecondary,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { showCommentDialog = true }
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "${post.comments}", fontSize = 13.sp, color = TextSecondary)
        }

        if (post.latestComment.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = post.latestComment,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant)
                    .padding(10.dp)
            )
        }
    }

    if (showCommentDialog) {
        AddCommentDialog(
            onDismiss = { showCommentDialog = false },
            onSend = { comment ->
                onCommentSend(comment)
                showCommentDialog = false
            }
        )
    }
}

@Composable
fun AddCommunityPostDialog(
    onDismiss: () -> Unit,
    onPost: (String) -> Unit
) {
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "Nova objava",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = { Text("Podijeli gaming trenutak...", color = TextHint) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = SurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onPost(content) },
                enabled = content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Objavi", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Odustani", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AddCommentDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "Komentar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Napisi komentar...", color = TextHint) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = SurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryPurple,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSend(comment) },
                enabled = comment.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Posalji", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Odustani", color = TextSecondary)
            }
        }
    )
}

private fun formatPostTime(createdAt: Long): String {
    if (createdAt <= 0L) return "maloprije"
    val diff = System.currentTimeMillis() - createdAt
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diff < minute -> "maloprije"
        diff < hour -> "prije ${diff / minute} min"
        diff < day -> "prije ${diff / hour} h"
        diff < 7 * day -> "prije ${diff / day} d"
        else -> SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(Date(createdAt))
    }
}
