package com.emeka45.universaldictionary.data

import com.emeka45.universaldictionary.model.Definition
import com.emeka45.universaldictionary.model.DictionaryEntry

object OfflineDictionary {
    private data class Item(val word:String,val pos:String,val definition:String,val synonyms:List<String> = emptyList(),val antonyms:List<String> = emptyList())
    private val items=listOf(
        Item("abate","verb","To become less intense or to make something less intense.",listOf("decrease","subside"),listOf("intensify")),
        Item("benevolent","adjective","Kindly disposed toward others and willing to do good.",listOf("kind","generous"),listOf("cruel")),
        Item("clarify","verb","To make an idea, statement, or situation easier to understand.",listOf("explain","illuminate"),listOf("confuse")),
        Item("diligent","adjective","Showing steady and careful effort in carrying out a task.",listOf("industrious","assiduous"),listOf("negligent")),
        Item("eloquent","adjective","Able to express ideas clearly and persuasively.",listOf("expressive","articulate"),listOf("inarticulate")),
        Item("ephemeral","adjective","Existing or popular for only a short time.",listOf("fleeting","transient"),listOf("lasting")),
        Item("frugal","adjective","Careful about spending and avoiding unnecessary waste.",listOf("thrifty","economical"),listOf("wasteful")),
        Item("gregarious","adjective","Fond of company and social interaction.",listOf("sociable","outgoing"),listOf("reserved")),
        Item("integrity","noun","Consistency between stated principles and honest conduct.",listOf("honesty","uprightness"),listOf("dishonesty")),
        Item("meticulous","adjective","Very careful about accuracy and small details.",listOf("thorough","precise"),listOf("careless")),
        Item("resilient","adjective","Able to recover from difficulty or adapt after stress.",listOf("adaptable","durable"),listOf("fragile")),
        Item("ubiquitous","adjective","Present, found, or encountered in many places at once.",listOf("pervasive","widespread"),listOf("rare")),
        Item("veracity","noun","Conformity to truth or accuracy.",listOf("truthfulness","accuracy"),listOf("falsehood")),
        Item("wisdom","noun","Good judgment based on knowledge, experience, and reflection.",listOf("prudence","insight"),listOf("folly")),
        Item("adroit","adjective","Skillful and effective, especially in handling a difficult task.",listOf("skilful","dexterous"),listOf("clumsy")),
        Item("audacious","adjective","Willing to take bold risks or act with unusual confidence.",listOf("bold","daring"),listOf("timid")),
        Item("candid","adjective","Truthful and direct without deliberate concealment.",listOf("frank","open"),listOf("evasive")),
        Item("cogent","adjective","Clear, logical, and convincing.",listOf("compelling","persuasive"),listOf("weak")),
        Item("dormant","adjective","Temporarily inactive but capable of becoming active later.",listOf("inactive","latent"),listOf("active")),
        Item("fortuitous","adjective","Happening by chance, often with a beneficial result.",listOf("chance","lucky")),
        Item("lucid","adjective","Clear and easy to understand.",listOf("clear","intelligible"),listOf("obscure")),
        Item("nuance","noun","A subtle distinction or variation in meaning or expression.",listOf("subtlety","shade")),
        Item("pragmatic","adjective","Focused on practical consequences and workable solutions.",listOf("practical","realistic"),listOf("idealistic")),
        Item("tenacious","adjective","Persistent and unwilling to give up easily.",listOf("persistent","determined"),listOf("yielding")),
        Item("adapt","verb","To change behavior or form so as to suit new conditions.",listOf("adjust","modify")),
        Item("coherent","adjective","Logical, orderly, and connected in a way that makes sense.",listOf("logical","consistent"),listOf("incoherent")),
        Item("concise","adjective","Expressing much information clearly in few words.",listOf("brief","succinct"),listOf("verbose")),
        Item("credible","adjective","Believable or worthy of trust.",listOf("believable","reliable"),listOf("unreliable")),
        Item("discreet","adjective","Careful to avoid attracting attention or revealing sensitive information.",listOf("tactful","prudent"),listOf("indiscreet")),
        Item("empirical","adjective","Based on observation, measurement, or experience rather than theory alone.",listOf("observational")),
        Item("feasible","adjective","Possible and practical to accomplish.",listOf("practicable","viable"),listOf("impossible")),
        Item("formidable","adjective","Inspiring respect or concern because of strength, size, or difficulty.",listOf("daunting","powerful")),
        Item("impartial","adjective","Treating relevant sides fairly without improper preference.",listOf("neutral","fair"),listOf("biased")),
        Item("innovate","verb","To introduce a new idea, method, or product.",listOf("invent","modernize")),
        Item("judicious","adjective","Showing sound judgment and careful consideration.",listOf("sensible","wise"),listOf("reckless")),
        Item("obsolete","adjective","No longer useful or current because something newer has replaced it.",listOf("outdated"),listOf("current")),
        Item("plausible","adjective","Reasonable enough to be believed or accepted as possible.",listOf("credible","believable"),listOf("implausible")),
        Item("profound","adjective","Very deep in meaning, thought, or effect.",listOf("deep","significant"),listOf("superficial")),
        Item("scrutinize","verb","To examine something closely and critically.",listOf("inspect","examine")),
        Item("substantiate","verb","To support a claim with evidence or proof.",listOf("verify","support")),
        Item("transient","adjective","Lasting only a short time.",listOf("temporary","brief"),listOf("permanent")),
        Item("validate","verb","To establish that something is sound, accurate, or acceptable.",listOf("confirm","verify"),listOf("invalidate")),
        Item("versatile","adjective","Able to adapt to many uses, tasks, or situations.",listOf("adaptable","flexible"),listOf("inflexible")),
        Item("vigilant","adjective","Alert and watchful for possible danger or problems.",listOf("watchful","alert"),listOf("careless")),
        Item("ambiguous","adjective","Open to more than one reasonable interpretation.",listOf("unclear","equivocal"),listOf("unambiguous")),
        Item("arbitrary","adjective","Based on personal choice or preference rather than a stated principle.",listOf("random","capricious")),
        Item("comprehensive","adjective","Including all or nearly all relevant parts or aspects.",listOf("complete","thorough"),listOf("limited")),
        Item("conundrum","noun","A difficult problem or question with no obvious answer.",listOf("puzzle","riddle")),
        Item("disparate","adjective","Fundamentally different in kind or character.",listOf("different","unlike")),
        Item("exemplary","adjective","Serving as a strong example of a desired quality or standard.",listOf("model","outstanding")),
        Item("intricate","adjective","Containing many connected details or complex parts.",listOf("complex","elaborate"),listOf("simple")),
        Item("pertinent","adjective","Directly relevant to the matter being considered.",listOf("relevant","apposite"),listOf("irrelevant")),
        Item("scrupulous","adjective","Extremely careful about correctness, honesty, or detail.",listOf("conscientious","careful"),listOf("careless")),
        Item("synthesize","verb","To combine separate ideas or elements into a coherent whole.",listOf("combine","integrate"))
    )
    private val byWord=items.associateBy{it.word}
    fun get(word:String):DictionaryEntry?=byWord[word.trim().lowercase()]?.let{
        DictionaryEntry(it.word,definitions=listOf(Definition(it.pos,it.definition,synonyms=it.synonyms,antonyms=it.antonyms,source="Universal Dictionary Offline Core")),source="Universal Dictionary Offline Core")
    }
    fun words():List<String> = items.map{it.word}
}
