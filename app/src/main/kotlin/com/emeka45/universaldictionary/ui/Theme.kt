package com.emeka45.universaldictionary.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF3559C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE2FF),
    onPrimaryContainer = Color(0xFF07164F),
    secondary = Color(0xFF5B5D72),
    secondaryContainer = Color(0xFFE1E2F4),
    tertiary = Color(0xFF7A4E00),
    tertiaryContainer = Color(0xFFFFDDA8),
    background = Color(0xFFF9F9FF),
    surface = Color(0xFFF9F9FF),
    surfaceContainer = Color(0xFFEDEEF7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8C4FF),
    onPrimary = Color(0xFF10256F),
    primaryContainer = Color(0xFF263B8F),
    onPrimaryContainer = Color(0xFFDDE2FF),
    secondary = Color(0xFFC5C5DC),
    secondaryContainer = Color(0xFF424355),
    tertiary = Color(0xFFE9B96E),
    tertiaryContainer = Color(0xFF5A410F),
    background = Color(0xFF111218),
    surface = Color(0xFF111218),
    surfaceContainer = Color(0xFF1D1E26)
)

@Composable
fun UniversalDictionaryTheme(
    darkOverride: Boolean? = null,
    largeText: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = darkOverride ?: isSystemInDarkTheme()
    val base = Typography()
    val typography = base.copy(
        displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(lineHeight = base.bodyLarge.lineHeight * 1.12f)
    ).let {
        if (!largeText) it else it.copy(
            bodyLarge = it.bodyLarge.copy(fontSize = it.bodyLarge.fontSize * 1.15f),
            bodyMedium = it.bodyMedium.copy(fontSize = it.bodyMedium.fontSize * 1.15f),
            titleLarge = it.titleLarge.copy(fontSize = it.titleLarge.fontSize * 1.1f),
            headlineSmall = it.headlineSmall.copy(fontSize = it.headlineSmall.fontSize * 1.08f)
        )
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = typography,
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(36.dp)
        ),
        content = content
    )
}
