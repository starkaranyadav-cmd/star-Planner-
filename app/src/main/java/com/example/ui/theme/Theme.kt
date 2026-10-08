package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(
    val displayName: String,
    val subtitle: String,
    val isDarkPalette: Boolean,
    val primaryColorPreview: Color,
    val secondaryColorPreview: Color,
    val bgColorPreview: Color
) {
    DARK_CYBER(
        displayName = "Dark Emerald",
        subtitle = "AI & Tech (#121212 • Radium Emerald • Neon Purple)",
        isDarkPalette = true,
        primaryColorPreview = CyberEmerald,
        secondaryColorPreview = CyberNeonPurple,
        bgColorPreview = CyberBg
    ),
    SOFT_TECH(
        displayName = "Soft Sage & Peach",
        subtitle = "Wellness & SaaS (#F4F6F9 • Light Peach • Mint Sage)",
        isDarkPalette = false,
        primaryColorPreview = SoftTechActionGreen,
        secondaryColorPreview = SoftTechLightPeach,
        bgColorPreview = SoftTechBg
    ),
    ORGANIC_EARTH(
        displayName = "Organic Earth",
        subtitle = "Lifestyle & Luxury (#F9F6F0 • Terracotta • Forest Olive)",
        isDarkPalette = false,
        primaryColorPreview = EarthTerracotta,
        secondaryColorPreview = EarthForestGreen,
        bgColorPreview = EarthBg
    ),
    NEON_POP(
        displayName = "Neon Emerald & Violet",
        subtitle = "Fintech & Crypto (#000000 • Radium Emerald • Deep Violet)",
        isDarkPalette = true,
        primaryColorPreview = NeonPopEmerald,
        secondaryColorPreview = NeonPopVioletAccent,
        bgColorPreview = NeonPopBg
    ),
    SYSTEM(
        displayName = "System Default",
        subtitle = "Follows Android System dark/light mode",
        isDarkPalette = true,
        primaryColorPreview = CyberEmerald,
        secondaryColorPreview = CyberNeonPurple,
        bgColorPreview = CyberBg
    ),
    // Backward compatibility aliases
    DARK(
        displayName = "Dark Mode",
        subtitle = "Dark Emerald aesthetic",
        isDarkPalette = true,
        primaryColorPreview = CyberEmerald,
        secondaryColorPreview = CyberNeonPurple,
        bgColorPreview = CyberBg
    ),
    LIGHT(
        displayName = "Light Mode",
        subtitle = "Soft Sage & Peach aesthetic",
        isDarkPalette = false,
        primaryColorPreview = SoftTechActionGreen,
        secondaryColorPreview = SoftTechLightPeach,
        bgColorPreview = SoftTechBg
    )
}

// 1. Dark Emerald & Violet Color Scheme (#121212, #00E676, #9D4EDD) - NO BLUES
val DarkCyberColorScheme = darkColorScheme(
    primary = CyberEmerald, // #00E676
    onPrimary = Color(0xFF000000),
    primaryContainer = CyberEmeraldContainer,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = CyberNeonPurple, // #9D4EDD
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = CyberNeonPurpleContainer,
    onSecondaryContainer = Color(0xFFF3E5F5),
    tertiary = AmberWarning, // #FFB703
    onTertiary = Color(0xFF000000),
    background = CyberBg, // #121212
    onBackground = Color(0xFFF8FAFC),
    surface = CyberSurface, // #1E1E1E
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = CyberSurfaceVariant, // #282828
    onSurfaceVariant = Color(0xFFA0AEC0),
    outline = CyberBorder, // #383838
    outlineVariant = Color(0xFF2A2A2A)
)

// 2. Soft Tech Color Scheme (#F4F6F9, #FFD1BA, #059669) - NO BLUES
val SoftTechColorScheme = lightColorScheme(
    primary = SoftTechActionGreen, // #059669 Fresh Emerald Forest
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = SoftTechMint, // #A7F3D0
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = SoftTechPeachDark,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = SoftTechLightPeach, // #FFD1BA
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = AmberWarning,
    onTertiary = Color(0xFF000000),
    background = SoftTechBg, // #F4F6F9
    onBackground = Color(0xFF0F172A),
    surface = SoftTechSurface, // #FFFFFF
    onSurface = Color(0xFF0F172A),
    surfaceVariant = SoftTechSurfaceVariant, // #EBF1F7
    onSurfaceVariant = Color(0xFF475569),
    outline = SoftTechBorder,
    outlineVariant = Color(0xFFE2E8F0)
)

// 3. Organic Earth Color Scheme (#F9F6F0, #E07A5F, #4A6B4B) - NO BLUES
val OrganicEarthColorScheme = lightColorScheme(
    primary = EarthTerracotta, // #E07A5F
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = EarthTerracottaContainer,
    onPrimaryContainer = Color(0xFF4A1A0C),
    secondary = EarthForestGreen, // #4A6B4B
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = EarthForestContainer,
    onSecondaryContainer = Color(0xFF1B381C),
    tertiary = AmberWarning,
    onTertiary = Color(0xFF000000),
    background = EarthBg, // #F9F6F0
    onBackground = Color(0xFF293241),
    surface = EarthSurface, // #FFFFFF
    onSurface = Color(0xFF1E293B),
    surfaceVariant = EarthSurfaceVariant,
    onSurfaceVariant = Color(0xFF5C6B73),
    outline = EarthBorder,
    outlineVariant = Color(0xFFEAE2D5)
)

// 4. Neon Pop Color Scheme (#000000, #00E676, #A855F7) - NO BLUES
val NeonPopColorScheme = darkColorScheme(
    primary = NeonPopEmerald, // #00E676 Radium Bright Green
    onPrimary = Color(0xFF000000),
    primaryContainer = NeonPopEmeraldContainer,
    onPrimaryContainer = Color(0xFF69F0AE),
    secondary = NeonPopVioletAccent, // #A855F7
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = NeonPopDeepViolet, // #2E1065
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = AmberWarning,
    onTertiary = Color(0xFF000000),
    background = NeonPopBg, // #000000 OLED Matte Black
    onBackground = Color(0xFFFFFFFF),
    surface = NeonPopSurface, // #0D1117
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = NeonPopSurfaceVariant, // #161B22
    onSurfaceVariant = Color(0xFF8B949E),
    outline = NeonPopBorder,
    outlineVariant = Color(0xFF21262D)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK_CYBER,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.DARK_CYBER -> DarkCyberColorScheme
        AppThemeMode.SOFT_TECH -> SoftTechColorScheme
        AppThemeMode.ORGANIC_EARTH -> OrganicEarthColorScheme
        AppThemeMode.NEON_POP -> NeonPopColorScheme
        AppThemeMode.DARK -> DarkCyberColorScheme
        AppThemeMode.LIGHT -> SoftTechColorScheme
        AppThemeMode.SYSTEM -> if (isSystemDark) DarkCyberColorScheme else SoftTechColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
