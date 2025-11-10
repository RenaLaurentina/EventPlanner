package com.example.evplan.usecases

import com.example.evplan.entity.Event
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class EventUseCase {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- 1. CREATE ---
    /**
     * Menyimpan objek Event baru ke Firestore.
     * @return ID dokumen yang baru dibuat.
     */
    suspend fun createEvent(event: Event): String {
        val eventMap = hashMapOf<String, Any?>(
            "title" to event.title,
            "description" to event.description,
            "date" to event.date?.let { Timestamp(it) }, // konversi Date -> Timestamp
            "location" to event.location,
            "reminder" to event.reminder,
            "createdAt" to Timestamp.now(),
            "userId" to auth.currentUser?.uid
        )

        val documentReference = db.collection("Event")
            .add(eventMap)
            .await()

        return documentReference.id
    }

    // --- 2. READ ALL ---
    suspend fun getEvents(): List<Event> {
        val snapshot = db.collection("Event")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            Event(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                date = doc.getTimestamp("date")?.toDate(),
                location = doc.getString("location") ?: "",
                reminder = doc.getBoolean("reminder") ?: false,
                createdAt = doc.getTimestamp("createdAt")?.toDate(),
                userId = doc.getString("userId")
            )
        }
    }

    // --- 3. READ SINGLE ---
    suspend fun getEventById(eventId: String): Event? {
        val document = db.collection("Event")
            .document(eventId)
            .get()
            .await()

        if (!document.exists()) return null

        return Event(
            id = document.id,
            title = document.getString("title") ?: "",
            description = document.getString("description") ?: "",
            date = document.getTimestamp("date")?.toDate(),
            location = document.getString("location") ?: "",
            reminder = document.getBoolean("reminder") ?: false,
            createdAt = document.getTimestamp("createdAt")?.toDate(),
            userId = document.getString("userId")
        )
    }

    // --- 4. UPDATE ---
    /**
     * Memperbarui data Event berdasarkan ID.
     *
     * IMPORTANT: Firestore .update() tidak menerima nilai null untuk field.
     * Oleh karena itu kita harus membuat Map<String, Any> yang hanya berisi field non-null.
     */
    suspend fun updateEvent(event: Event) {
        require(event.id.isNotEmpty()) { "Event ID tidak boleh kosong untuk pembaruan." }

        // Bangun map awal dengan tipe Any? (bisa null)
        val rawUpdates = mapOf<String, Any?>(
            "title" to event.title,
            "description" to event.description,
            "date" to event.date?.let { Timestamp(it) },
            "location" to event.location,
            "reminder" to event.reminder
        )

        // Hapus entry yang bernilai null — sehingga hasilnya Map<String, Any>
        val updates: Map<String, Any?> = rawUpdates.filterValues { it != null }

        if (updates.isNotEmpty()) {
            db.collection("Event")
                .document(event.id)
                .update(updates)
                .await()
        } else {
            // Tidak ada field untuk diupdate; bisa pilih untuk lempar error atau hanya return
            return
        }
    }

    // --- 5. DELETE ---
    suspend fun deleteEvent(eventId: String) {
        db.collection("Event")
            .document(eventId)
            .delete()
            .await()
    }

}
