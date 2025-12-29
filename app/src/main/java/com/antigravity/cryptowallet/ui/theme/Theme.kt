package com.antigravity.cryptowallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val BrutalBlack = Black
val BrutalWhite = White

enum class ThemeType {
    DEFAULT, MIDNIGHT, FOREST, CRIMSON, SLATE, VIOLET
}

private val DefaultScheme = lightColorScheme(
    primary = Black, onPrimary = White, background = White, onBackground = Black, surface = White, onSurface = Black
)

private val MidnightScheme = lightColorScheme(
    primary = Color(0xFF191970), onPrimary = White, background = White, onBackground = Color(0xFF191970), surface = White, onSurface = Color(0xFF191970)
)

private val ForestScheme = lightColorScheme(
    primary = Color(0xFF006400), onPrimary = White, background = White, onBackground = Color(0xFF006400), surface = White, onSurface = Color(0xFF006400)
)

private val CrimsonScheme = lightColorScheme(
    primary = Color(0xFF8B0000), onPrimary = White, background = White, onBackground = Color(0xFF8B0000), surface = White, onSurface = Color(0xFF8B0000)
)

private val SlateScheme = lightColorScheme(
    primary = Color(0xFF2F4F4F), onPrimary = White, background = White, onBackground = Color(0xFF2F4F4F), surface = White, onSurface = Color(0xFF2F4F4F)
)

private val VioletScheme = lightColorScheme(
    primary = Color(0xFF4B0082), onPrimary = White, background = White, onBackground = Color(0xFF4B0082), surface = White, onSurface = Color(0xFF4B0082)
)

val BrutalistTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

@Composable
fun CryptoWalletTheme(
    themeType: ThemeType = ThemeType.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        ThemeType.DEFAULT -> DefaultScheme
        ThemeType.MIDNIGHT -> MidnightScheme
        ThemeType.FOREST -> ForestScheme
        ThemeType.CRIMSON -> CrimsonScheme
        ThemeType.SLATE -> SlateScheme
        ThemeType.VIOLET -> VioletScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BrutalistTypography,
        content = content
    )
}
