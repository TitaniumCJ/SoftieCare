package com.softiecare.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SoftieCareColors = darkColorScheme(
    primary = StatusNormal,
    onPrimary = SpaceBackground,
    background = SpaceBackground,
    onBackground = TextPrimary,
    surface = SurfacePanel,
    onSurface = TextPrimary,
    surfaceVariant = SurfacePanel,
    onSurfaceVariant = TextSecondary,
    error = StatusCritical,
    onError = TextPrimary
)

// Strict dark mode: system theme and dynamic colors are ignored
@Composable
fun SoftieCareTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SoftieCareColors,
        content = content
    )
}
