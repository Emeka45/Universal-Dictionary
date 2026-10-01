package com.emeka45.universaldictionary.data

import com.emeka45.universaldictionary.model.Definition
import com.emeka45.universaldictionary.model.DictionaryEntry

object SpecialistDictionary {
    private data class Item(val category:String,val word:String,val definition:String,val pos:String="noun")
    private val items=listOf(
        Item("Legal","affidavit","A written statement confirmed by oath or affirmation for use as evidence.","noun"),
        Item("Legal","tort","A civil wrong that can give rise to legal liability, apart from breach of contract.","noun"),
        Item("Legal","equity","A body of principles developed to supplement strict legal rules and provide fair remedies.","noun"),
        Item("Legal","injunction","A court order requiring a person to do or refrain from doing a specified act.","noun"),
        Item("Medical","hypertension","Persistently elevated blood pressure that can increase the risk of cardiovascular disease.","noun"),
        Item("Medical","antibiotic","A medicine used to treat certain bacterial infections.","noun"),
        Item("Medical","diagnosis","The identification of a disease or condition from its signs, symptoms, and investigations.","noun"),
        Item("Science","catalyst","A substance or process that increases the rate of a chemical reaction without being consumed.","noun"),
        Item("Science","osmosis","Movement of solvent through a selectively permeable membrane toward a region of higher solute concentration.","noun"),
        Item("Technology","algorithm","A defined sequence of steps for solving a problem or performing a computation.","noun"),
        Item("Technology","encryption","The process of transforming information so that it can be read only by authorized parties.","noun"),
        Item("Business","liquidity","The ease with which an asset can be converted into cash without a substantial loss in value.","noun"),
        Item("Business","revenue","Income generated from ordinary business activities before expenses are deducted.","noun"),
        Item("Academic","hypothesis","A testable proposed explanation for an observation or phenomenon.","noun"),
        Item("Academic","citation","A reference identifying a source used to support or document a piece of work.","noun"),
        Item("Nigerian English","go-slow","A traffic jam or severe traffic congestion.","noun"),
        Item("Nigerian English","long leg","Influential connections used to obtain access, assistance, or an advantage.","noun"),
        Item("Nigerian Pidgin","abeg","A polite or emphatic form of 'please'.","interjection"),
        Item("Nigerian Pidgin","wahala","Trouble, problem, difficulty, or an unwanted situation.","noun"),
        Item("Nigerian Pidgin","dey","A common Pidgin form of 'be/is/are', with meaning determined by context.","verb"),
        Item("Nigerian Pidgin","naija","A familiar short form for Nigeria.","noun")
    )
    private val byWord=items.associateBy{it.word}
    fun get(word:String):DictionaryEntry?=byWord[word.trim().lowercase()]?.let{
        DictionaryEntry(it.word,definitions=listOf(Definition(it.pos,it.definition,source="Universal Dictionary Specialist Core")),source="Universal Dictionary Specialist Core")
    }
    fun categories():List<String> = items.map{it.category}.distinct()
    fun words(category:String):List<String> = items.filter{it.category==category}.map{it.word}
}
