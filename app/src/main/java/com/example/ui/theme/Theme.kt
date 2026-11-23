package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = IslamicTeal,
    onPrimary = CleanWhite,
    primaryContainer = SoftMint,
    onPrimaryContainer = IslamicTealDark,
    secondary = WarmAmber,
    onSecondary = IslamicTealDark,
    secondaryContainer = WarmAmberSoft,
    onSecondaryContainer = IslamicTealDark,
    tertiary = ShareGreen,
    onTertiary = CleanWhite,
    tertiaryContainer = ShareGreenSoft,
    onTertiaryContainer = IslamicTealDark,
    background = CleanWhite,
    onBackground = TextPrimary,
    surface = CleanWhite,
    onSurface = TextPrimary,
    surfaceVariant = SoftMintTint,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = AlertRed,
    onError = CleanWhite,
    errorContainer = AlertRedSoft,
    onErrorContainer = AlertRed
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftMint,
    onPrimary = IslamicTealDark,
    primaryContainer = IslamicTeal,
    onPrimaryContainer = CleanWhite,
    secondary = WarmAmber,
    onSecondary = IslamicTealDark,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = WarmAmber,
    tertiary = ShareGreen,
    onTertiary = CleanWhite,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = IslamicTealLight,
    error = AlertRedBorder,
    onError = DarkBackground,
    errorContainer = AlertRed,
    onErrorContainer = CleanWhite
)

val UmmahShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun UmmahDailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UmmahTypography,
        shapes = UmmahShapes,
        content = content
    )
}
