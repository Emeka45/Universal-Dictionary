package com.emeka45.universaldictionary

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderHelper {
    private const val CHANNEL="vocabulary_reminders"
    fun enable(context:Context){
        val manager=context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL,"Vocabulary reminders",NotificationManager.IMPORTANCE_DEFAULT))
        val intent=PendingIntent.getBroadcast(context,45,Intent(context,ReminderReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cal=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,20);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1)}
        context.getSystemService(AlarmManager::class.java).setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.timeInMillis,AlarmManager.INTERVAL_DAY,intent)
    }
    fun cancel(context:Context){val intent=PendingIntent.getBroadcast(context,45,Intent(context,ReminderReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);context.getSystemService(AlarmManager::class.java).cancel(intent)}
    internal const val CHANNEL_ID=CHANNEL
}
