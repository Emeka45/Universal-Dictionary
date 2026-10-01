package com.emeka45.universaldictionary.model

data class Definition(
    val partOfSpeech: String,
    val text: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val source: String? = null
)

data class DictionaryEntry(
    val word: String,
    val phonetic: String? = null,
    val audioUrl: String? = null,
    val origin: String? = null,
    val definitions: List<Definition>,
    val source: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val synonyms: List<String> get() = definitions.flatMap { it.synonyms }.distinct()
    val antonyms: List<String> get() = definitions.flatMap { it.antonyms }.distinct()
}

data class WordHistory(val word: String, val timestamp: Long)
