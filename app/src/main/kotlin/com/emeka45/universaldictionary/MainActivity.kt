package com.emeka45.universaldictionary

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.emeka45.universaldictionary.data.DictionaryRepository
import com.emeka45.universaldictionary.model.DictionaryEntry
import com.emeka45.universaldictionary.model.SourceDictionary
import com.emeka45.universaldictionary.ui.UniversalDictionaryTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository = DictionaryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incoming = if (intent?.action == Intent.ACTION_PROCESS_TEXT) {
            intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        } else null
        setContent { UniversalDictionaryTheme { DictionaryApp(repository, incoming?.trim()) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DictionaryApp(repository: DictionaryRepository, incoming: String?) {
    var query by remember { mutableStateOf(incoming ?: "") }
    var entry by remember { mutableStateOf<DictionaryEntry?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var suggestions by remember { mutableStateOf(emptyList<String>()) }
    var source by remember { mutableStateOf<SourceDictionary?>(null) }
    var saved by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()

    fun lookup(word: String = query) {
        scope.launch {
            loading = true
            error = null
            suggestions = emptyList()
            repository.lookup(word).onSuccess { entry = it }
                .onFailure { error = it.message ?: "Lookup failed." }
            loading = false
        }
    }

    LaunchedEffect(incoming) { if (!incoming.isNullOrBlank()) lookup(incoming) }

    if (source != null) {
        InAppDictionary(source!!, query) { source = null }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Universal Dictionary", fontWeight = FontWeight.Bold)
                        Text("Meaning • Pronunciation • Discovery", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (it.length >= 2) scope.launch { suggestions = repository.suggestions(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Search any word") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (loading) CircularProgressIndicator(Modifier.size(22.dp))
                        else TextButton(onClick = { lookup() }) { Text("LOOK UP") }
                    }
                )
            }
            if (suggestions.isNotEmpty()) {
                item {
                    Card {
                        Column(Modifier.fillMaxWidth()) {
                            suggestions.forEach { s ->
                                Text(
                                    s,
                                    Modifier.fillMaxWidth().clickable {
                                        query = s
                                        lookup(s)
                                    }.padding(14.dp)
                                )
                            }
                        }
                    }
                }
            }
            error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
            if (entry == null && error == null) {
                item { WelcomeCard() }
            }
            entry?.let { current ->
                item {
                    EntryCard(
                        current,
                        saved.contains(current.word.lowercase()),
                        onSave = {
                            val key = current.word.lowercase()
                            saved = if (saved.contains(key)) saved - key else saved + key
                        },
                        onSpeak = { playAudio(current.audioUrl) }
                    )
                }
                item { Text("Compare with other dictionaries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(DictionaryRepository.sources) { item ->
                    SourceCard(item) { source = item }
                }
            }
            if (entry == null && error == null) {
                item { Text("Major dictionary sources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(DictionaryRepository.sources.take(4)) { item -> SourceCard(item) { source = item } }
            }
        }
    }
}

private fun playAudio(url: String?) {
    if (url.isNullOrBlank()) return
    runCatching {
        MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { it.start() }
            setOnCompletionListener { it.release() }
            setOnErrorListener { mp, _, _ -> mp.release(); true }
        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("A dictionary built around you", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Get definitions, examples, synonyms, antonyms and pronunciation, then explore major dictionary sources without leaving the app.")
            Text("You can also select text in another app and choose Universal Dictionary.", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun EntryCard(entry: DictionaryEntry, saved: Boolean, onSave: () -> Unit, onSpeak: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(entry.word, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    entry.phonetic?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                }
                IconButton(onClick = onSpeak, enabled = entry.audioUrl != null) {
                    Icon(Icons.Default.VolumeUp, "Pronunciation")
                }
                IconButton(onClick = onSave) {
                    Icon(if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, "Save")
                }
            }
            entry.origin?.let {
                Text("Origin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(it)
            }
            entry.definitions.forEachIndexed { index, definition ->
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text((index + 1).toString() + ". " + definition.partOfSpeech, fontWeight = FontWeight.Bold)
                    Text(definition.text)
                    definition.example?.let { Text("“" + it + "”") }
                    if (definition.synonyms.isNotEmpty()) Text("Synonyms: " + definition.synonyms.joinToString(", "))
                    if (definition.antonyms.isNotEmpty()) Text("Antonyms: " + definition.antonyms.joinToString(", "))
                }
            }
            Text("Source: " + entry.source, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SourceCard(source: SourceDictionary, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Language, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(source.name, fontWeight = FontWeight.Bold)
                Text(source.description, style = MaterialTheme.typography.bodySmall)
            }
            Text("OPEN", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun InAppDictionary(source: SourceDictionary, word: String, onBack: () -> Unit) {
    val url = remember(source.name, word) { source.urlFor(word) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(source.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        AndroidView(
            Modifier.fillMaxSize().padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            }
        )
    }
}
