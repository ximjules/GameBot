package com.gamebot.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class EventModel(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val participants: Int = 0,
    val day: Int = 0,
    val userId: String = ""
)

class EventRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserEventsRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("events")

    suspend fun getEvents(): List<EventModel> {
        return try {
            val result = getUserEventsRef().get().await()
            result.documents.map { doc ->
                EventModel(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    date = doc.getString("date") ?: "",
                    time = doc.getString("time") ?: "",
                    description = doc.getString("description") ?: "",
                    participants = (doc.getLong("participants") ?: 0).toInt(),
                    day = (doc.getLong("day") ?: 0).toInt(),
                    userId = doc.getString("userId") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addEvent(event: EventModel) {
        val uid = auth.currentUser?.uid ?: return
        val eventMap = hashMapOf(
            "title" to event.title,
            "date" to event.date,
            "time" to event.time,
            "description" to event.description,
            "participants" to event.participants,
            "day" to event.day,
            "userId" to uid
        )
        getUserEventsRef().add(eventMap)
    }

    fun deleteEvent(eventId: String) {
        getUserEventsRef().document(eventId).delete()
    }
}