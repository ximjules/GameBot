package com.gamebot.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class EventModel(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val participants: Int = 0,
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0,
    val userId: String = "",
    val isPublic: Boolean = false,
    val joinedBy: List<String> = emptyList()
)

class EventRepository {
    private val db = FirestoreProvider.db
    private val auth = FirebaseAuth.getInstance()
    var lastError: String? = null
        private set

    private fun getUserEventsRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("events")

    private fun getPublicEventsRef() = db.collection("publicEvents")

    suspend fun getEvents(): List<EventModel> {
        return try {
            val privateEvents = getPrivateEvents()
            val publicEvents = getPublicEvents()
            (privateEvents + publicEvents)
                .distinctBy { event -> "${event.id}-${event.isPublic}" }
                .sortedWith(compareBy<EventModel> { it.year }.thenBy { it.month }.thenBy { it.day })
        } catch (e: Exception) {
            android.util.Log.e("EventRepo", "getEvents error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun getPrivateEvents(): List<EventModel> {
        return try {
            withTimeoutOrNull(10000) {
                getUserEventsRef().get().await().documents.map { it.toEventModel() }
            } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("EventRepo", "getPrivateEvents error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun getPublicEvents(): List<EventModel> {
        return try {
            withTimeoutOrNull(10000) {
                getPublicEventsRef().get().await().documents.map { it.toEventModel() }
            } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("EventRepo", "getPublicEvents error: ${e.message}")
            emptyList()
        }
    }

    suspend fun addEvent(event: EventModel): Boolean {
        lastError = null
        val uid = auth.currentUser?.uid
        if (uid == null) {
            lastError = "No authenticated user."
            return false
        }
        return try {
            val eventMap = hashMapOf(
                "title" to event.title,
                "date" to event.date,
                "time" to event.time,
                "description" to event.description,
                "participants" to event.participants,
                "day" to event.day,
                "month" to event.month,
                "year" to event.year,
                "userId" to uid,
                "isPublic" to event.isPublic,
                "joinedBy" to listOf(uid)
            )
            val saved = withTimeoutOrNull(30000) {
                val document = getUserEventsRef().add(eventMap).await()
                ensureUserProfile(uid)
                if (event.isPublic) {
                    try {
                        getPublicEventsRef().document(document.id).set(eventMap).await()
                    } catch (e: Exception) {
                        android.util.Log.e("EventRepo", "public event copy error", e)
                    }
                }
                true
            } ?: false
            if (!saved) lastError = "Firestore request timed out after 30 seconds."
            saved
        } catch (e: Exception) {
            lastError = e.message ?: e.javaClass.simpleName
            android.util.Log.e("EventRepo", "addEvent error", e)
            false
        }
    }

    suspend fun deleteEvent(eventId: String, isPublic: Boolean = false): Boolean {
        return try {
            withTimeoutOrNull(30000) {
                getUserEventsRef().document(eventId).delete().await()
                if (isPublic) {
                    getPublicEventsRef().document(eventId).delete().await()
                }
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("EventRepo", "deleteEvent error: ${e.message}")
            false
        }
    }

    suspend fun joinPublicEvent(eventId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            withTimeoutOrNull(30000) {
                getPublicEventsRef().document(eventId)
                    .update("joinedBy", FieldValue.arrayUnion(uid))
                    .await()
                true
            } ?: false
        } catch (e: Exception) {
            android.util.Log.e("EventRepo", "joinPublicEvent error: ${e.message}")
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
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }
}
private fun com.google.firebase.firestore.DocumentSnapshot.toEventModel(): EventModel {
    return EventModel(
        id = id,
        title = getString("title") ?: "",
        date = getString("date") ?: "",
        time = getString("time") ?: "",
        description = getString("description") ?: "",
        participants = (getLong("participants") ?: 0).toInt(),
        day = (getLong("day") ?: 0).toInt(),
        month = (getLong("month") ?: 0).toInt(),
        year = (getLong("year") ?: 0).toInt(),
        userId = getString("userId") ?: "",
        isPublic = getBoolean("isPublic") ?: false,
        joinedBy = (get("joinedBy") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    )
}
