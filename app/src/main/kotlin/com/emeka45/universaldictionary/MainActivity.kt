package com.emeka45.universaldictionary

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emeka45.universaldictionary.data.DictionaryRepository
import com.emeka45.universaldictionary.data.OfflineDictionary
import com.emeka45.universaldictionary.data.SpecialistDictionary
import com.emeka45.universaldictionary.model.DictionaryEntry
import com.emeka45.universaldictionary.ui.UniversalDictionaryTheme
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incoming = if (intent?.action == Intent.ACTION_PROCESS_TEXT) intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()?.trim() else null
        setContent { UniversalDictionaryTheme { DictionaryApp(incoming) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DictionaryApp(incoming: String?) {
    val context=LocalContext.current
    val repo=remember{DictionaryRepository(context.applicationContext)}
    val scope=rememberCoroutineScope()
    var tab by remember{mutableIntStateOf(0)}
    var query by remember{mutableStateOf(incoming.orEmpty())}
    var entry by remember{mutableStateOf<DictionaryEntry?>(null)}
    var loading by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf<String?>(null)}
    var suggestions by remember{mutableStateOf(emptyList<String>())}
    var saved by remember{mutableStateOf(emptySet<String>())}
    var history by remember{mutableStateOf(emptyList<String>())}
    var streak by remember{mutableIntStateOf(0)}
    var lookups by remember{mutableIntStateOf(0)}
    val tts=remember{TextToSpeech(context){it}}
    DisposableEffect(Unit){onDispose{tts.shutdown()}}
    suspend fun refresh(){saved=repo.savedWords();history=repo.history();streak=repo.streak();lookups=repo.lookupCount()}
    fun lookup(word:String=query){if(word.isBlank())return;scope.launch{loading=true;error=null;suggestions=emptyList();repo.lookup(word).onSuccess{entry=it}.onFailure{error=it.message};refresh();loading=false}}
    LaunchedEffect(Unit){refresh();if(!incoming.isNullOrBlank())lookup(incoming)}
    fun copy(e:DictionaryEntry){(context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Universal Dictionary",entryText(e)))}
    fun share(e:DictionaryEntry){context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,entryText(e))},"Share word"))}
    Scaffold(topBar={TopAppBar(title={Column{Text("Universal Dictionary",fontWeight=FontWeight.Bold);Text("Words • Learning • Discovery",style=MaterialTheme.typography.labelSmall)}})},bottomBar={NavigationBar{val labels=listOf("Home","Saved","History","Quiz","More");val icons=listOf(Icons.Default.Home,Icons.Default.Bookmark,Icons.Default.History,Icons.Default.Quiz,Icons.Default.MoreHoriz);labels.forEachIndexed{i,l->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icons[i],null)},label={Text(l)})}}}){
        val padding=PaddingValues(bottom=80.dp)
        when(tab){
            0->HomeScreen(query,{query=it},suggestions,{suggestions=it},loading,error,entry,saved,{w->scope.launch{saved=repo.toggleSaved(w);refresh()}},{lookup()},{lookup(it)},{url->if(url.isNullOrBlank()) tts.speak(entry?.word.orEmpty(),TextToSpeech.QUEUE_FLUSH,null,"dictionary") else playAudio(url)},{e->copy(e)},{e->share(e)},repo,Modifier.padding(padding))
            1->WordListScreen("Saved words","Your personal vocabulary collection",saved.toList().sorted(),{lookup(it);tab=0},{scope.launch{repo.clearSaved();refresh()}},Modifier.padding(padding))
            2->WordListScreen("Search history","Your latest lookups",history,{lookup(it);tab=0},{scope.launch{repo.clearHistory();refresh()}},Modifier.padding(padding))
            3->QuizScreen({lookup(it);tab=0},Modifier.padding(padding))
            else->MoreScreen(streak,lookups,{lookup(it);tab=0},Modifier.padding(padding))
        }
    }
}

@Composable private fun HomeScreen(query:String,onQuery:(String)->Unit,suggestions:List<String>,onSuggestions:(List<String>)->Unit,loading:Boolean,error:String?,entry:DictionaryEntry?,saved:Set<String>,onSave:(String)->Unit,onLookup:()->Unit,onSuggestion:(String)->Unit,onSpeak:(String?)->Unit,onCopy:(DictionaryEntry)->Unit,onShare:(DictionaryEntry)->Unit,repo:DictionaryRepository,modifier:Modifier){
    val scope=rememberCoroutineScope()
    LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{OutlinedTextField(value=query,onValueChange={onQuery(it);if(it.trim().length>=2)scope.launch{onSuggestions(repo.suggestions(it))}},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("Search any word")},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={if(loading)CircularProgressIndicator(Modifier.size(22.dp))else IconButton(onClick=onLookup){Icon(Icons.Default.Search,"Look up")}})}
        if(suggestions.isNotEmpty())item{Card{Column(Modifier.fillMaxWidth()){suggestions.forEach{s->Text(s,Modifier.fillMaxWidth().clickable{onSuggestion(s)}.padding(14.dp))}}}}
        item{WordOfDayCard(repo,onSuggestion)}
        if(entry==null&&error==null)item{WelcomeCard()}
        error?.let{msg->item{OutlinedCard{Column(Modifier.padding(16.dp)){Text(msg,color=MaterialTheme.colorScheme.error,fontWeight=FontWeight.Bold);Text("Try a suggestion or check your connection.")}}}}
        entry?.let{e->item{EntryCard(e,saved.contains(e.word.lowercase()),{onSave(e.word)},{onSpeak(e.audioUrl)},{onCopy(e)},{onShare(e)})}}
    }
}
@Composable private fun WordOfDayCard(repo:DictionaryRepository,onWord:(String)->Unit){val word=remember{repo.wordOfTheDay()};Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.tertiaryContainer)){Column(Modifier.padding(18.dp)){Text("WORD OF THE DAY",style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold);Text(word,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);TextButton(onClick={onWord(word)}){Text("Explore today's word")}}}}
@Composable private fun WelcomeCard(){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Your words, everywhere.",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Definitions, examples, synonyms, antonyms, pronunciation, specialist terms and learning tools.");Text("Select text in another app and choose Universal Dictionary.")}}}
@Composable private fun EntryCard(e:DictionaryEntry,saved:Boolean,onSave:()->Unit,onSpeak:()->Unit,onCopy:()->Unit,onShare:()->Unit){Card(shape=RoundedCornerShape(24.dp)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(e.word,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);e.phonetic?.let{Text(it)}};IconButton(onClick=onSpeak,enabled=e.audioUrl!=null){Icon(Icons.Default.VolumeUp,"Pronunciation")};IconButton(onClick=onSave){Icon(if(saved)Icons.Default.Bookmark else Icons.Default.BookmarkBorder,"Save")}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick=onCopy){Icon(Icons.Default.ContentCopy,null);Spacer(Modifier.width(5.dp));Text("Copy")};OutlinedButton(onClick=onShare){Icon(Icons.Default.Share,null);Spacer(Modifier.width(5.dp));Text("Share")}};e.origin?.let{Text("Origin",fontWeight=FontWeight.Bold);Text(it)};WordForms(e.word);e.definitions.forEachIndexed{i,d->Column{Text((i+1).toString()+". "+d.partOfSpeech,fontWeight=FontWeight.Bold);Text(d.text);d.example?.let{Text("Example: "+it)};if(d.synonyms.isNotEmpty())Text("Synonyms: "+d.synonyms.joinToString(", "));if(d.antonyms.isNotEmpty())Text("Antonyms: "+d.antonyms.joinToString(", "))}};Text("Source: "+e.source,style=MaterialTheme.typography.labelMedium)}}}
@Composable private fun WordForms(word:String){val f=when{word.endsWith("y")&&word.length>2->"Possible plural: "+word.dropLast(1)+"ies";word.endsWith("ing")->"Possible base form: "+word.dropLast(3);word.endsWith("ed")->"Possible base form: "+word.dropLast(2);else->""};if(f.isNotBlank())Text(f,style=MaterialTheme.typography.labelMedium)}
@Composable private fun WordListScreen(title:String,subtitle:String,words:List<String>,onWord:(String)->Unit,onClear:()->Unit,modifier:Modifier){LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(subtitle)}if(words.isNotEmpty())TextButton(onClick=onClear){Text("Clear")}}};if(words.isEmpty())item{WelcomeCard()};items(words.distinct()){w->ListItem(headlineContent={Text(w)},leadingContent={Icon(Icons.Default.Book,null)},modifier=Modifier.clickable{onWord(w)});HorizontalDivider()}}}
@Composable private fun QuizScreen(onWord:(String)->Unit,modifier:Modifier){val pool=remember{OfflineDictionary.words().toList()};var answer by remember{mutableStateOf(pool.first())};var options by remember{mutableStateOf(listOf(answer))};var chosen by remember{mutableStateOf<String?>(null)};fun next(){answer=pool.random();options=(listOf(answer)+pool.filter{it!=answer}.shuffled().take(3)).shuffled();chosen=null};LaunchedEffect(Unit){next()};LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Vocabulary Quiz",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Choose the word matching the definition.");Card{Column(Modifier.padding(20.dp)){Text(OfflineDictionary.get(answer)?.definitions?.firstOrNull()?.text.orEmpty());options.forEach{o->Button(onClick={chosen=o},enabled=chosen==null,modifier=Modifier.fillMaxWidth().padding(vertical=3.dp)){Text(o)}};chosen?.let{Text(if(it==answer)"Correct! 🎉" else "The answer is "+answer,fontWeight=FontWeight.Bold);TextButton(onClick={next}){Text("Next question")};TextButton(onClick={onWord(answer)}){Text("Study word")}}}}}}}
@Composable private fun MoreScreen(streak:Int,lookups:Int,onWord:(String)->Unit,modifier:Modifier){var category by remember{mutableStateOf(SpecialistDictionary.categories().first())};val categories=SpecialistDictionary.categories();LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)){Column(Modifier.padding(20.dp)){Text("Learning dashboard",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(streak.toString()+" day learning streak");Text(lookups.toString()+" lookups recorded")}}};item{Text("Specialist & Nigerian vocabulary",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};items(categories){c->FilterChip(selected=category==c,onClick={category=c},label={Text(c)})};items(SpecialistDictionary.words(category)){w->ListItem(headlineContent={Text(w)},supportingContent={Text(category)},modifier=Modifier.clickable{onWord(w)})};item{OutlinedCard{Column(Modifier.padding(16.dp)){Text("Free/open data policy",fontWeight=FontWeight.Bold);Text("No proprietary dictionary credentials or databases are used. Open resources require compatible licensing and attribution.")}}}}}
private fun entryText(e:DictionaryEntry)=buildString{append(e.word);e.phonetic?.let{append(" ");append(it)};appendLine();e.definitions.forEachIndexed{i,d->{append(i+1);append(". ");append(d.partOfSpeech);append(": ");appendLine(d.text);d.example?.let{append("Example: ");appendLine(it)};if(d.synonyms.isNotEmpty()){append("Synonyms: ");appendLine(d.synonyms.joinToString(", "))};if(d.antonyms.isNotEmpty()){append("Antonyms: ");appendLine(d.antonyms.joinToString(", "))}};append("Source: ");append(e.source)}
private fun playAudio(url:String?){if(url.isNullOrBlank())return;runCatching{MediaPlayer().apply{setDataSource(url);prepareAsync();setOnPreparedListener{it.start()};setOnCompletionListener{it.release()};setOnErrorListener{mp,_,_->mp.release();true}}}}
