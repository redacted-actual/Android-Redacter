package com.redactedactual.redacter.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.redactedactual.redacter.PdfProcessor
import com.redactedactual.redacter.RedactionType
import com.redactedactual.redacter.VoiceCommandManager
import kotlinx.coroutines.launch

@Composable
fun DocumentRedactorScreen(context: Context) {
    val scope = rememberCoroutineScope()
    val pdfProcessor = remember { PdfProcessor(context) }
    val voiceManager = remember { VoiceCommandManager(context) }

    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var currentPage by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val activeTypes by voiceManager.activeTypes.collectAsState()

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // For images: convert to single Bitmap (add helper if needed)
            // For simplicity, convert single image to List<Bitmap>
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isLoading = true
                pages = pdfProcessor.processPdf(it, emptySet()) // initial load
                currentPage = 0
                isLoading = false
            }
        }
    }

    LaunchedEffect(activeTypes) {
        if (activeTypes.isNotEmpty() && pages.isNotEmpty()) {
            scope.launch {
                isLoading = true
                val redactedPages = pages.map { page ->
                    pdfProcessor.redactionEngine.redact(page, activeTypes) // Note: expose engine or duplicate logic
                }
                pages = redactedPages
                isLoading = false
                Toast.makeText(context, "Redaction complete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (pages.isNotEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(
                    bitmap = pages[currentPage].asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(enabled = currentPage > 0, onClick = { currentPage-- }) { Text("Prev") }
                Text("Page ${currentPage + 1} of ${pages.size}")
                Button(enabled = currentPage < pages.size - 1, onClick = { currentPage++ }) { Text("Next") }
            }
        } else {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Select an image or PDF to begin")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { pdfLauncher.launch("application/pdf") }) { Text("Load PDF") }
            Button(onClick = { voiceManager.startListening() }) { Text("🎤 Voice") }
            Button(
                enabled = pages.isNotEmpty() && !isSaving,
                onClick = {
                    scope.launch {
                        isSaving = true
                        val uri = pdfProcessor.saveAsPdf(pages)
                        isSaving = false
                        if (uri != null) {
                            Toast.makeText(context, "Saved to Documents/Redacter", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Save")
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }

    DisposableEffect(Unit) { onDispose { voiceManager.destroy() } }
}
