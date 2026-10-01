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
    private val bundled = BundledDictionary(context)

    suspend fun lookup(input: String): Result<DictionaryEntry> = withContext(Dispatchers.IO) {
        val word = WordSearch.normalize(input)
        if (word.isBlank()) return@withContext Result.failure(IllegalArgumentException("Enter a word."))
        synchronized(cache) { cache[word] }?.let { saveHistory(word); return@withContext Result.success(it) }
        OfflineDictionary.get(word)?.let { saveHistory(word); recordLearning(); synchronized(cache) { cache[word] = it }; return@withContext Result.success(it) }
        ExpandedOfflineDictionary.get(word)?.let { saveHistory(word); recordLearning(); synchronized(cache) { cache[word] = it }; return@withContext Result.success(it) }
        SpecialistDictionary.get(word)?.let { saveHistory(word); recordLearning(); synchronized(cache) { cache[word] = it }; return@withContext Result.success(it) }
        bundled.get(word)?.let { saveHistory(word); recordLearning(); synchronized(cache) { cache[word] = it }; return@withContext Result.success(it) }
        runCatching {
            val encoded = URLEncoder.encode(word, "UTF-8")
            val request = Request.Builder()
                .url("https://api.dictionaryapi.dev/api/v2/entries/en/$encoded")
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) { val near=nearest(word); if(near!=null) error("No entry for \""+word+"\". Did you mean \""+near+"\"?"); error("No entry found for \""+word+"\". Check the spelling.") }
                parse(response.body?.string() ?: "[]").firstOrNull()
                    ?: error("No entry found for \""+word+"\".")
            }.also {
                synchronized(cache) { cache[word] = it }
                saveHistory(word)
                recordLearning()
            }
        }.recoverCatching {
            lookupExpandedOnline(word) ?: run {
                val near = nearest(word)
                if (near != null) error("No entry for \"$word\". Did you mean \"$near\"?")
                error("No entry found for \"$word\". Check the spelling.")
            }
        }.also {
            synchronized(cache) { cache[word] = it }
            saveHistory(word)
            recordLearning()
        }
    }

    private fun lookupExpandedOnline(word:String): DictionaryEntry? {
        val encoded = URLEncoder.encode(word, "UTF-8")
        val request = Request.Builder()
            .url("https://englishdictionaryapi.com/api/v1/words/$encoded")
            .header("Accept", "application/json")
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use null
            parseExpanded(response.body?.string() ?: "{}", word)
        }
    }

    suspend fun suggestions(input: String): List<String> = withContext(Dispatchers.IO) {
        if (input.trim().length < 2) return@withContext emptyList()
        runCatching {
            val local = bundled.suggestions(input)
            if (local.isNotEmpty()) return@runCatching local
            val encoded = URLEncoder.encode(input.trim(), "UTF-8")
            val request = Request.Builder().url("https://api.datamuse.com/sug?s=$encoded&max=8").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching emptyList()
                val arr = JSONArray(response.body?.string() ?: "[]")
                buildList { for (i in 0 until arr.length()) arr.optJSONObject(i)?.optString("word")?.takeIf(String::isNotBlank)?.let(::add) }
            }
        }.getOrElse {
            WordSearch.localEntries(input).map { it.word } +
                (OfflineDictionary.words()+ExpandedOfflineDictionary.words()+SpecialistDictionary.allWords()).filter { it.startsWith(input.trim().lowercase(Locale.US)) }
        }.distinct().take(8)
    }

    suspend fun streak(): Int = context.dictionaryDataStore.data.first()[STREAK] ?: 0
    suspend fun lookupCount(): Int = context.dictionaryDataStore.data.first()[LOOKUPS] ?: 0
    suspend fun clearHistory() { context.dictionaryDataStore.edit { it.remove(HISTORY) } }
    suspend fun clearSaved() { context.dictionaryDataStore.edit { it.remove(SAVED) } }
    suspend fun exportVocabulary(): String = buildString {
        appendLine("# Universal Dictionary Vocabulary")
        appendLine("# Saved words")
        savedWords().sorted().forEach(::appendLine)
        appendLine("# Recent history")
        history().forEach(::appendLine)
    }
    suspend fun importVocabulary(text:String) {
        val words=text.lineSequence().map{it.trim().lowercase(Locale.US)}
            .filter{it.length in 2..40 && it.all{c->c.isLetter() || c=='-' || c=='\''}}.toSet()
        if(words.isEmpty()) return
        context.dictionaryDataStore.edit{p->p[SAVED]=p[SAVED].orEmpty().plus(words)}
    }

    fun wordOfTheDay(): String { val words=(OfflineDictionary.words()+ExpandedOfflineDictionary.words()+SpecialistDictionary.allWords()).distinct().sorted(); return words[(System.currentTimeMillis()/86400000L % words.size).toInt()] }
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
            p[HISTORY] = h.toList().takeLast(30).toSet()
        }
    }

    private fun nearest(word:String):String? { val all=OfflineDictionary.words()+ExpandedOfflineDictionary.words()+SpecialistDictionary.categories().flatMap{SpecialistDictionary.words(it)}+cache.keys; return all.minByOrNull{distance(word,it)}?.takeIf{distance(word,it)<=maxOf(2,word.length/3)} }    private fun distance(a:String,b:String):Int { val d=IntArray(b.length+1){it}; for(i in a.indices){var prev=d[0];d[0]=i+1;for(j in b.indices){val cur=d[j+1];d[j+1]=minOf(d[j+1]+1,d[j]+1,prev+if(a[i]==b[j])0 else 1);prev=cur}};return d[b.length] }    private fun parse(json: String): List<DictionaryEntry> {
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

    private fun parseExpanded(json:String, fallbackWord:String): DictionaryEntry? {
        val root = org.json.JSONObject(json)
        val word = root.optString("word", fallbackWord)
        val pronunciation = root.optJSONObject("pronunciation")
        val groups = root.optJSONArray("partsOfSpeech")
        val defs = mutableListOf<Definition>()
        if (groups != null) for (i in 0 until groups.length()) {
            val g = groups.optJSONObject(i) ?: continue
            val pos = g.optString("partOfSpeech", "definition")
            val senses = g.optJSONArray("senses") ?: continue
            for (j in 0 until senses.length()) {
                val s = senses.optJSONObject(j) ?: continue
                val text = s.optString("definition").takeIf { it.isNotBlank() } ?: continue
                defs += Definition(pos, text, s.optString("example").takeIf { it.isNotBlank() }, source="English Dictionary API")
            }
        }
        if (defs.isEmpty()) return null
        return DictionaryEntry(
            word=word,
            phonetic=pronunciation?.optString("ipa")?.takeIf { it.isNotBlank() },
            audioUrl=pronunciation?.optString("audioUrl")?.takeIf { it.isNotBlank() },
            origin=root.optString("etymology").takeIf { it.isNotBlank() },
            definitions=defs,
            source="English Dictionary API"
        )
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
