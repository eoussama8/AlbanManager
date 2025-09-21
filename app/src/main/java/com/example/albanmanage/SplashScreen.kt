package com.example.albanmanage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.albanmanage.ui.theme.AlbanManageTheme

import com.example.albanmanage.data.ThemePreferences
import com.example.albanmanage.ui.theme.AlbaneBlue
import com.example.albanmanage.ui.theme.AlbaneGrey
import com.example.albanmanage.ui.theme.AlbaneLightBlue
import com.example.albanmanage.ui.theme.AlbaneRed
import com.example.albanmanage.ui.theme.AlbaneWhite
import kotlinx.coroutines.delay


@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreferences = ThemePreferences(this)

        setContent {

            AlbanManageTheme() {
                SplashScreen()
            }
        }
    }
}


@Composable
fun SplashScreen() {
    val context = LocalContext.current
    val logoScale = remember { Animatable(0.5f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textSlide = remember { Animatable(30f) }
    val taglineAlpha = remember { Animatable(0f) }
    val backgroundWave = remember { Animatable(0f) }
    val redAccentScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Background wave animation
        backgroundWave.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(300)

        // Logo entrance with elegant bounce
        logoAlpha.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 80f))

        delay(400)

        // Red accent appear
        redAccentScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 120f))

        delay(300)

        // App name slide up
        textAlpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        textSlide.animateTo(0f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f))

        delay(400)

        // Tagline fade in
        taglineAlpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))

        delay(1800)
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        (context as? ComponentActivity)?.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AlbaneWhite,
                        AlbaneLightBlue.copy(alpha = 0.1f),
                        AlbaneWhite
                    ),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated background elements
        ElegantBackgroundElements(waveValue = backgroundWave.value)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(40.dp)
        ) {
            // Logo container with subtle elevation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
            ) {
                // Logo background circle
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            ambientColor = AlbaneBlue.copy(alpha = 0.2f),
                            spotColor = AlbaneBlue.copy(alpha = 0.2f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AlbaneWhite,
                                    AlbaneWhite.copy(alpha = 0.95f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Logo image
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(140.dp)
                )

                // Red accent dot (decorative element inspired by logo's red underline)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-20).dp, y = (-20).dp)
                        .graphicsLayer {
                            scaleX = redAccentScale.value
                            scaleY = redAccentScale.value
                        }
                        .background(
                            color = AlbaneRed,
                            shape = CircleShape
                        )
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = AlbaneRed.copy(alpha = 0.3f),
                            spotColor = AlbaneRed.copy(alpha = 0.3f)
                        )
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            // App name with brand colors
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp,
                color = AlbaneBlue,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = textAlpha.value
                        translationY = textSlide.value
                    }
            )

            // Red underline accent (inspired by logo design)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(4.dp)
                    .graphicsLayer {
                        alpha = textAlpha.value
                        scaleX = textAlpha.value
                    }
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

            Spacer(modifier = Modifier.height(20.dp))

            // Elegant tagline
            Text(
                text = "Professional Album Management",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                letterSpacing = 0.8.sp,
                color = AlbaneGrey.copy(alpha = 0.9f),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = taglineAlpha.value
                    }
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Elegant loading animation with brand colors
            ElegantLoadingAnimation(
                modifier = Modifier.graphicsLayer { alpha = taglineAlpha.value },
                primaryColor = AlbaneBlue,
                accentColor = AlbaneRed
            )
        }
    }
}

@Composable
fun ElegantBackgroundElements(waveValue: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Floating blue circles - subtle and elegant
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-20).dp)
                .graphicsLayer {
                    alpha = 0.03f + (waveValue * 0.02f)
                    scaleX = 0.9f + (waveValue * 0.15f)
                    scaleY = 0.9f + (waveValue * 0.15f)
                }
                .background(
                    color = AlbaneBlue.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 40.dp)
                .graphicsLayer {
                    alpha = 0.04f + (waveValue * 0.03f)
                    scaleX = 0.8f + (waveValue * 0.2f)
                    scaleY = 0.8f + (waveValue * 0.2f)
                }
                .background(
                    color = AlbaneLightBlue.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )

        // Small red accent circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.TopStart)
                .offset(x = 40.dp, y = 120.dp)
                .graphicsLayer {
                    alpha = 0.05f + (waveValue * 0.03f)
                    scaleX = 0.7f + (waveValue * 0.4f)
                    scaleY = 0.7f + (waveValue * 0.4f)
                    rotationZ = waveValue * 180f
                }
                .background(
                    color = AlbaneRed.copy(alpha = 0.06f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun ElegantLoadingAnimation(
    modifier: Modifier = Modifier,
    primaryColor: Color = AlbaneBlue,
    accentColor: Color = AlbaneRed,
    dotSize: Dp = 10.dp,
    spaceBetween: Dp = 14.dp
) {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 120L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 2000
                        0.0f at 0 with LinearOutSlowInEasing
                        1.0f at 300 with FastOutSlowInEasing
                        0.3f at 600 with LinearOutSlowInEasing
                        0.0f at 900 with LinearOutSlowInEasing
                        0.0f at 2000 with LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        dots.forEachIndexed { index, animatable ->
            val value = animatable.value
            val color = if (index == 0) accentColor else primaryColor

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        alpha = 0.3f + (0.7f * value)
                        scaleX = 0.5f + (0.8f * value)
                        scaleY = 0.5f + (0.8f * value)
                    }
                    .background(
                        color = color.copy(alpha = 0.7f + (0.3f * value)),
                        shape = CircleShape
                    )
            )

            if (index < dots.size - 1) {
                Spacer(modifier = Modifier.width(spaceBetween))
            }
        }
    }
}