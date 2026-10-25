package com.example.ovyu.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OvyuBlueLight,
    onPrimary = Color(0xFF00243D),
    secondary = OvyuPurpleLight,
    tertiary = OvyuBlue
)

private val LightColorScheme = lightColorScheme(
    primary = OvyuBlue,
    onPrimary = Color.White,
    secondary = OvyuPurple,
    onSecondary = Color.White,
    tertiary = OvyuDarkBlue,
    onTertiary = Color.White
)

@Composable
fun OvyuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desligado por padrão para manter sempre as cores da marca Ovyu.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
