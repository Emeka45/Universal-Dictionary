package com.emeka45.universaldictionary.model

data class Definition(
    val partOfSpeech: String,
    val text: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)

data class DictionaryEntry(
    val word: String,
    val phonetic: String? = null,
    val audioUrl: String? = null,
    val origin: String? = null,
    val definitions: List<Definition>,
    val source: String
)

data class SourceDictionary(
    val name: String,
    val description: String,
    val urlFor: (String) -> String
)
