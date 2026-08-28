package com.gamebot.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class NoteModel(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val game: String = "",
    val isGuide: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class NoteRepository {
    private val db = FirestoreProvider.db
    private val auth = FirebaseAuth.getInstance()

    private fun getUserNotesRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("notes")

    suspend fun getNotes(): List<NoteModel> {
        return try {
            withTimeoutOrNull(10000) {
                val result = getUserNotesRef()
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                result.documents.map { doc ->
                    NoteModel(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        game = doc.getString("game") ?: "",
                        isGuide = doc.getBoolean("isGuide") ?: false,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addNote(note: NoteModel): Boolean {
        return try {
            val noteMap = hashMapOf(
                "title" to note.title,
                "content" to note.content,
                "game" to note.game,
                "isGuide" to note.isGuide,
                "createdAt" to System.currentTimeMillis()
            )
            withTimeoutOrNull(30000) {
                ensureUserProfile(auth.currentUser?.uid ?: return@withTimeoutOrNull false)
                getUserNotesRef().add(noteMap).await()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateNote(note: NoteModel): Boolean {
        return try {
            val noteMap = hashMapOf(
                "title" to note.title,
                "content" to note.content,
                "game" to note.game,
                "isGuide" to note.isGuide,
                "createdAt" to note.createdAt
            )
            withTimeoutOrNull(30000) {
                getUserNotesRef().document(note.id).set(noteMap).await()
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteNote(noteId: String): Boolean {
        return try {
            withTimeoutOrNull(30000) {
                getUserNotesRef().document(noteId).delete().await()
                true
            } ?: false
        } catch (e: Exception) {
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

