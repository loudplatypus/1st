package com.example.data.repository

import com.example.data.database.JournalDao
import com.example.data.database.CalendarDao
import com.example.data.database.PingDao
import com.example.data.database.PhotoMemoryDao
import com.example.data.model.JournalEntry
import com.example.data.model.CalendarEvent
import com.example.data.model.PingMessage
import com.example.data.model.PhotoMemory
import kotlinx.coroutines.flow.Flow

class CoupleRepository(
    private val journalDao: JournalDao,
    private val calendarDao: CalendarDao,
    private val pingDao: PingDao,
    private val photoMemoryDao: PhotoMemoryDao
) {
    // Streams of data
    val journals: Flow<List<JournalEntry>> = journalDao.getAllJournals()
    val pinnedJournals: Flow<List<JournalEntry>> = journalDao.getPinnedJournals()
    val calendarEvents: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()
    val pings: Flow<List<PingMessage>> = pingDao.getAllPings()
    val photoMemoriesDesc: Flow<List<PhotoMemory>> = photoMemoryDao.getAllMemoriesDesc()
    val photoMemoriesAsc: Flow<List<PhotoMemory>> = photoMemoryDao.getAllMemoriesAsc()

    // Journal Actions
    suspend fun insertJournal(entry: JournalEntry) {
        journalDao.insertJournal(entry)
    }

    suspend fun updateJournal(entry: JournalEntry) {
        journalDao.updateJournal(entry)
    }

    suspend fun deleteJournal(entry: JournalEntry) {
        journalDao.deleteJournal(entry)
    }

    suspend fun updatePinStatus(id: Long, isPinned: Boolean) {
        journalDao.updatePinStatus(id, isPinned)
    }

    // Calendar Actions
    suspend fun insertEvent(event: CalendarEvent) {
        calendarDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: CalendarEvent) {
        calendarDao.deleteEvent(event)
    }

    // Ping Actions
    suspend fun sendPing(ping: PingMessage) {
        pingDao.insertPing(ping)
    }

    suspend fun updateResponseEmoji(id: Long, emoji: String) {
        pingDao.updateResponseEmoji(id, emoji)
    }

    suspend fun clearPings() {
        pingDao.clearAllPings()
    }

    // Photo Memories Actions
    suspend fun insertMemory(memory: PhotoMemory) {
        photoMemoryDao.insertMemory(memory)
    }

    suspend fun deleteMemory(memory: PhotoMemory) {
        photoMemoryDao.deleteMemory(memory)
    }

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) {
        photoMemoryDao.updateFavoriteStatus(id, isFavorite)
    }
}
