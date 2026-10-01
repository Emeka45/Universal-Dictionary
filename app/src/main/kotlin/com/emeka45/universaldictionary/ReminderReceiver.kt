package com.emeka45.universaldictionary

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.emeka45.universaldictionary.data.DictionaryRepository
import kotlinx.coroutines.runBlocking

class ReminderReceiver:BroadcastReceiver(){
    override fun onReceive(context:Context,intent:Intent?){
        val repo=DictionaryRepository(context.applicationContext)
        val word=repo.wordOfTheDay()
        val notification=NotificationCompat.Builder(context,ReminderHelper.CHANNEL_ID)
            .setSmallIcon(com.emeka45.universaldictionary.R.mipmap.ic_launcher)
            .setContentTitle("Universal Dictionary")
            .setContentText("Today's word: $word")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(45,notification)
    }
}
