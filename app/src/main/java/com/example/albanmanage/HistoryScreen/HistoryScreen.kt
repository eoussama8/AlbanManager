package com.example.albanmanage.HistoryScreen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(historyDao: HistoryDao) {
    val coroutineScope = rememberCoroutineScope()
    var historyList by remember { mutableStateOf<List<HistoryEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load history from DB
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = historyDao.getAllHistory()
            withContext(Dispatchers.Main) {
                historyList = data
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color.White
                    )
                )
            )
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "History",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlbaneBlue
                )

                if (historyList.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pdf),
                            contentDescription = null,
                            tint = AlbaneGrey,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${historyList.size} files",
                            fontSize = 12.sp,
                            color = AlbaneGrey,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AlbaneBlue,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading history...",
                            fontSize = 14.sp,
                            color = AlbaneGrey
                        )
                    }
                }
            }
            historyList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyHistoryContent()
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyList, key = { it.id }) { history ->
                        HistoryItemCard(
                            history = history,
                            onDeleteClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    historyDao.delete(history)
                                    val updated = historyDao.getAllHistory()
                                    withContext(Dispatchers.Main) {
                                        historyList = updated
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    history: HistoryEntity,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isClicked = true
                openPdfByFileName(context, history.fileName) {
                    isClicked = false
                }
            }
            .shadow(
                elevation = if (isClicked) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isClicked) AlbaneBlue.copy(alpha = 0.05f) else Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PDF Icon with background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            AlbaneBlue.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pdf),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AlbaneBlue
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = history.fileName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                            .format(Date(history.date)),
                        fontSize = 12.sp,
                        color = AlbaneGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "${history.productCount} products",
                    color = AlbaneBlue
                )
                StatChip(
                    label = "Before: ${history.totalBefore}",
                    color = AlbaneRed.copy(alpha = 0.7f)
                )
                StatChip(
                    label = "After: ${history.totalAfter}",
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Open Button - Primary action
                Button(
                    onClick = {
                        openPdfByFileName(context, history.fileName)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlbaneBlue
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pdf),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Open PDF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Share Button
                OutlinedButton(
                    onClick = {
                        sharePdfByFileName(context, history.fileName)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AlbaneBlue
                    ),
                    border = BorderStroke(1.dp, AlbaneBlue)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete Button
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AlbaneRed
                    ),
                    border = BorderStroke(1.dp, AlbaneRed.copy(alpha = 0.5f))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete History Item",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete \"${history.fileName}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AlbaneRed
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    color: Color
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyHistoryContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        AlbaneGrey.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_empty_folder),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AlbaneGrey
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "No history yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your PDF generation history will appear here once you start creating reports",
                fontSize = 14.sp,
                color = AlbaneGrey,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// PDF File Operations Functions
private fun findPdfFile(context: Context, fileName: String): File? {
    // Try multiple directories where PDFs might be stored
    val directories = listOf(
        // Downloads folder (primary location)
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        // App-specific external storage
        File(context.getExternalFilesDir(null), "PDFs"),
        // App-specific internal storage
        File(context.filesDir, "PDFs"),
        // Documents folder
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        // App external files directory
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        // Cache directory
        context.externalCacheDir
    )

    for (directory in directories) {
        if (directory?.exists() == true) {
            try {
                // Try exact filename match first
                val exactFile = File(directory, fileName)
                if (exactFile.exists()) {
                    return exactFile
                }

                // Try searching for files containing the base name (without timestamp)
                val baseFileName = fileName.replace(".pdf", "")
                directory.listFiles { _, name ->
                    name.endsWith(".pdf", ignoreCase = true) &&
                            (name.contains(baseFileName, ignoreCase = true) ||
                                    name.contains("AlbanManage", ignoreCase = true) ||
                                    name.contains("Invoice", ignoreCase = true))
                }?.sortedByDescending { it.lastModified() }?.firstOrNull()?.let { return it }

                // Also try searching subdirectories
                directory.listFiles { file -> file.isDirectory }?.forEach { subDir ->
                    val subFile = File(subDir, fileName)
                    if (subFile.exists()) {
                        return subFile
                    }
                }
            } catch (e: SecurityException) {
                // Skip directory if no permission
                continue
            }
        }
    }
    return null
}

private fun openPdfByFileName(context: Context, fileName: String, onComplete: () -> Unit = {}) {
    try {
        // Debug: Show what we're looking for
        Toast.makeText(context, "Looking for: $fileName", Toast.LENGTH_SHORT).show()

        val file = findPdfFile(context, fileName)

        if (file == null || !file.exists()) {
            // More detailed error message
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val message = if (downloadsDir?.exists() == true) {
                val pdfFiles = downloadsDir.listFiles { _, name -> name.endsWith(".pdf") }?.map { it.name }
                "PDF not found. Available PDFs in Downloads: ${pdfFiles?.joinToString(", ") ?: "None"}"
            } else {
                "Downloads folder not accessible"
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onComplete()
            return
        }

        // Debug: Show what we found
        Toast.makeText(context, "Found: ${file.name} at ${file.parent}", Toast.LENGTH_SHORT).show()

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: IllegalArgumentException) {
                // Fallback if FileProvider fails
                Toast.makeText(context, "FileProvider error: ${e.message}", Toast.LENGTH_LONG).show()
                Uri.fromFile(file)
            }
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
            Toast.makeText(context, "Opening ${file.name}", Toast.LENGTH_SHORT).show()
        } else {
            // Try with a chooser
            try {
                context.startActivity(Intent.createChooser(intent, "Open PDF"))
            } catch (e: Exception) {
                Toast.makeText(context, "No PDF reader app found. Please install a PDF reader.", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: SecurityException) {
        Toast.makeText(context, "Permission error: ${e.message}", Toast.LENGTH_LONG).show()
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

private fun sharePdfByFileName(context: Context, fileName: String) {
    try {
        val file = findPdfFile(context, fileName)

        if (file == null || !file.exists()) {
            Toast.makeText(context, "PDF file not found: $fileName", Toast.LENGTH_LONG).show()
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
            putExtra(Intent.EXTRA_SUBJECT, "AlbanManage Report - $fileName")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the report: $fileName")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(Intent.createChooser(intent, "Share PDF"))
        Toast.makeText(context, "Sharing ${file.name}", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error sharing PDF: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
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