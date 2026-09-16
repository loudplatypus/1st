package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.data.model.JournalEntry
import com.example.data.model.CalendarEvent
import com.example.data.model.PingMessage
import com.example.data.model.PhotoMemory
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournals(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedJournals(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntry)

    @Update
    suspend fun updateJournal(entry: JournalEntry)

    @Delete
    suspend fun deleteJournal(entry: JournalEntry)

    @Query("UPDATE journal_entries SET isPinned = :pinned WHERE id = :id")
    suspend fun updatePinStatus(id: Long, pinned: Boolean)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY dateMillis ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)
}

@Dao
interface PingDao {
    @Query("SELECT * FROM ping_messages ORDER BY timestamp DESC")
    fun getAllPings(): Flow<List<PingMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPing(ping: PingMessage)

    @Query("UPDATE ping_messages SET responseEmoji = :emoji WHERE id = :id")
    suspend fun updateResponseEmoji(id: Long, emoji: String)

    @Query("DELETE FROM ping_messages")
    suspend fun clearAllPings()
}

@Dao
interface PhotoMemoryDao {
    @Query("SELECT * FROM photo_memories ORDER BY timestamp DESC")
    fun getAllMemoriesDesc(): Flow<List<PhotoMemory>>

    @Query("SELECT * FROM photo_memories ORDER BY timestamp ASC")
    fun getAllMemoriesAsc(): Flow<List<PhotoMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: PhotoMemory)

    @Delete
    suspend fun deleteMemory(memory: PhotoMemory)

    @Query("UPDATE photo_memories SET isFavorite = :favorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, favorite: Boolean)
}

@Database(
    entities = [JournalEntry::class, CalendarEvent::class, PingMessage::class, PhotoMemory::class],
    version = 3,
    exportSchema = false
)
abstract class CoupleDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun calendarDao(): CalendarDao
    abstract fun pingDao(): PingDao
    abstract fun photoMemoryDao(): PhotoMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: CoupleDatabase? = null

        fun getDatabase(context: Context): CoupleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CoupleDatabase::class.java,
                    "couple_sync_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
