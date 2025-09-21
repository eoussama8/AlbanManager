package com.example.albanmanage.HistoryScreen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings.Global.getString
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.albanmanage.R
import com.example.albanmanage.ui.theme.AlbaneBlue
import com.example.albanmanage.ui.theme.AlbaneGrey
import com.example.albanmanage.ui.theme.AlbaneRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class HistoryItem(
    val fileName: String,
    val date: String,
    val filePath: String,
    val fileSize: Long,
    val timestamp: Long // For proper sorting
)

@Composable
fun HistoryScreen(currentLanguage: String) {
    val context = LocalContext.current
    var historyItems by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var permissionDenied by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<HistoryItem?>(null) }

    // Apply language dynamically
    LaunchedEffect(currentLanguage) {
        val locale = Locale(currentLanguage)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDenied = false
            loadHistoryItems(context) { items, error ->
                historyItems = items
                errorMessage = error
                isLoading = false
            }
        } else {
            permissionDenied = true
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || checkStoragePermission(context)) {
            loadHistoryItems(context) { items, error ->
                historyItems = items
                errorMessage = error
                isLoading = false
            }
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { itemToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_pdf_title)) },
            text = { Text(stringResource(R.string.delete_pdf_message, itemToDelete.fileName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        fun onDelete(item: HistoryItem) {
                            deletePdf(context, item.filePath) { success ->
                                // You cannot call stringResource here directly
                                // Instead, handle it inside a LaunchedEffect or wrap with context
                                if (success) {
                                    historyItems = historyItems.filter { it.filePath != item.filePath }
                                    Toast.makeText(context, context.getString(R.string.pdf_deleted_success), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.pdf_delete_failed), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = AlbaneRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.history_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlbaneBlue
                )
                Text(
                    text = stringResource(R.string.history_subtitle),
                    fontSize = 14.sp,
                    color = AlbaneGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AlbaneBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.loading_history), color = AlbaneGrey, fontSize = 14.sp)
                    }
                }
            }

            permissionDenied -> {
                PermissionDeniedContent(
                    onRetryClick = { permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE) }
                )
            }

            errorMessage != null -> {
                ErrorContent(
                    errorMessage = errorMessage!!,
                    onRetryClick = {
                        isLoading = true
                        errorMessage = null
                        loadHistoryItems(context) { items, error ->
                            historyItems = items
                            errorMessage = error
                            isLoading = false
                        }
                    }
                )
            }

            historyItems.isEmpty() -> {
                EmptyHistoryContent()
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(historyItems) { item ->
                        AnimatedVisibility(visible = true, enter = slideInVertically() + fadeIn(), exit = fadeOut()) {
                            HistoryItemCard(item = item, onDeleteClick = { showDeleteDialog = item })
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PermissionDeniedContent(onRetryClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = AlbaneRed.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AlbaneRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.storage_permission_required),
                color = AlbaneRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(containerColor = AlbaneRed)
            ) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

@Composable
private fun ErrorContent(errorMessage: String, onRetryClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = AlbaneRed.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_disabled),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AlbaneRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = AlbaneRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(containerColor = AlbaneRed)
            ) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun EmptyHistoryContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = AlbaneGrey.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_empty_folder),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AlbaneGrey
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.no_history_found),
                color = AlbaneGrey,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.no_history_description),
                color = AlbaneGrey.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    var isClicked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isClicked = true
                openPdf(context, item.filePath) {
                    isClicked = false
                }
            }
            .shadow(if (isClicked) 8.dp else 4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isClicked) AlbaneBlue.copy(alpha = 0.05f) else Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pdf),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AlbaneBlue
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fileName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    color = AlbaneGrey,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = formatFileSize(item.fileSize),
                    fontSize = 12.sp,
                    color = AlbaneGrey.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Action buttons
            Row {
                IconButton(onClick = {
                    sharePdf(context, item.filePath)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = stringResource(R.string.share_pdf),
                        tint = AlbaneBlue
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete PDF",
                        tint = AlbaneRed
                    )
                }
            }
        }
    }
}

private fun checkStoragePermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

private fun loadHistoryItems(
    context: Context,
    callback: (List<HistoryItem>, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val items = mutableListOf<HistoryItem>()

            // Try multiple directories
            val directories = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File(context.getExternalFilesDir(null), "PDFs"),
                File(context.filesDir, "PDFs")
            )

            for (directory in directories) {
                if (directory.exists()) {
                    directory.listFiles { _, name ->
                        name.endsWith(".pdf", ignoreCase = true)
                    }?.forEach { file ->
                        if (file.name.contains("AlbanManage", ignoreCase = true)) {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val date = dateFormat.format(Date(file.lastModified()))

                            items.add(
                                HistoryItem(
                                    fileName = file.name,
                                    date = date,
                                    filePath = file.absolutePath,
                                    fileSize = file.length(),
                                    timestamp = file.lastModified()
                                )
                            )
                        }
                    }
                }
            }

            val sortedItems = items.sortedByDescending { it.timestamp }

            withContext(Dispatchers.Main) {
                callback(sortedItems, null)
            }

        } catch (e: SecurityException) {
            withContext(Dispatchers.Main) {
                callback(emptyList(), context.getString(R.string.permission_denied))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback(emptyList(), "Error loading files: ${e.message}")
            }
        }
    }
}

private fun openPdf(context: Context, filePath: String, onComplete: () -> Unit = {}) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_LONG).show()
            onComplete()
            return
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // Check if there's an app to handle PDFs
        val packageManager = context.packageManager
        val activities = packageManager.queryIntentActivities(intent, 0)

        if (activities.isNotEmpty()) {
            context.startActivity(intent)
        } else {
            // Try with a chooser
            try {
                context.startActivity(Intent.createChooser(intent, "Open PDF"))
            } catch (e: Exception) {
                Toast.makeText(context, "No PDF reader app found", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error opening PDF: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    } finally {
        onComplete()
    }
}

private fun sharePdf(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_LONG).show()
            return
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error sharing PDF: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun deletePdf(
    context: Context,
    filePath: String,
    onComplete: (Boolean) -> Unit // Normal lambda
) {
    try {
        val file = File(filePath)
        if (file.exists()) {
            val deleted = file.delete()
            onComplete(deleted)
        } else {
            onComplete(false)
        }
    } catch (e: Exception) {
        onComplete(false)
    }
}


private fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    return if (kb < 1024) {
        String.format(Locale.getDefault(), "%.1f KB", kb)
    } else {
        String.format(Locale.getDefault(), "%.1f MB", kb / 1024)
    }
}