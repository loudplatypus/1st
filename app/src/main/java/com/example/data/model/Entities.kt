package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // Encrypted string
    val description: String, // Encrypted string
    val timestamp: Long,
    val presetImageName: String, // Predefined romantic artwork illustrations
    val author: String, // "Partner A" or "Partner B"
    val isPinned: Boolean = false, // If true, can be displayed on home screen / widget
    val isEncrypted: Boolean = true
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // Encrypted string
    val description: String, // Encrypted string
    val dateMillis: Long,
    val eventType: String, // "ANNIVERSARY", "DATE_NIGHT", "MILESTONE", "REMINDER"
    val notificationSound: String, // Sound style name e.g. "Romantic Flutter", "Warm Heartbeat"
    val isSynced: Boolean = true, // Simulating cloud sync
    val createdBy: String,
    val isEncrypted: Boolean = true
)

@Entity(tableName = "ping_messages")
data class PingMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageText: String, // Pre-set message or custom text
    val timestamp: Long,
    val senderName: String,
    val pingType: String = "SWEET_MESSAGE", // "SWEET_MESSAGE", "CHECK_IN", "LOCATION", "JOKE"
    val isReceived: Boolean = false, // To simulate receipt notification
    val locationLatitude: Double = 0.0,
    val locationLongitude: Double = 0.0,
    val locationLabel: String = "",
    val soundPlayed: String = "Celestial Chime",
    val responseEmoji: String = ""
)

@Entity(tableName = "photo_memories")
data class PhotoMemory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // Encrypted title
    val caption: String, // Encrypted caption
    val location: String, // Encrypted location
    val photoUrl: String, // Paste URL, local file URI, or built-in photography preset label
    val timestamp: Long, // Date of memory
    val author: String, // Who captured it ("Alex" or "Taylor")
    val isFavorite: Boolean = false,
    val isEncrypted: Boolean = true
)

