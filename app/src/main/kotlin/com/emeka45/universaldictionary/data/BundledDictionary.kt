package com.emeka45.universaldictionary.data

import android.content.Context
import com.emeka45.universaldictionary.model.Definition
import com.emeka45.universaldictionary.model.DictionaryEntry
import org.json.JSONObject
import java.util.Locale

/**
 * Bundled Open Dictionary reader.
 *
 * The CI build generates app/src/main/assets/dictionary from the public
 * Open Dictionary dataset. Files are partitioned by the first two letters,
 * so a lookup only reads one small JSON shard instead of loading the entire
 * 260k+ entry dictionary into memory.
 */
class BundledDictionary(private val context: Context) {
    private val shardCache = LinkedHashMap<String, JSONObject>(12, 0.75f, true)

    fun get(input: String): DictionaryEntry? {
        val word = input.trim().lowercase(Locale.US)
        if (word.isBlank()) return null
        val definition = loadShard(word)?.optJSONObject(word) ?: return null
        return toEntry(definition, word)
    }

    fun suggestions(input: String, limit: Int = 8): List<String> {
        val prefix = input.trim().lowercase(Locale.US)
        if (prefix.length < 2) return emptyList()
        val shard = loadShard(prefix) ?: return emptyList()
        val result = ArrayList<String>(limit)
        val keys = shard.keys()
        while (keys.hasNext() && result.size < limit) {
            val key = keys.next()
            if (key.startsWith(prefix)) result += key
        }
        return result.sorted()
    }

    fun wordsForWordOfDay(): Sequence<String> = sequence {
        // The UI does not need to enumerate the full dataset. Word-of-day
        // selection remains backed by the curated dictionaries unless a
        // specific shard is requested.
    }

    private fun loadShard(word: String): JSONObject? {
        val first = word.firstOrNull() ?: return null
        val pair = if (word.length == 1) word else word.substring(0, 2)
        val key = "$first/$pair.json"
        synchronized(shardCache) {
            shardCache[key]?.let { return it }
        }
        val json = runCatching {
            context.assets.open("dictionary/$key").bufferedReader().use { JSONObject(it.readText()) }
        }.getOrNull() ?: return null
        synchronized(shardCache) {
            shardCache[key] = json
            while (shardCache.size > 12) shardCache.remove(shardCache.entries.first().key)
        }
        return json
    }

    private fun toEntry(root: JSONObject, fallbackWord: String): DictionaryEntry {
        val definitions = mutableListOf<Definition>()
        val etymologies = root.optJSONArray("etymologies")
        var origin: String? = null

        if (etymologies != null) {
            for (e in 0 until etymologies.length()) {
                val etymology = etymologies.optJSONObject(e) ?: continue
                if (origin == null) {
                    origin = etymology.optString("etymology")
                        .takeIf { it.isNotBlank() }
                }
                val parts = etymology.optJSONArray("partsOfSpeech") ?: continue
                for (p in 0 until parts.length()) {
                    val part = parts.optJSONObject(p) ?: continue
                    val pos = part.optString("partOfSpeech", "Other")
                    val senses = part.optJSONArray("senses") ?: continue
                    for (s in 0 until senses.length()) {
                        val sense = senses.optJSONObject(s) ?: continue
                        val text = sense.optString("sense").trim()
                        if (text.isBlank()) continue
                        val examples = sense.optJSONArray("examples")
                        val example = examples?.optString(0)?.takeIf { it.isNotBlank() }
                        val date = sense.optString("date").takeIf { it.isNotBlank() }
                        val enriched = if (date != null) "$text [$date]" else text
                        definitions += Definition(
                            partOfSpeech = pos,
                            text = enriched,
                            example = example,
                            source = "Open Dictionary (Wiktionary)"
                        )
                    }
                }
            }
        }

        return DictionaryEntry(
            word = root.optString("word", fallbackWord),
            origin = origin,
            definitions = definitions,
            source = "Open Dictionary (Wiktionary)"
        )
    }
}
