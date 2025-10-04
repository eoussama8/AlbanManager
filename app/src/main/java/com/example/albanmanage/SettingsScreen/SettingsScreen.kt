package com.example.albanmanage.SettingsScreen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.albanmanage.ui.theme.AlbaneBlue
import com.example.albanmanage.ui.theme.AlbaneGrey
import kotlinx.coroutines.launch

data class Language(val code: String, val name: String, val flag: String)

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onLanguageChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe language from DataStore directly
    val selectedLanguage by settingsRepository.language.collectAsState(initial = "en")

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoSaveEnabled by remember { mutableStateOf(true) }

    val languages = listOf(
        Language("en", "English", "🇺🇸"),
        Language("fr", "Français", "🇫🇷"),
        Language("ar", "العربية", "🇸🇦")
    )

    // Language selection dialog
    if (showLanguageDialog) {
        var tempSelectedLanguage by remember { mutableStateOf(selectedLanguage) }

        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", fontWeight = FontWeight.Bold, color = AlbaneBlue) },
            text = {
                LazyColumn {
                    items(languages, key = { it.code }) { language ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    tempSelectedLanguage = language.code
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (tempSelectedLanguage == language.code)
                                    AlbaneBlue.copy(alpha = 0.1f) else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(language.flag, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                                Text(
                                    language.name,
                                    fontSize = 16.sp,
                                    fontWeight = if (tempSelectedLanguage == language.code) FontWeight.Bold else FontWeight.Normal,
                                    color = if (tempSelectedLanguage == language.code) AlbaneBlue else Color.Black
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showLanguageDialog = false
                    coroutineScope.launch {
                        settingsRepository.saveLanguage(tempSelectedLanguage)
                        onLanguageChanged(tempSelectedLanguage)
                    }
                }) {
                    Text("OK", color = AlbaneBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About AlbanManage", fontWeight = FontWeight.Bold, color = AlbaneBlue) },
            text = {
                Column {
                    Text("Version 1.0.0", fontSize = 14.sp, color = AlbaneGrey, modifier = Modifier.padding(bottom = 8.dp))
                    Text(
                        "AlbanManage helps organize and share your PDF documents efficiently.",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("© 2024 AlbanManage Team", fontSize = 12.sp, color = AlbaneGrey)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = AlbaneBlue)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection(title = "Language") {
                    SettingsItem(
                        icon = "🌍",
                        title = "Language",
                        subtitle = languages.find { it.code == selectedLanguage }?.name ?: "English",
                        onClick = { showLanguageDialog = true }
                    )
                }
            }
        }
    }
}


@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AlbaneBlue, modifier = Modifier.padding(bottom = 12.dp))
        Card(
            modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) { Column(modifier = Modifier.padding(4.dp)) { content() } }
    }
}

@Composable
private fun SettingsItem(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Text(subtitle, fontSize = 14.sp, color = AlbaneGrey, modifier = Modifier.padding(top = 2.dp))
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AlbaneGrey, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsSwitchItem(icon: String, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Text(subtitle, fontSize = 14.sp, color = AlbaneGrey, modifier = Modifier.padding(top = 2.dp))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AlbaneBlue, uncheckedThumbColor = Color.White, uncheckedTrackColor = AlbaneGrey)
            )
        }
    }
}
