package com.example.albanmanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.albanmanage.HistoryScreen.AppDatabase
import com.example.albanmanage.HistoryScreen.HistoryDao
import kotlinx.coroutines.launch
import com.example.albanmanage.HistoryScreen.HistoryScreen
import com.example.albanmanage.HomeScreen.HomeScreen
import com.example.albanmanage.SettingsScreen.SettingsRepository
import com.example.albanmanage.SettingsScreen.SettingsScreen
import com.example.albanmanage.components.BottomNavigationBar
import com.example.albanmanage.components.NavItem
import com.example.albanmanage.data.ThemeMode
import com.example.albanmanage.ui.theme.AlbanManageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = SettingsRepository(applicationContext)

        val db = AppDatabase.getDatabase(applicationContext)
        val historyDao = db.historyDao()
        setContent {
            // Observe selected language
            val selectedLanguage by settingsRepository.language.collectAsState(initial = "en")

            AlbanManageTheme {
                // Pass language state to MainScreen
                MainScreenWithNavigation(
                    settingsRepository = settingsRepository,
                    currentLanguage = selectedLanguage,
                    onLanguageChanged = { newLang ->
                        lifecycleScope.launch {
                            settingsRepository.saveLanguage(newLang)
                        }
                    },
                    historyDao = historyDao // pass DAO here
                )

            }
        }
    }
}

@Composable
fun MainScreenWithNavigation(

    settingsRepository: SettingsRepository,
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    historyDao: HistoryDao // pass DAO from caller
) {
    var selectedIndex by remember { mutableStateOf(0) }

    val navItems = listOf(
        NavItem("Home", R.drawable.home, "home"),
        NavItem("History", R.drawable.history, "history"),
        NavItem("Settings", R.drawable.settings, "settings")
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedIndex) {
                0 -> HomeScreen(currentLanguage, historyDao)
                1 -> HistoryScreen(historyDao)
                2 -> SettingsScreen(
                    settingsRepository = settingsRepository,
                    onLanguageChanged = onLanguageChanged
                )
            }
        }
    }
}
