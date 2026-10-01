package com.emeka45.universaldictionary.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun UniversalDictionaryTheme(
    darkOverride:Boolean? = null,
    largeText:Boolean = false,
    content:@Composable () -> Unit
) {
    val dark = darkOverride ?: isSystemInDarkTheme()
    val base=Typography()
    val typography=if(largeText) base.copy(
        bodyLarge=base.bodyLarge.copy(fontSize=base.bodyLarge.fontSize*1.15f),
        bodyMedium=base.bodyMedium.copy(fontSize=base.bodyMedium.fontSize*1.15f),
        labelLarge=base.labelLarge.copy(fontSize=base.labelLarge.fontSize*1.1f),
        titleLarge=base.titleLarge.copy(fontSize=base.titleLarge.fontSize*1.12f),
        headlineSmall=base.headlineSmall.copy(fontSize=base.headlineSmall.fontSize*1.1f)
    ) else base
    MaterialTheme(
        colorScheme=if(dark) darkColorScheme() else lightColorScheme(),
        typography=typography,
        content=content
    )
}
