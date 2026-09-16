package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CoupleDatabase
import com.example.data.model.CalendarEvent
import com.example.data.model.JournalEntry
import com.example.data.model.PingMessage
import com.example.data.model.PhotoMemory
import com.example.data.repository.CoupleRepository
import com.example.data.security.CoupleEncryption
import com.example.ui.sound.RomanticSounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class CoupleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CoupleRepository

    // Global Reactive States from Room
    val allJournals: StateFlow<List<JournalEntry>>
    val pinnedJournals: StateFlow<List<JournalEntry>>
    val allEvents: StateFlow<List<CalendarEvent>>
    val allPings: StateFlow<List<PingMessage>>
    val allPhotoMemoriesDesc: StateFlow<List<PhotoMemory>>
    val allPhotoMemoriesAsc: StateFlow<List<PhotoMemory>>

    // Device/Session States (for Couples Simulation)
    var currentPartner = mutableStateOf("Alex") // Toggles between "Alex" and "Taylor"
    var themeMode = mutableStateOf("Romantic Rose") // "Romantic Rose", "Sunset Amber", "Midnight Sky", "Emerald Garden"
    var isE2EEnabled = mutableStateOf(true) // Toggle viewing raw Base64 database records vs decrypted keys
    var showNotificationBanner = mutableStateOf<String?>(null) // Real-time overlay simulation

    init {
        val database = CoupleDatabase.getDatabase(application)
        repository = CoupleRepository(
            journalDao = database.journalDao(),
            calendarDao = database.calendarDao(),
            pingDao = database.pingDao(),
            photoMemoryDao = database.photoMemoryDao()
        )

        allJournals = repository.journals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        pinnedJournals = repository.pinnedJournals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allEvents = repository.calendarEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allPings = repository.pings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allPhotoMemoriesDesc = repository.photoMemoriesDesc.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allPhotoMemoriesAsc = repository.photoMemoriesAsc.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial data if DB is completely empty
        viewModelScope.launch {
            seedInitialData()
        }
    }

    // SIMULATED COUPLING ACTIONS
    fun togglePartner() {
        currentPartner.value = if (currentPartner.value == "Alex") "Taylor" else "Alex"
    }

    fun changeTheme(newTheme: String) {
        themeMode.value = newTheme
    }

    fun toggleE2E() {
        isE2EEnabled.value = !isE2EEnabled.value
    }

    // JOURNALING
    fun addJournalEntry(title: String, description: String, presetImage: String) {
        viewModelScope.launch {
            // Decryptable strings are stored encrypted in the DB
            val encryptedTitle = CoupleEncryption.encrypt(title)
            val encryptedDesc = CoupleEncryption.encrypt(description)

            val entry = JournalEntry(
                title = encryptedTitle,
                description = encryptedDesc,
                timestamp = System.currentTimeMillis(),
                presetImageName = presetImage,
                author = currentPartner.value,
                isPinned = false
            )
            repository.insertJournal(entry)
        }
    }

    fun toggleJournalPin(entry: JournalEntry) {
        viewModelScope.launch {
            repository.updatePinStatus(entry.id, !entry.isPinned)
        }
    }

    fun deleteJournal(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournal(entry)
        }
    }

    // PHOTO MEMORIES
    fun addPhotoMemory(title: String, caption: String, location: String, photoUrl: String, customTimestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val encryptedTitle = CoupleEncryption.encrypt(title)
            val encryptedCaption = CoupleEncryption.encrypt(caption)
            val encryptedLocation = CoupleEncryption.encrypt(location)

            val memory = PhotoMemory(
                title = encryptedTitle,
                caption = encryptedCaption,
                location = encryptedLocation,
                photoUrl = photoUrl,
                timestamp = customTimestamp,
                author = currentPartner.value,
                isFavorite = false
            )
            repository.insertMemory(memory)
        }
    }

    fun deletePhotoMemory(memory: PhotoMemory) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun toggleMemoryFavorite(memory: PhotoMemory) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(memory.id, !memory.isFavorite)
        }
    }

    // CALENDAR / MILESTONES
    fun addCalendarEvent(
        title: String,
        notes: String,
        dateMillis: Long,
        type: String,
        soundName: String
    ) {
        viewModelScope.launch {
            val encryptedTitle = CoupleEncryption.encrypt(title)
            val encryptedNotes = CoupleEncryption.encrypt(notes)

            val event = CalendarEvent(
                title = encryptedTitle,
                description = encryptedNotes,
                dateMillis = dateMillis,
                eventType = type,
                notificationSound = soundName,
                createdBy = currentPartner.value
            )
            repository.insertEvent(event)
        }
    }

    fun deleteCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // WORKDAY PINGS & CHECKS
    fun sendPing(
        messageText: String,
        pingType: String,
        locationLabel: String = "",
        lat: Double = 0.0,
        lng: Double = 0.0,
        sound: String = "Celestial Chime"
    ) {
        viewModelScope.launch {
            // Store the ping. Sender is current partner.
            val ping = PingMessage(
                messageText = messageText,
                timestamp = System.currentTimeMillis(),
                senderName = currentPartner.value,
                pingType = pingType,
                locationLabel = locationLabel,
                locationLatitude = lat,
                locationLongitude = lng,
                soundPlayed = sound
            )
            repository.sendPing(ping)

            // Simulate immediate partner receiving & play the romantic audio chime!
            // When Alex sends a ping, Taylor "hears" it and vice-versa
            RomanticSounds.playSound(sound)
            showNotificationBanner.value = "New ping received from ${currentPartner.value}: \"$messageText\"! 🎧 Playing sound: $sound"
        }
    }

    fun clearPings() {
        viewModelScope.launch {
            repository.clearPings()
        }
    }

    fun updateResponseEmoji(id: Long, emoji: String) {
        viewModelScope.launch {
            repository.updateResponseEmoji(id, emoji)
            
            // Trigger feedback
            val partner = if (currentPartner.value == "Alex") "Taylor" else "Alex"
            RomanticSounds.playSound("Warm Heartbeat")
            showNotificationBanner.value = "You responded with \"$emoji\" to the check-in! $partner can see your status now!"
        }
    }

    // TRIGGER ADORABLE CHEESY JOKES
    private val coupleJokes = listOf(
        "Are you a keyboard? Because you're just my type! ⌨️",
        "Do you have a map? Because I keep getting lost in your eyes. 🗺️",
        "Are you Wi-Fi? Because I'm feeling a really strong connection here! 📶",
        "I'm not a photographer, but I can easily picture us together forever. 📸",
        "Are you made of Copper and Tellurium? Because you're totally Cu-Te! 🧪",
        "If you were a triangle, you'd be acute one! 📐",
        "Do you like raisins? How do you feel about a romantic date tonight? 🍇",
        "Is your name Google? Because you have everything I've been searching for! 🔍",
        "I'm falling for you faster than an uncaught exception! 💻",
        "Our love is like a Kotlin coroutine: perfectly synchronized and never-blocking! ⏳",
        "If you were a fruit, you'd be a fine-apple! 🍍",
        "You must be my compiler, because you make my life come together. 🛠️",
        "Are you carbon-14? Because I really want to date you. 🦕",
        "Are you a high-contrast theme? Because you make my dark world bright! 🌟"
    )

    fun triggerRandomJoke() {
        val joke = coupleJokes.random()
        val authorOfJoke = if (currentPartner.value == "Alex") "Taylor" else "Alex"
        
        viewModelScope.launch {
            // Post a simulated joke from your partner back to you!
            val ping = PingMessage(
                messageText = joke,
                timestamp = System.currentTimeMillis(),
                senderName = authorOfJoke,
                pingType = "JOKE",
                soundPlayed = "Romantic Flutter"
            )
            repository.sendPing(ping)

            // Play romantic joke alert
            RomanticSounds.playSound("Romantic Flutter")
            showNotificationBanner.value = "$authorOfJoke triggered a cheesy romantic joke: \"$joke\""
        }
    }

    // DATABASE SEEDING
    private suspend fun seedInitialData() {
        // Only seed if empty
        repository.journals.collect { journalsList ->
            if (journalsList.isEmpty()) {
                // Seed journals
                repository.insertJournal(
                    JournalEntry(
                        title = CoupleEncryption.encrypt("First Picnic Date! 🧺"),
                        description = CoupleEncryption.encrypt("We sat under the cherry blossoms, ate chocolate strawberries, and talked for six uninterrupted hours. Unforgettable start to us!"),
                        timestamp = System.currentTimeMillis() - 15 * 86400000L, // 15 days ago
                        presetImageName = "Picnic Date",
                        author = "Alex",
                        isPinned = true
                    )
                )

                repository.insertJournal(
                    JournalEntry(
                        title = CoupleEncryption.encrypt("Rainy Movie Night 🍿"),
                        description = CoupleEncryption.encrypt("Stuck indoors while it poured outside. Cooked carbonara pasta and binged vintage mystery films with warm wool blankets."),
                        timestamp = System.currentTimeMillis() - 5 * 86400000L, // 5 days ago
                        presetImageName = "Movie Night",
                        author = "Taylor",
                        isPinned = false
                    )
                )

                // Seed Calendar events
                val cal = Calendar.getInstance()
                cal.set(Calendar.MONTH, Calendar.OCTOBER)
                cal.set(Calendar.DAY_OF_MONTH, 15)
                val anniversaryMillis = cal.timeInMillis

                repository.insertEvent(
                    CalendarEvent(
                        title = CoupleEncryption.encrypt("Our official Anniversary! 👩‍❤️‍👨"),
                        description = CoupleEncryption.encrypt("The day we decided to make our journey together forever. Plan: Rooftop candle-lit bistro reservation."),
                        dateMillis = anniversaryMillis,
                        eventType = "ANNIVERSARY",
                        notificationSound = "Romantic Flutter",
                        createdBy = "Alex"
                    )
                )

                val cal2 = Calendar.getInstance()
                cal2.add(Calendar.DAY_OF_YEAR, 3) // 3 days in future
                repository.insertEvent(
                    CalendarEvent(
                        title = CoupleEncryption.encrypt("Special Date Night: Cooking Class 👨‍🍳"),
                        description = CoupleEncryption.encrypt("Handmade gourmet ravioli challenge group. Bring the nice merlot wine!"),
                        dateMillis = cal2.timeInMillis,
                        eventType = "DATE_NIGHT",
                        notificationSound = "Sparkling Twinkle",
                        createdBy = "Taylor"
                    )
                )

                // Seed some pings
                repository.sendPing(
                    PingMessage(
                        messageText = "Hey! Just wanted to check in. Hope your meetings are going beautifully! Thinking of you. ♥",
                        timestamp = System.currentTimeMillis() - 2 * 3600000L,
                        senderName = "Taylor",
                        pingType = "SWEET_MESSAGE",
                        soundPlayed = "Celestial Chime"
                    )
                )

                repository.sendPing(
                    PingMessage(
                        messageText = "Arrived safely at the downtown workspace! 📍",
                        timestamp = System.currentTimeMillis() - 1 * 3600000L,
                        senderName = "Alex",
                        pingType = "LOCATION",
                        locationLabel = "Downtown Workspace",
                        locationLatitude = 40.7128,
                        locationLongitude = -74.0060,
                        soundPlayed = "Lovable Purr"
                    )
                )
            }
        }

        viewModelScope.launch {
            repository.photoMemoriesDesc.collect { memoriesList ->
                if (memoriesList.isEmpty()) {
                    repository.insertMemory(
                        PhotoMemory(
                            title = CoupleEncryption.encrypt("Rooftop Sunset Cafe ☕"),
                            caption = CoupleEncryption.encrypt("We found this cozy hidden terrace. The sunset over the skyline was pure gold and pink!"),
                            location = CoupleEncryption.encrypt("The Glass Terrace"),
                            photoUrl = "Sunset Dream",
                            timestamp = System.currentTimeMillis() - 8 * 86400000L,
                            author = "Taylor",
                            isFavorite = true
                        )
                    )
                    repository.insertMemory(
                        PhotoMemory(
                            title = CoupleEncryption.encrypt("Weekend Beach Picnic 🌊"),
                            caption = CoupleEncryption.encrypt("The waves were high and the sea breeze was brisk, but the warm tea made everything perfect."),
                            location = CoupleEncryption.encrypt("Sandy Cove Beach"),
                            photoUrl = "Golden Coast",
                            timestamp = System.currentTimeMillis() - 12 * 86400000L,
                            author = "Alex",
                            isFavorite = false
                        )
                    )
                    repository.insertMemory(
                        PhotoMemory(
                            title = CoupleEncryption.encrypt("Warm Coffee & Notebooks 📔"),
                            caption = CoupleEncryption.encrypt("Shared a giant pastry and began sketching ideas out for our next big travel dream!"),
                            location = CoupleEncryption.encrypt("Daily Brew Cafe"),
                            photoUrl = "Midnight Cafe",
                            timestamp = System.currentTimeMillis() - 2 * 86400000L,
                            author = "Taylor",
                            isFavorite = true
                        )
                    )
                }
            }
        }
    }
}
