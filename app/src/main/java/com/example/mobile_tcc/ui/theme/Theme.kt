package com.example.mobile_tcc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    background = Background,
    surface = Background,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorColor,
    errorContainer = ErrorContainer,
    outlineVariant = OutlineVariant
)

@Composable
fun Mobile_TCCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography, // Se der erro vermelho aqui, não se preocupe, arrumaremos depois
        content = content
    )
}