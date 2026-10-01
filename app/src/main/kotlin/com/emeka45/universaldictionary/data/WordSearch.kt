package com.emeka45.universaldictionary.data

import com.emeka45.universaldictionary.model.DictionaryEntry
import java.util.Locale

object WordSearch {
    fun normalize(input:String):String = input.trim().lowercase(Locale.US).replace(Regex("\\s+")," ")
    fun candidates(input:String):List<String> {
        val w=normalize(input)
        if(w.isBlank()) return emptyList()
        val out=linkedSetOf(w)
        if(w.endsWith("ies")&&w.length>4) out += w.dropLast(3)+"y"
        if(w.endsWith("ves")&&w.length>4) { out += w.dropLast(3)+"f"; out += w.dropLast(3)+"fe" }
        if(w.endsWith("ing")&&w.length>5) { out += w.dropLast(3); out += w.dropLast(3)+"e" }
        if(w.endsWith("ed")&&w.length>4) { out += w.dropLast(2); out += w.dropLast(1) }
        if(w.endsWith("es")&&w.length>4) out += w.dropLast(2)
        if(w.endsWith("s")&&w.length>3) out += w.dropLast(1)
        return out.toList()
    }
    fun localEntries(query:String):List<DictionaryEntry> {
        val q=normalize(query)
        val all=(OfflineDictionary.words().mapNotNull(OfflineDictionary::get)+
            SpecialistDictionary.allWords().mapNotNull(SpecialistDictionary::get)).distinctBy{it.word}
        return all.filter { e ->
            val hay=(e.word+" "+e.definitions.joinToString(" "){d->d.text+" "+d.synonyms.joinToString(" ")}).lowercase(Locale.US)
            hay.contains(q)
        }.sortedBy { if(it.word.equals(q,true)) 0 else if(it.word.startsWith(q)) 1 else 2 }
    }
}
