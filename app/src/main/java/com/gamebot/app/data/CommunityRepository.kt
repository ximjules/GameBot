package com.gamebot.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

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
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsRef = db.collection("communityPosts")

    suspend fun getPosts(): List<CommunityPostModel> {
        return try {
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
                    likedBy = doc.get("likedBy") as? List<String> ?: emptyList(),
                    comments = (doc.getLong("comments") ?: 0L).toInt(),
                    latestComment = doc.getString("latestComment") ?: ""
                )
            }
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
            postsRef.add(post).await()
            true
        } catch (e: Exception) {
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
            postsRef.document(post.id).update("likedBy", updatedLikes).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            postsRef.document(postId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addComment(post: CommunityPostModel, username: String, comment: String): Boolean {
        return try {
            postsRef.document(post.id).update(
                mapOf(
                    "comments" to post.comments + 1,
                    "latestComment" to "${username.ifBlank { "Player" }}: ${comment.trim()}"
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
