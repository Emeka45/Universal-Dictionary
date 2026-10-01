package com.emeka45.universaldictionary

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.emeka45.universaldictionary.data.SettingsStore
import com.emeka45.universaldictionary.model.DictionaryEntry
import com.emeka45.universaldictionary.ui.UniversalDictionaryTheme
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incoming = if (intent?.action == Intent.ACTION_PROCESS_TEXT) intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()?.trim() else null
        setContent { DictionarySettingsHost(incoming) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DictionarySettingsHost(incoming:String?) {
    val context=LocalContext.current
    val settings=remember{SettingsStore(context.applicationContext)}
    var dark by remember{mutableStateOf(settings.darkMode())}
    var large by remember{mutableStateOf(settings.largeText())}
    UniversalDictionaryTheme(darkOverride=dark,largeText=large){
        DictionaryApp(incoming,{dark=it;settings.setDarkMode(it)},{large=it;settings.setLargeText(it)})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DictionaryApp(incoming: String?, onDarkMode:(Boolean)->Unit, onLargeText:(Boolean)->Unit) {
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
            else->MoreScreen(streak,lookups,{lookup(it);tab=0},Modifier.padding(padding),onDarkMode,onLargeText)
        }
    }
}

@Composable private fun HomeScreen(query:String,onQuery:(String)->Unit,suggestions:List<String>,onSuggestions:(List<String>)->Unit,loading:Boolean,error:String?,entry:DictionaryEntry?,saved:Set<String>,onSave:(String)->Unit,onLookup:()->Unit,onSuggestion:(String)->Unit,onSpeak:(String?)->Unit,onCopy:(DictionaryEntry)->Unit,onShare:(DictionaryEntry)->Unit,repo:DictionaryRepository,modifier:Modifier){
    val scroll=rememberScrollState()
    val categories=remember{SpecialistDictionary.categories().take(8)}
    LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        item{
            Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
                Text("Find the right word.",style=MaterialTheme.typography.headlineLarge)
                Text("Definitions, pronunciation, specialist terms and learning tools.",color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item{
            OutlinedTextField(
                value=query,onValueChange=onQuery,modifier=Modifier.fillMaxWidth(),singleLine=true,
                shape=RoundedCornerShape(22.dp),placeholder={Text("Search a word, term or phrase")},
                leadingIcon={Icon(Icons.Default.Search,"Search")},
                trailingIcon={Row(verticalAlignment=Alignment.CenterVertically){
                    if(query.isNotBlank())IconButton(onClick={onQuery("")}){Icon(Icons.Default.Close,"Clear")}
                    if(loading)CircularProgressIndicator(Modifier.size(22.dp))else IconButton(onClick=onLookup){Icon(Icons.Default.ArrowForward,"Look up")}
                }}
            )
        }
        if(suggestions.isNotEmpty())item{
            ElevatedCard{Column(Modifier.padding(vertical=6.dp)){
                Text("Smart suggestions",Modifier.padding(horizontal=16.dp,vertical=8.dp),style=MaterialTheme.typography.labelLarge)
                suggestions.take(8).forEach{s->ListItem(headlineContent={Text(s,fontWeight=FontWeight.SemiBold)},leadingContent={Icon(Icons.Default.Search,null)},modifier=Modifier.clickable{onSuggestion(s)})}
            }}
        }
        item{WordOfDayCard(repo,onSuggestion)}
        item{
            OutlinedCard(shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.LocalFireDepartment,null);Spacer(Modifier.width(8.dp));Text("Learning momentum",fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("${repo.hashCode().let{""}}")}
                Text("Use Quiz and Saved to turn lookups into active vocabulary practice.",style=MaterialTheme.typography.bodySmall)
            }}
        }
        item{Text("Explore by field",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
        item{Row(Modifier.horizontalScroll(scroll),horizontalArrangement=Arrangement.spacedBy(8.dp)){categories.forEach{category->AssistChip(onClick={SpecialistDictionary.words(category).firstOrNull()?.let(onSuggestion)},label={Text(category)},leadingIcon={Icon(Icons.Default.AutoStories,null)})}}}
        error?.let{msg->item{OutlinedCard{Column(Modifier.padding(16.dp)){Text("We couldn't find that word",fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.error);Text(msg);Text("Check spelling or choose a smart suggestion.")}}}}
        if(entry==null&&error==null)item{WelcomeCard()}
        entry?.let{e->item{EntryCard(e,saved.contains(e.word.lowercase()),{onSave(e.word)},{onSpeak(e.audioUrl)},{onCopy(e)},{onShare(e)})}}
    }
}@Composable private fun WordOfDayCard(repo:DictionaryRepository,onWord:(String)->Unit){val word=remember{repo.wordOfTheDay()};Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.tertiaryContainer)){Column(Modifier.padding(18.dp)){Text("WORD OF THE DAY",style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold);Text(word,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);TextButton(onClick={onWord(word)}){Text("Explore today's word")}}}}
@Composable private fun WelcomeCard(){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Your words, everywhere.",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Definitions, examples, synonyms, antonyms, pronunciation, specialist terms and learning tools.");Text("Select text in another app and choose Universal Dictionary.")}}}
@Composable private fun EntryCard(e:DictionaryEntry,saved:Boolean,onSave:()->Unit,onSpeak:()->Unit,onCopy:()->Unit,onShare:()->Unit){
    Card(shape=RoundedCornerShape(28.dp)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(15.dp)){
        Row(verticalAlignment=Alignment.Top){
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){Text(e.word,style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold);e.phonetic?.let{Text(it,color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.titleMedium)};Text(e.definitions.firstOrNull()?.partOfSpeech?:"word",style=MaterialTheme.typography.labelLarge)}
            IconButton(onClick=onSpeak){Icon(Icons.Default.VolumeUp,"Pronounce")}
            IconButton(onClick=onSave){Icon(if(saved)Icons.Default.Bookmark else Icons.Default.BookmarkBorder,"Save")}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilledTonalButton(onClick=onSpeak){Icon(Icons.Default.RecordVoiceOver,null);Spacer(Modifier.width(5.dp));Text("Listen")};OutlinedButton(onClick=onCopy){Icon(Icons.Default.ContentCopy,null);Spacer(Modifier.width(5.dp));Text("Copy")};OutlinedButton(onClick=onShare){Icon(Icons.Default.Share,null);Spacer(Modifier.width(5.dp));Text("Share")}}
        e.origin?.let{Text("Origin",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text(it)}
        WordForms(e.word)
        e.definitions.forEachIndexed{i,d->Column(verticalArrangement=Arrangement.spacedBy(7.dp)){
            Text("${i+1}. ${d.partOfSpeech}",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
            Text(d.text,style=MaterialTheme.typography.bodyLarge)
            d.example?.let{Surface(color=MaterialTheme.colorScheme.surfaceContainer,shape=RoundedCornerShape(16.dp)){Text("“${it}”",Modifier.padding(13.dp))}}
            if(d.synonyms.isNotEmpty()){Text("Synonyms",fontWeight=FontWeight.SemiBold);Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){d.synonyms.distinct().take(10).forEach{AssistChip(onClick={},label={Text(it)})}}}
            if(d.antonyms.isNotEmpty()){Text("Antonyms",fontWeight=FontWeight.SemiBold);Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){d.antonyms.distinct().take(10).forEach{AssistChip(onClick={},label={Text(it)})}}}
        }}
        Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(13.dp)){Text("Word intelligence",fontWeight=FontWeight.Bold);Text("Related words, word forms and usage clues are grouped here to help you learn beyond a single definition.",style=MaterialTheme.typography.bodySmall)}}
        Text("Source: ${e.source}",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }}
}@Composable private fun WordForms(word:String){val f=when{word.endsWith("y")&&word.length>2->"Possible plural: "+word.dropLast(1)+"ies";word.endsWith("ing")->"Possible base form: "+word.dropLast(3);word.endsWith("ed")->"Possible base form: "+word.dropLast(2);else->""};if(f.isNotBlank())Text(f,style=MaterialTheme.typography.labelMedium)}
@Composable private fun WordListScreen(title:String,subtitle:String,words:List<String>,onWord:(String)->Unit,onClear:()->Unit,modifier:Modifier){LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(subtitle)}if(words.isNotEmpty())TextButton(onClick=onClear){Text("Clear")}}};if(words.isEmpty())item{WelcomeCard()};items(words.distinct()){w->ListItem(headlineContent={Text(w)},leadingContent={Icon(Icons.Default.Book,null)},modifier=Modifier.clickable{onWord(w)});HorizontalDivider()}}}
@Composable private fun QuizScreen(onWord:(String)->Unit,modifier:Modifier){
    val pool=remember{OfflineDictionary.words().mapNotNull(OfflineDictionary::get)}
    var mode by remember{mutableStateOf("Definition")}
    var answer by remember{mutableStateOf(pool.first())}
    var options by remember{mutableStateOf(emptyList<String>())}
    var chosen by remember{mutableStateOf<String?>(null)}
    var score by remember{mutableIntStateOf(0)}
    var answered by remember{mutableIntStateOf(0)}
    fun correct():String=when(mode){"Synonym"->answer.synonyms.firstOrNull();"Antonym"->answer.antonyms.firstOrNull();else->answer.word}?:answer.word
    fun next(){
        val eligible=when(mode){"Synonym"->pool.filter{it.synonyms.isNotEmpty()};"Antonym"->pool.filter{it.antonyms.isNotEmpty()};else->pool}
        answer=eligible.random();val right=correct()
        val wrong=pool.map{it.word}.filter{it!=right}.shuffled().take(3)
        options=(listOf(right)+wrong).distinct().shuffled();chosen=null
    }
    LaunchedEffect(mode){next()}
    LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Text("Vocabulary Lab",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);Text("Definitions, synonyms, antonyms and spelling practice.")}
        item{Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("Definition","Synonym","Antonym","Spelling").forEach{m->FilterChip(selected=mode==m,onClick={mode=m},label={Text(m)})}}}
        item{OutlinedCard{Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text("Score",fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("${score} / ${answered}",style=MaterialTheme.typography.titleLarge)}}}
        item{Card(shape=RoundedCornerShape(28.dp)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text(when(mode){"Synonym"->"Choose the synonym for “${answer.word}”.";"Antonym"->"Choose the antonym for “${answer.word}”.";"Spelling"->"Choose the correctly spelled word.";"Definition"->"Choose the word matching this definition."},fontWeight=FontWeight.Bold)
            if(mode=="Definition")Text(answer.definitions.firstOrNull()?.text.orEmpty())
            options.forEach{o->OutlinedButton(onClick={if(chosen==null){chosen=o;answered++;if(o==correct())score++}},enabled=chosen==null,modifier=Modifier.fillMaxWidth()){Text(o)}}
            chosen?.let{Text(if(it==correct())"Correct! 🎉" else "Correct answer: ${correct()}",fontWeight=FontWeight.Bold);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={next()}){Text("Next")};OutlinedButton(onClick={onWord(answer.word)}){Text("Study word")}}}
        }}}
    }
}@Composable private fun MoreScreen(streak:Int,lookups:Int,onWord:(String)->Unit,modifier:Modifier,onDarkMode:(Boolean)->Unit,onLargeText:(Boolean)->Unit){
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    var category by remember{mutableStateOf(SpecialistDictionary.categories().first())}
    val categories=SpecialistDictionary.categories()
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)ReminderHelper.enable(context)}
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")){uri->if(uri!=null)scope.launch{val text=DictionaryRepository(context.applicationContext).exportVocabulary();context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use{it.write(text)}}}
    val import=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch{val text=context.contentResolver.openInputStream(uri)?.bufferedReader()?.use{it.readText()}.orEmpty();DictionaryRepository(context.applicationContext).importVocabulary(text)}}
    LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)){Column(Modifier.padding(20.dp)){Text("Learning dashboard",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(streak.toString()+" day learning streak");Text(lookups.toString()+" lookups recorded")}}}
        item{OutlinedCard{Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Text("Vocabulary backup",fontWeight=FontWeight.Bold);Text("Move your saved vocabulary between devices.");Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={export.launch("universal-dictionary-vocabulary.txt")}){Text("Export")};OutlinedButton(onClick={import.launch(arrayOf("text/plain","text/*"))}){Text("Import")}}}}}
        item{OutlinedCard{Column(Modifier.padding(16.dp)){Text("Daily learning reminder",fontWeight=FontWeight.Bold);Text("Receive a daily Word of the Day notification at about 8:00 PM.");Button(onClick={if(android.os.Build.VERSION.SDK_INT>=33)permission.launch(android.Manifest.permission.POST_NOTIFICATIONS) else ReminderHelper.enable(context)},modifier=Modifier.padding(top=8.dp)){Text("Enable reminder")};TextButton(onClick={ReminderHelper.cancel(context)}){Text("Turn off")}}}}
        item{val settings=remember{SettingsStore(LocalContext.current.applicationContext)};var dark by remember{mutableStateOf(settings.darkMode())};var large by remember{mutableStateOf(settings.largeText())};OutlinedCard{Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text("Accessibility & appearance",fontWeight=FontWeight.Bold);Text("Adjust readability and theme without changing system settings.");Row(verticalAlignment=Alignment.CenterVertically){Text("Dark mode",Modifier.weight(1f));Switch(checked=dark,onCheckedChange={dark=it;onDarkMode(it)})};Row(verticalAlignment=Alignment.CenterVertically){Text("Larger text",Modifier.weight(1f));Switch(checked=large,onCheckedChange={large=it;onLargeText(it)})};Text("TalkBack and system font settings remain supported.")}}}
        item{Text("Specialist & Nigerian vocabulary",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
        items(categories){c->FilterChip(selected=category==c,onClick={category=c},label={Text(c)})}
        items(SpecialistDictionary.words(category)){w->ListItem(headlineContent={Text(w)},supportingContent={Text(category)},modifier=Modifier.clickable{onWord(w)})}
        item{OutlinedCard{Column(Modifier.padding(16.dp)){Text("Free/open data policy",fontWeight=FontWeight.Bold);Text("No proprietary dictionary credentials or databases are used. Open resources require compatible licensing and attribution.")}}}
    }
}
private fun entryText(e:DictionaryEntry)=buildString{append(e.word);e.phonetic?.let{append(" ");append(it)};appendLine();e.definitions.forEachIndexed{i,d->{append(i+1);append(". ");append(d.partOfSpeech);append(": ");appendLine(d.text);d.example?.let{append("Example: ");appendLine(it)};if(d.synonyms.isNotEmpty()){append("Synonyms: ");appendLine(d.synonyms.joinToString(", "))};if(d.antonyms.isNotEmpty()){append("Antonyms: ");appendLine(d.antonyms.joinToString(", "))}};append("Source: ");append(e.source)}
private fun playAudio(url:String?){if(url.isNullOrBlank())return;runCatching{MediaPlayer().apply{setDataSource(url);prepareAsync();setOnPreparedListener{it.start()};setOnCompletionListener{it.release()};setOnErrorListener{mp,_,_->mp.release();true}}}}
