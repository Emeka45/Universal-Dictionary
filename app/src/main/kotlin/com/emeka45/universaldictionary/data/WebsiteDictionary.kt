package com.emeka45.universaldictionary.data

import com.emeka45.universaldictionary.model.Definition
import com.emeka45.universaldictionary.model.DictionaryEntry
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale

/**
 * Online source of truth for Universal Dictionary.
 *
 * The website endpoint fronts the same Open Dictionary data used by the
 * dictionary website. The app caches successful lookups and retains small
 * local fallbacks for offline resilience.
 */
class WebsiteDictionary(private val client: OkHttpClient = OkHttpClient()) {
    companion object {
        private const val BASE_URL = "https://emeka45.github.io/api/dictionary"
    }

    fun get(input: String): DictionaryEntry? {
        val word = input.trim().lowercase(Locale.US)
        if (word.isBlank()) return null
        val encoded = URLEncoder.encode(word, "UTF-8")
        val request = Request.Builder()
            .url("$BASE_URL?word=$encoded")
            .header("Accept", "application/json")
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use null
            parse(response.body?.string().orEmpty(), word)
        }
    }

    fun suggestions(input: String, limit: Int = 8): List<String> {
        val prefix = input.trim().lowercase(Locale.US)
        if (prefix.length < 2) return emptyList()
        val encoded = URLEncoder.encode(prefix, "UTF-8")
        val request = Request.Builder()
            .url("$BASE_URL?suggest=$encoded&limit=$limit")
            .header("Accept", "application/json")
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use emptyList()
            val root = JSONObject(response.body?.string().orEmpty())
            val values = root.optJSONArray("suggestions") ?: return@use emptyList()
            buildList {
                for (i in 0 until values.length()) {
                    values.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
    }

    private fun parse(json: String, fallbackWord: String): DictionaryEntry? {
        val root = runCatching { JSONObject(json) }.getOrNull() ?: return null
        val definitions = mutableListOf<Definition>()
        var origin: String? = null
        val etymologies = root.optJSONArray("etymologies") ?: return null

        for (e in 0 until etymologies.length()) {
            val etymology = etymologies.optJSONObject(e) ?: continue
            if (origin == null) {
                origin = etymology.optString("etymology").takeIf { it.isNotBlank() }
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
                        source = "Universal Dictionary website · Open Dictionary (Wiktionary)"
                    )
                }
            }
        }

        if (definitions.isEmpty()) return null
        return DictionaryEntry(
            word = root.optString("word", fallbackWord),
            origin = origin,
            definitions = definitions,
            source = "Universal Dictionary website"
        )
    }
}
