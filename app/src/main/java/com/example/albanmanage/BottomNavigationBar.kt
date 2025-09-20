package com.example.albanmanage.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.albanmanage.R
import com.example.albanmanage.ui.theme.AlbaneBlue
import com.example.albanmanage.ui.theme.AlbaneGrey
import com.example.albanmanage.ui.theme.AlbaneLightBlue
import com.example.albanmanage.ui.theme.AlbaneRed
import com.example.albanmanage.ui.theme.AlbaneWhite

// Navigation Items Data Class
data class NavItem(
    val title: String,
    val iconRes: Int,
    val route: String
)

@Composable
fun BottomNavigationBar(
    items: List<NavItem> = defaultNavItems(),
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ambientColor = AlbaneBlue.copy(alpha = 0.15f),
                spotColor = AlbaneBlue.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = AlbaneWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Top border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AlbaneLightBlue.copy(alpha = 0.3f),
                            AlbaneBlue.copy(alpha = 0.2f),
                            AlbaneLightBlue.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                NavBarItem(
                    item = item,
                    isSelected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val iconScale = remember { Animatable(1f) }
    val textAlpha = remember { Animatable(if (isSelected) 1f else 0.7f) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            iconScale.animateTo(1.15f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f))
            iconScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f))
            textAlpha.animateTo(1f, animationSpec = tween(200))
        } else {
            textAlpha.animateTo(0.7f, animationSpec = tween(200))
        }
    }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            }
            .padding(vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Icon(
                imageVector = ImageVector.vectorResource(id = item.iconRes),
                contentDescription = item.title,
                tint = if (isSelected) AlbaneBlue else AlbaneGrey.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Label Text
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) AlbaneBlue else AlbaneGrey.copy(alpha = 0.8f),
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                }
            )

            // Active indicator line
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    AlbaneRed,
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun FloatingBottomNavigationBar(
    items: List<NavItem> = defaultNavItems(),
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = AlbaneBlue.copy(alpha = 0.15f),
                spotColor = AlbaneBlue.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AlbaneWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (index == 1) { // Middle item (History)
                    FloatingNavBarItem(
                        item = item,
                        isSelected = selectedIndex == index,
                        onClick = { onItemSelected(index) }
                    )
                } else {
                    NavBarItem(
                        item = item,
                        isSelected = selectedIndex == index,
                        onClick = { onItemSelected(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingNavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val elevation = remember { Animatable(if (isSelected) 16f else 8f) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            scale.animateTo(1.1f, animationSpec = spring(dampingRatio = 0.6f))
            scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f))
            elevation.animateTo(20f, animationSpec = tween(300))
        } else {
            elevation.animateTo(8f, animationSpec = tween(300))
        }
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(
                elevation = elevation.value.dp,
                shape = CircleShape,
                ambientColor = if (isSelected) AlbaneRed.copy(alpha = 0.2f) else AlbaneBlue.copy(alpha = 0.1f),
                spotColor = if (isSelected) AlbaneRed.copy(alpha = 0.2f) else AlbaneBlue.copy(alpha = 0.1f)
            )
            .background(
                brush = if (isSelected) Brush.radialGradient(
                    colors = listOf(AlbaneRed, AlbaneRed.copy(alpha = 0.9f))
                ) else Brush.radialGradient(
                    colors = listOf(AlbaneBlue, AlbaneBlue.copy(alpha = 0.9f))
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = item.iconRes),
                contentDescription = item.title,
                tint = AlbaneWhite,
                modifier = Modifier.size(26.dp)
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlbaneWhite
                )
            }
        }
    }
}

@Composable
fun defaultNavItems(): List<NavItem> {
    return listOf(
        NavItem(stringResource(R.string.nav_home), R.drawable.home, "home"),
        NavItem(stringResource(R.string.nav_history), R.drawable.history, "history"),
        NavItem(stringResource(R.string.nav_settings), R.drawable.settings, "settings")
    )
}