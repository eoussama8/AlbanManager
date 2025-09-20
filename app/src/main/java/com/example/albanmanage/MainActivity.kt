package com.example.albanmanage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.albanmanage.ui.theme.AlbaneBlue
import com.example.albanmanage.ui.theme.AlbaneGrey
import com.example.albanmanage.ui.theme.AlbaneWhite
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.albanmanage.HistoryScreen.HistoryScreen
import com.example.albanmanage.HomeScreen.HomeScreen
import com.example.albanmanage.SettingsScreen.SettingsScreen
import com.example.albanmanage.components.BottomNavigationBar
import com.example.albanmanage.components.NavItem
import com.example.albanmanage.data.ThemePreferences
import com.example.albanmanage.ui.theme.AlbanManageTheme


class MainActivity : ComponentActivity() {
    private lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreferences = ThemePreferences(this)
        enableEdgeToEdge()

        setContent {
            AlbanManageTheme(themePreferences = themePreferences) {
                MainScreenWithNavigation()
            }
        }
    }
}

@Composable
fun MainScreenWithNavigation() {
    var selectedIndex by remember { mutableStateOf(0) }

    // Navigation items - replace R.drawable.ic_* with your actual drawable resources
    val navItems = listOf(
        NavItem("Home", R.drawable.home, "home"),
        NavItem("History", R.drawable.history, "history"),
        NavItem("Settings", R.drawable.settings, "settings")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    selectedIndex = index
                }
            )
        }
    ) { innerPadding ->
        // Main content based on selected tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AlbaneWhite),
            contentAlignment = Alignment.Center
        ) {
            when (selectedIndex) {
                0 -> HomeScreen()
                1 -> HistoryScreen()
                2 -> SettingsScreen()
            }
        }
    }
}






