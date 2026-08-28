package com.gamebot.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class CommunityPostModel(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likedBy: List<String> = emptyList(),
    val comments: Int = 0,
    val latestComment: String = ""
) {
    val likes: Int
        get() = likedBy.size
}
class CommunityRepository {
    private val db = FirestoreProvider.db
    private val auth = FirebaseAuth.getInstance()
    private val postsRef = db.collection("communityPosts")

    suspend fun getPosts(): List<CommunityPostModel> {
        return try {
            withTimeoutOrNull(10000) {
                val result = postsRef
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                result.documents.map { doc ->
                    CommunityPostModel(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "Player",
                        content = doc.getString("content") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        likedBy = (doc.get("likedBy") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        comments = (doc.getLong("comments") ?: 0L).toInt(),
                        latestComment = doc.getString("latestComment") ?: ""
                    )
                }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addPost(content: String, username: String, imageUrl: String = ""): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val post = hashMapOf(
                "userId" to uid,
                "username" to username.ifBlank { "Player" },
                "content" to content,
                "imageUrl" to imageUrl,
                "createdAt" to System.currentTimeMillis(),
                "likedBy" to emptyList<String>(),
                "comments" to 0,
                "latestComment" to ""
            )
            withTimeoutOrNull(30000) {
                ensureUserProfile(uid, username)
                postsRef.add(post).await()
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "addPost error: ${e.message}")
            false
        }
    }

    suspend fun toggleLike(post: CommunityPostModel): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val updatedLikes = if (uid in post.likedBy) {
            post.likedBy - uid
        } else {
            post.likedBy + uid
        }

        return try {
            withTimeoutOrNull(30000) {
                postsRef.document(post.id).update("likedBy", updatedLikes).await()
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "toggleLike error: ${e.message}")
            false
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            withTimeoutOrNull(30000) {
                postsRef.document(postId).delete().await()
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "deletePost error: ${e.message}")
            false
        }
    }

    suspend fun addComment(post: CommunityPostModel, username: String, comment: String): Boolean {
        return try {
            withTimeoutOrNull(30000) {
                postsRef.document(post.id).update(
                    mapOf(
                        "comments" to post.comments + 1,
                        "latestComment" to "${username.ifBlank { "Player" }}: ${comment.trim()}"
                    )
                ).await()
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("CommunityRepo", "addComment error: ${e.message}")
            false
        }
    }

    private suspend fun ensureUserProfile(uid: String, username: String) {
        db.collection("users").document(uid).set(
            mapOf(
                "email" to (auth.currentUser?.email ?: ""),
                "username" to username.ifBlank { auth.currentUser?.email?.substringBefore("@") ?: "Player" },
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }
}
