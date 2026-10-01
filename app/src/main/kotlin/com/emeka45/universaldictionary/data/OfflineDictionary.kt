package com.emeka45.universaldictionary.data

import com.emeka45.universaldictionary.model.Definition
import com.emeka45.universaldictionary.model.DictionaryEntry

object OfflineDictionary {
    private val entries = mapOf(
        "abate" to e("abate","To become less intense, or to make something less intense.","verb","decrease,diminish,lessen","increase"),
        "benevolent" to e("benevolent","Kind, generous, and inclined to help others.","adjective","kind,charitable,generous","cruel"),
        "clarify" to e("clarify","To make something easier to understand.","verb","explain,elucidate","confuse"),
        "diligent" to e("diligent","Showing careful and persistent effort.","adjective","careful,industrious","careless"),
        "eloquent" to e("eloquent","Fluent or persuasive in speaking or writing.","adjective","expressive,articulate","inarticulate"),
        "ephemeral" to e("ephemeral","Lasting for a very short time.","adjective","fleeting,brief,transient","lasting"),
        "frugal" to e("frugal","Careful about spending money or using resources.","adjective","economical,thrifty","wasteful"),
        "gregarious" to e("gregarious","Fond of company and sociable.","adjective","sociable,outgoing","reserved"),
        "integrity" to e("integrity","The quality of being honest and having strong moral principles.","noun","honesty,uprightness","dishonesty"),
        "meticulous" to e("meticulous","Very careful and precise about details.","adjective","careful,thorough,precise","careless"),
        "resilient" to e("resilient","Able to recover quickly from difficulty.","adjective","tough,adaptable","fragile"),
        "ubiquitous" to e("ubiquitous","Present, appearing, or found everywhere.","adjective","omnipresent,pervasive","rare"),
        "veracity" to e("veracity","Conformity to truth or accuracy.","noun","truthfulness,accuracy","falsehood"),
        "wisdom" to e("wisdom","The ability to use knowledge and experience to make sound judgments.","noun","insight,prudence","foolishness")
    )
    fun get(word:String):DictionaryEntry?=entries[word.trim().lowercase()]
    fun words():Set<String>=entries.keys
    private fun e(w:String,t:String,p:String,s:String,a:String)=DictionaryEntry(w,definitions=listOf(Definition(p,t,synonyms=s.split(','),antonyms=a.split(','),source="Universal Dictionary Offline Core")),source="Universal Dictionary Offline Core")
}
