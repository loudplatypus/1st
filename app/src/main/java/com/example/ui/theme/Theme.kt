package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ROMANTIC ROSE COLOR PALETTES
private val RoseLight = lightColorScheme(
    primary = RosePrimaryLight,
    secondary = RoseSecondaryLight,
    tertiary = RoseTertiaryLight,
    background = RoseBackgroundLight,
    surface = RoseSurfaceLight,
    onPrimary = RoseOnPrimaryLight,
    onBackground = RoseOnBackgroundLight,
    onSurface = RoseOnBackgroundLight,
    onSecondary = Color.White
)
private val RoseDark = darkColorScheme(
    primary = RosePrimaryDark,
    secondary = RoseSecondaryDark,
    tertiary = RoseTertiaryDark,
    background = RoseBackgroundDark,
    surface = RoseSurfaceDark,
    onPrimary = RoseOnPrimaryDark,
    onBackground = RoseOnBackgroundDark,
    onSurface = RoseOnBackgroundDark,
    onSecondary = Color.White
)

// SUNSET AMBER COLOR PALETTES
private val AmberLight = lightColorScheme(
    primary = AmberPrimary,
    secondary = AmberSecondary,
    tertiary = Color(0xFFF59E0B),
    background = AmberBackground,
    surface = AmberSurface,
    onPrimary = AmberOnPrimary,
    onBackground = AmberOnBackground,
    onSurface = AmberOnBackground,
    onSecondary = Color.White
)
private val AmberDark = darkColorScheme(
    primary = AmberPrimaryDark,
    secondary = AmberSecondaryDark,
    tertiary = Color(0xFFFBBF24),
    background = AmberBackgroundDark,
    surface = AmberSurfaceDark,
    onPrimary = AmberOnPrimaryDark,
    onBackground = AmberOnBackgroundDark,
    onSurface = AmberOnBackgroundDark,
    onSecondary = Color.White
)

// MIDNIGHT SKY COLOR PALETTES
private val SkyLight = lightColorScheme(
    primary = SkyPrimary,
    secondary = SkySecondary,
    tertiary = Color(0xFF818CF8),
    background = SkyBackground,
    surface = SkySurface,
    onPrimary = SkyOnPrimary,
    onBackground = SkyOnBackground,
    onSurface = SkyOnBackground,
    onSecondary = Color.White
)
private val SkyDark = darkColorScheme(
    primary = SkyPrimaryDark,
    secondary = SkySecondaryDark,
    tertiary = Color(0xFF312E81),
    background = SkyBackgroundDark,
    surface = SkySurfaceDark,
    onPrimary = SkyOnPrimaryDark,
    onBackground = SkyOnBackgroundDark,
    onSurface = SkyOnBackgroundDark,
    onSecondary = Color.White
)

// EMERALD GARDEN COLOR PALETTES
private val GardenLight = lightColorScheme(
    primary = GardenPrimary,
    secondary = GardenSecondary,
    tertiary = Color(0xFF34D399),
    background = GardenBackground,
    surface = GardenSurface,
    onPrimary = GardenOnPrimary,
    onBackground = GardenOnBackground,
    onSurface = GardenOnBackground,
    onSecondary = Color.White
)
private val GardenDark = darkColorScheme(
    primary = GardenPrimaryDark,
    secondary = GardenSecondaryDark,
    tertiary = Color(0xFF022C22),
    background = GardenBackgroundDark,
    surface = GardenSurfaceDark,
    onPrimary = GardenOnPrimaryDark,
    onBackground = GardenOnBackgroundDark,
    onSurface = GardenOnBackgroundDark,
    onSecondary = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "Romantic Rose",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Sunset Amber" -> if (darkTheme) AmberDark else AmberLight
        "Midnight Sky" -> if (darkTheme) SkyDark else SkyLight
        "Emerald Garden" -> if (darkTheme) GardenDark else GardenLight
        else -> if (darkTheme) RoseDark else RoseLight // Default to Romantic Rose
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
