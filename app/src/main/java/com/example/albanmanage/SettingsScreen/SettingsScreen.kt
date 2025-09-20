package com.example.albanmanage.SettingsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.albanmanage.ui.theme.AlbaneBlue
import com.example.albanmanage.ui.theme.AlbaneGrey

@Composable
fun SettingsScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = "⚙️ Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = AlbaneBlue
        )
        Text(
            text = "Customize your experience",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AlbaneGrey,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Preferences and configuration",
            fontSize = 14.sp,
            color = AlbaneGrey.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}