package com.gamebot.app.data

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreProvider {
    private const val DATABASE_ID = "default"

    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(DATABASE_ID)
    }
}
