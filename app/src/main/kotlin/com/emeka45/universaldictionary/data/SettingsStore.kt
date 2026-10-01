package com.emeka45.universaldictionary.data

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs=context.getSharedPreferences("dictionary_settings",Context.MODE_PRIVATE)
    fun darkMode():Boolean=prefs.getBoolean("dark_mode",false)
    fun largeText():Boolean=prefs.getBoolean("large_text",false)
    fun setDarkMode(value:Boolean){prefs.edit().putBoolean("dark_mode",value).apply()}
    fun setLargeText(value:Boolean){prefs.edit().putBoolean("large_text",value).apply()}
}
