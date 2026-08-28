package com.gamebot.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class GameModel(
    val id: String = "",
    val title: String = "",
    val genre: String = "",
    val studio: String = "",
    val progress: Float = 0f,
    val category: String = "backlog",
    val coverUrl: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

class GameRepository {
    private val db = FirestoreProvider.db
    private val auth = FirebaseAuth.getInstance()

    private fun getUserGamesRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("games")

    suspend fun getGames(): List<GameModel> {
        return try {
            withTimeoutOrNull(10000) {
                val result = getUserGamesRef().get().await()
                result.documents.map { doc ->
                    GameModel(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        genre = doc.getString("genre") ?: "",
                        studio = doc.getString("studio") ?: "",
                        progress = (doc.getDouble("progress") ?: 0.0).toFloat(),
                        category = doc.getString("category") ?: "backlog",
                        coverUrl = doc.getString("coverUrl") ?: "",
                        addedAt = doc.getLong("addedAt") ?: 0L
                    )
                }
            } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "getGames error: ${e.message}")
            emptyList()
        }
    }

    suspend fun addGame(game: GameModel): Boolean {
        auth.currentUser?.uid ?: return false
        return try {
            val gameMap = hashMapOf(
                "title" to game.title,
                "genre" to game.genre,
                "studio" to game.studio,
                "progress" to game.progress,
                "category" to game.category,
                "coverUrl" to game.coverUrl,
                "addedAt" to game.addedAt
            )
            withTimeoutOrNull(30000) {
                ensureUserProfile(auth.currentUser?.uid ?: return@withTimeoutOrNull false)
                getUserGamesRef().add(gameMap).await()
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "addGame error: ${e.message}")
            false
        }
    }

    suspend fun updateGame(game: GameModel): Boolean {
        if (game.id.isBlank()) return false
        return try {
            val gameMap = hashMapOf(
                "title" to game.title,
                "genre" to game.genre,
                "studio" to game.studio,
                "progress" to game.progress,
                "category" to game.category,
                "coverUrl" to game.coverUrl,
                "addedAt" to game.addedAt
            )
            withTimeoutOrNull(30000) {
                getUserGamesRef().document(game.id).set(gameMap).await()
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "updateGame error: ${e.message}")
            false
        }
    }

    suspend fun deleteGame(gameId: String): Boolean {
        if (gameId.isBlank()) return false
        return try {
            withTimeoutOrNull(30000) {
                getUserGamesRef().document(gameId).delete().await()
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "deleteGame error: ${e.message}")
            false
        }
    }

    private suspend fun ensureUserProfile(uid: String) {
        db.collection("users").document(uid).set(
            mapOf(
                "email" to (auth.currentUser?.email ?: ""),
                "username" to (auth.currentUser?.email?.substringBefore("@") ?: "Player"),
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }
}
