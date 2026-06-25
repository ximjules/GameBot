package com.gamebot.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserGamesRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("games")

    suspend fun getGames(): List<GameModel> {
        return try {
            val uid = auth.currentUser?.uid
            android.util.Log.d("GameRepo", "getGames UID: $uid")
            val result = getUserGamesRef().get().await()
            android.util.Log.d("GameRepo", "getGames count: ${result.documents.size}")
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
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "getGames error: ${e.message}")
            emptyList()
        }
    }

    suspend fun addGame(game: GameModel): Boolean {
        return try {
            val uid = auth.currentUser?.uid
            android.util.Log.d("GameRepo", "addGame UID: $uid")
            val gameMap = hashMapOf(
                "title" to game.title,
                "genre" to game.genre,
                "studio" to game.studio,
                "progress" to game.progress,
                "category" to game.category,
                "coverUrl" to game.coverUrl,
                "addedAt" to game.addedAt
            )
            getUserGamesRef().add(gameMap)
            android.util.Log.d("GameRepo", "Igra poslana u Firestore!")
            true
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "addGame error: ${e.message}")
            false
        }
    }

    suspend fun updateGame(game: GameModel): Boolean {
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
            getUserGamesRef().document(game.id).set(gameMap)
            android.util.Log.d("GameRepo", "Igra ažurirana!")
            true
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "updateGame error: ${e.message}")
            false
        }
    }

    suspend fun deleteGame(gameId: String): Boolean {
        return try {
            getUserGamesRef().document(gameId).delete()
            android.util.Log.d("GameRepo", "Igra obrisana!")
            true
        } catch (e: Exception) {
            android.util.Log.e("GameRepo", "deleteGame error: ${e.message}")
            false
        }
    }
}