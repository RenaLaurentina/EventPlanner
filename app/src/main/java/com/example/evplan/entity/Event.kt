package com.example.evplan.entity

import com.google.firebase.Timestamp
import java.util.Date

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date? = null,        // ubah ke Date?
    val location: String = "",
    val reminder: Boolean = false,
    val createdAt: Date? = null,   // ubah ke Date?
    val userId: String? = null
)
