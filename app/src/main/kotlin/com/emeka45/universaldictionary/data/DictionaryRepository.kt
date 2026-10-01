package com.emeka45.universaldictionary.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.emeka45.universaldictionary.model.Definition
import com.emeka45.universaldictionary.model.DictionaryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.Locale

private val Context.dictionaryDataStore by preferencesDataStore("dictionary_preferences")

class DictionaryRepository(private val context: Context, private val client: OkHttpClient = OkHttpClient()) {
    private val cache = LinkedHashMap<String, DictionaryEntry>(50, 0.75f, true)

    suspend fun lookup(input: String): Result<DictionaryEntry> = withContext(Dispatchers.IO) {
        val word = input.trim().lowercase(Locale.US)
        if (word.isBlank()) return@withContext Result.failure(IllegalArgumentException("Enter a word."))
        synchronized(cache) { cache[word] }?.let { saveHistory(word); return@withContext Result.success(it) }
        OfflineDictionary.get(word)?.let { saveHistory(word); synchronized(cache) { cache[word] = it }; return@withContext Result.success(it) }
        runCatching {
            val encoded = URLEncoder.encode(word, "UTF-8")
            val request = Request.Builder()
                .url("https://api.dictionaryapi.dev/api/v2/entries/en/$encoded")
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("No entry found for \\$word.")
                parse(response.body?.string() ?: "[]").firstOrNull()
                    ?: error("No entry found for \\$word.")
            }.also {
                synchronized(cache) { cache[word] = it }
                saveHistory(word)
            }
        }
    }

    suspend fun suggestions(input: String): List<String> = withContext(Dispatchers.IO) {
        if (input.trim().length < 2) return@withContext emptyList()
        runCatching {
            val encoded = URLEncoder.encode(input.trim(), "UTF-8")
            val request = Request.Builder().url("https://api.datamuse.com/sug?s=$encoded&max=8").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching emptyList()
                val arr = JSONArray(response.body?.string() ?: "[]")
                buildList { for (i in 0 until arr.length()) arr.optJSONObject(i)?.optString("word")?.takeIf(String::isNotBlank)?.let(::add) }
            }
        }.getOrDefault(emptyList())
    }

    suspend fun streak(): Int = context.dictionaryDataStore.data.first()[STREAK] ?: 0
    suspend fun lookupCount(): Int = context.dictionaryDataStore.data.first()[LOOKUPS] ?: 0
    suspend fun clearHistory() { context.dictionaryDataStore.edit { it.remove(HISTORY) } }
    suspend fun clearSaved() { context.dictionaryDataStore.edit { it.remove(SAVED) } }
    fun wordOfTheDay(): String { val words=(OfflineDictionary.words()+listOf("adroit","audacious","candid","cogent","dormant","fortuitous","lucid","nuance","pragmatic","tenacious")).sorted(); return words[(System.currentTimeMillis()/86400000L % words.size).toInt()] }
    suspend fun recordLearning() { val today=System.currentTimeMillis()/86400000L; context.dictionaryDataStore.edit { p -> val last=p[LAST_DAY]; if(last!=today){ val old=p[STREAK]?:0; p[STREAK]=if(last==today-1) old+1 else 1; p[LAST_DAY]=today }; p[LOOKUPS]=(p[LOOKUPS]?:0)+1 } }

    suspend fun savedWords(): Set<String> = context.dictionaryDataStore.data.first()[SAVED].orEmpty()
    suspend fun history(): List<String> = context.dictionaryDataStore.data.first()[HISTORY].orEmpty().toList().asReversed()

    suspend fun toggleSaved(word: String): Set<String> {
        val key = word.lowercase(Locale.US)
        var result = emptySet<String>()
        context.dictionaryDataStore.edit { p ->
            val current = p[SAVED].orEmpty().toMutableSet()
            if (!current.add(key)) current.remove(key)
            p[SAVED] = current
            result = current
        }
        return result
    }

    private suspend fun saveHistory(word: String) {
        context.dictionaryDataStore.edit { p ->
            val h = p[HISTORY].orEmpty().toMutableSet()
            h.remove(word)
            h.add(word)
            p[HISTORY] = h.takeLast(30).toSet()
        }
    }

    private fun parse(json: String): List<DictionaryEntry> {
        val root = JSONArray(json)
        return buildList {
            for (i in 0 until root.length()) {
                val item = root.getJSONObject(i)
                val phonetics = item.optJSONArray("phonetics")
                var phonetic = item.optString("phonetic").takeIf { it.isNotBlank() }
                var audio: String? = null
                if (phonetics != null) for (p in 0 until phonetics.length()) {
                    val ph = phonetics.getJSONObject(p)
                    if (phonetic == null) phonetic = ph.optString("text").takeIf { it.isNotBlank() }
                    if (audio == null) audio = ph.optString("audio").takeIf { it.isNotBlank() }
                }
                val meanings = item.optJSONArray("meanings")
                val defs = mutableListOf<Definition>()
                if (meanings != null) for (m in 0 until meanings.length()) {
                    val meaning = meanings.getJSONObject(m)
                    val pos = meaning.optString("partOfSpeech", "definition")
                    val ds = meaning.optJSONArray("definitions") ?: continue
                    for (d in 0 until ds.length()) {
                        val def = ds.getJSONObject(d)
                        defs += Definition(
                            pos,
                            def.optString("definition"),
                            def.optString("example").takeIf { it.isNotBlank() },
                            strings(def.optJSONArray("synonyms")),
                            strings(def.optJSONArray("antonyms")),
                            "Free Dictionary API"
                        )
                    }
                }
                add(DictionaryEntry(
                    item.optString("word"),
                    phonetic,
                    audio?.let { if (it.startsWith("//")) "https:$it" else it },
                    item.optString("origin").takeIf { it.isNotBlank() },
                    defs,
                    "Free Dictionary API"
                ))
            }
        }
    }

    private fun strings(a: JSONArray?): List<String> =
        if (a == null) emptyList() else buildList { for (i in 0 until a.length()) add(a.optString(i)) }

    companion object {
        private val SAVED = stringSetPreferencesKey("saved_words")
        private val HISTORY = stringSetPreferencesKey("history")
        private val STREAK = intPreferencesKey("streak")
        private val LOOKUPS = intPreferencesKey("lookups")
        private val LAST_DAY = longPreferencesKey("last_day")
    }
}
