package com.emeka45.universaldictionary.data

import com.emeka45.universaldictionary.model.Definition
import com.emeka45.universaldictionary.model.DictionaryEntry

object SpecialistDictionary {
    private data class Item(val category:String,val word:String,val definition:String,val pos:String="noun")
    private val items=listOf(
        // Legal
        Item("Legal","affidavit","A written statement confirmed by oath or affirmation for use as evidence."),
        Item("Legal","tort","A civil wrong that can give rise to legal liability apart from breach of contract."),
        Item("Legal","equity","Principles developed to supplement strict legal rules and provide fair remedies."),
        Item("Legal","injunction","A court order requiring a person to do or refrain from doing a specified act."),
        Item("Legal","estoppel","A rule that can prevent a person from denying a position on which another reasonably relied."),
        Item("Legal","precedent","An earlier judicial decision used as authority in deciding a later case."),
        Item("Legal","jurisdiction","The legal authority of a court or other body to hear and determine a matter."),
        Item("Legal","liability","A legal responsibility or obligation arising from an act, omission, agreement, or rule."),
        Item("Legal","indemnity","A promise to compensate another for specified loss or liability."),
        Item("Legal","negligence","Failure to exercise the reasonable care required in the circumstances."),
        Item("Legal","contract","A legally enforceable agreement between parties."),
        Item("Legal","consideration","Something of legal value exchanged in support of a simple contract."),
        Item("Legal","trust","A legal arrangement in which property is held by a trustee for beneficiaries or a stated purpose."),
        Item("Legal","probate","The legal process for establishing a will and administering a deceased person's estate."),
        Item("Legal","bail","The conditional release of an accused person while proceedings continue."),
        Item("Legal","locus","The place or position relevant to a legal matter."),
        Item("Legal","ratio decidendi","The legal principle necessary to the decision of a court."),
        Item("Legal","obiter","A judicial observation made in a judgment that is not necessary to the decision."),
        Item("Legal","damages","Money awarded by a court to compensate for a legally recognized loss."),
        Item("Legal","remedy","A legal means of enforcing a right or obtaining redress for a wrong."),
        // Medical
        Item("Medical","hypertension","Persistently elevated blood pressure that can increase health risks."),
        Item("Medical","antibiotic","A medicine used to treat certain bacterial infections."),
        Item("Medical","diagnosis","Identification of a disease or condition from clinical evidence."),
        Item("Medical","prognosis","An expected course or outcome of a disease or condition."),
        Item("Medical","anemia","A condition involving insufficient healthy red blood cells or hemoglobin."),
        Item("Medical","diabetes","A group of disorders characterized by persistently elevated blood glucose."),
        Item("Medical","vaccine","A preparation that trains the immune system to recognize a specific pathogen or antigen."),
        Item("Medical","inflammation","A biological response of tissues to injury, infection, or irritation."),
        Item("Medical","symptom","A change in health perceived or reported by a patient."),
        Item("Medical","pathogen","An organism or agent capable of causing disease."),
        Item("Medical","antiseptic","A substance used to reduce microorganisms on living tissue."),
        Item("Medical","analgesic","A medicine used to relieve pain."),
        Item("Medical","dosage","The amount of a medicine administered at one time or over a specified period."),
        Item("Medical","epidemic","A disease occurrence above the expected level in a population or area."),
        // Accounting & Economics
        Item("Accounting","asset","A resource controlled by an entity from which future economic benefits are expected."),
        Item("Accounting","liability","A present obligation expected to result in an outflow of resources."),
        Item("Accounting","equity","The residual interest in assets after liabilities are deducted."),
        Item("Accounting","depreciation","Systematic allocation of the depreciable amount of a long-term asset over its useful life."),
        Item("Accounting","accrual","Recognition of income or expense when it is earned or incurred rather than when cash moves."),
        Item("Accounting","ledger","A record in which financial transactions are classified into accounts."),
        Item("Accounting","audit","A systematic examination of financial information against applicable criteria."),
        Item("Accounting","reconciliation","A process of comparing records and explaining differences between them."),
        Item("Economics","liquidity","The ease with which an asset can be converted into cash without major loss of value."),
        Item("Economics","inflation","A sustained increase in the general level of prices."),
        Item("Economics","deflation","A sustained decrease in the general level of prices."),
        Item("Economics","scarcity","The condition in which available resources are insufficient to satisfy all wants."),
        Item("Economics","elasticity","A measure of how responsive one economic variable is to a change in another."),
        Item("Economics","monopoly","A market structure dominated by a single seller with substantial market power."),
        Item("Economics","revenue","Income received from selling goods or services."),
        // Science
        Item("Chemistry","catalyst","A substance that increases the rate of a chemical reaction without being consumed overall."),
        Item("Chemistry","molecule","A discrete group of atoms held together by chemical bonds."),
        Item("Chemistry","isotope","A form of an element having the same number of protons but a different number of neutrons."),
        Item("Chemistry","oxidation","A chemical process that can involve loss of electrons or increase in oxidation state."),
        Item("Chemistry","solvent","A substance capable of dissolving another substance to form a solution."),
        Item("Physics","momentum","The product of an object's mass and velocity."),
        Item("Physics","velocity","The rate of change of position with direction included."),
        Item("Physics","acceleration","The rate at which velocity changes with time."),
        Item("Physics","friction","A force that opposes relative motion between surfaces or materials."),
        Item("Physics","voltage","Electrical potential difference between two points."),
        Item("Biology","osmosis","Movement of solvent through a selectively permeable membrane toward higher solute concentration."),
        Item("Biology","photosynthesis","A process in which plants and some organisms use light energy to make organic compounds."),
        Item("Biology","mitosis","A form of cell division producing two daughter cells with matching chromosome sets."),
        Item("Biology","enzyme","A biological catalyst that speeds up a biochemical reaction."),
        Item("Biology","ecosystem","A community of organisms interacting with each other and their physical environment."),
        // Technology / Computer Science
        Item("Technology","algorithm","A defined sequence of steps for solving a problem or performing a computation."),
        Item("Technology","encryption","Transformation of information so that access requires an appropriate key or secret."),
        Item("Technology","database","An organized collection of data designed for storage, retrieval, and management."),
        Item("Technology","compiler","Software that translates source code into another form suitable for execution or further processing."),
        Item("Technology","bandwidth","A measure of the capacity of a communication channel to carry data."),
        Item("Technology","latency","The time delay between an event and the corresponding response or data transfer."),
        Item("Technology","protocol","A defined set of rules governing communication or interaction between systems."),
        Item("Technology","cache","A faster storage layer used to retain data likely to be needed again."),
        Item("Technology","API","A defined interface through which software components communicate."),
        Item("Technology","debugging","The process of locating, understanding, and correcting software defects."),
        // Engineering
        Item("Engineering","tensile","Relating to forces that stretch or pull a material."),
        Item("Engineering","torque","A turning effect produced by a force about an axis or point."),
        Item("Engineering","tolerance","An allowable range of variation in a specified dimension or property."),
        Item("Engineering","prototype","An early model built to test design ideas or requirements."),
        Item("Engineering","circuit","A connected path through which electric current can flow."),
        // Academic & Literature
        Item("Academic","hypothesis","A testable proposed explanation for an observation or phenomenon."),
        Item("Academic","citation","A reference identifying a source used to support or document scholarly work."),
        Item("Academic","methodology","A structured account of methods used in a study or investigation."),
        Item("Academic","thesis","A sustained scholarly argument or a dissertation submitted for an academic qualification."),
        Item("Academic","bibliography","A list of sources consulted or cited in a work."),
        Item("Literature","metaphor","A figure of speech that describes one thing in terms of another without a literal comparison."),
        Item("Literature","irony","A contrast between apparent meaning and a different intended or contextual meaning."),
        Item("Literature","alliteration","Repetition of an initial consonant sound in nearby words."),
        Item("Literature","protagonist","A principal character around whom a narrative is organized."),
        Item("Literature","foreshadowing","A narrative device that gives clues about events that may occur later."),
        // Business
        Item("Business","entrepreneur","A person who organizes resources and accepts responsibility for a business venture."),
        Item("Business","profit","The excess of revenue over relevant costs for a period."),
        Item("Business","capital","Money or other resources committed to a business or productive activity."),
        Item("Business","inventory","Goods or materials held for sale, production, or consumption in operations."),
        Item("Business","marketing","Activities involved in understanding, communicating with, and serving a market."),
        // Nigerian English / Pidgin
        Item("Nigerian English","go-slow","A traffic jam or severe traffic congestion."),
        Item("Nigerian English","long leg","Influential connections used to obtain access, assistance, or an advantage."),
        Item("Nigerian English","flash","A brief call made and ended quickly, often as a signal to return the call.","verb"),
        Item("Nigerian English","settle","To resolve a dispute or provide money or another benefit in a context where the meaning depends on local usage.","verb"),
        Item("Nigerian Pidgin","abeg","A polite or emphatic form of 'please'.","interjection"),
        Item("Nigerian Pidgin","wahala","Trouble, problem, difficulty, or an unwanted situation."),
        Item("Nigerian Pidgin","dey","A common Pidgin form of 'be/is/are', with meaning determined by context.","verb"),
        Item("Nigerian Pidgin","naija","A familiar short form for Nigeria."),
        Item("Nigerian Pidgin","chop","To eat; in some contexts, food or eating.","verb"),
        Item("Nigerian Pidgin","yarn","To talk, tell, or chat.","verb"),
        Item("Nigerian Pidgin","pikin","A child or young person."),
        Item("Nigerian Pidgin","sabi","To know, understand, or be skilled at something.","verb"),
        Item("Nigerian Pidgin","japa","To leave or travel away, especially to seek opportunities elsewhere.","verb")
    )
    private val byWord=items.associateBy{it.word}
    fun get(word:String):DictionaryEntry?=byWord[word.trim().lowercase()]?.let{
        DictionaryEntry(it.word,definitions=listOf(Definition(it.pos,it.definition,source="Universal Dictionary Specialist Core")),source="Universal Dictionary Specialist Core")
    }
    fun categories():List<String> = items.map{it.category}.distinct()
    fun words(category:String):List<String> = items.filter{it.category==category}.map{it.word}
    fun allWords():List<String> = items.map{it.word}
}
