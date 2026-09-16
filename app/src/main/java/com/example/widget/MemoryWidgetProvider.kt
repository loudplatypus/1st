package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.CoupleDatabase
import com.example.data.security.CoupleEncryption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MemoryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val goMainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            goMainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Run coroutine to fetch latest pinned memory from Room in a simple background thread
        val db = CoupleDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pinnedList = db.journalDao().getPinnedJournals().first()
                val latestPinnedEntry = pinnedList.firstOrNull()

                // Resolve decrypted or placeholder content
                val title = latestPinnedEntry?.let { entry ->
                    CoupleEncryption.decrypt(entry.title)
                } ?: "No Pinned Moments"

                val desc = latestPinnedEntry?.let { entry ->
                    CoupleEncryption.decrypt(entry.description)
                } ?: "Pin an active visual memory to showcase it here! ♥"

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.memory_widget_layout)
                    views.setTextViewText(R.id.widget_title, title)
                    views.setTextViewText(R.id.widget_desc, desc)
                    views.setOnClickPendingIntent(R.id.text_container, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                // Fail-safe default fallback
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.memory_widget_layout)
                    views.setTextViewText(R.id.widget_title, "Our Love Journey")
                    views.setTextViewText(R.id.widget_desc, "Tap to synchronize pings!")
                    views.setOnClickPendingIntent(R.id.text_container, pendingIntent)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
