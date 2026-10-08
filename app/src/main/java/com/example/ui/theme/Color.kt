package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PALETTE 1: DARK EMERALD & VIOLET (AI, Tech & Portfolios) - #121212, #00E676, #9D4EDD
// (Replaced skyblue/blue with vibrant Radium Emerald & Neon Purple)
// ============================================================================
val CyberBg = Color(0xFF121212)
val CyberSurface = Color(0xFF1E1E1E)
val CyberSurfaceVariant = Color(0xFF282828)
val CyberBorder = Color(0xFF383838)

// Radium Emerald (#00E676): High-energy, eye-friendly, dynamic actions
val CyberEmerald = Color(0xFF00E676)
val CyberEmeraldDark = Color(0xFF00C853)
val CyberEmeraldContainer = Color(0xFF003816)

// Neon Purple (#9D4EDD): Sub-elements, active tabs, premium hyper-tech touch
val CyberNeonPurple = Color(0xFF9D4EDD)
val CyberNeonPurpleLight = Color(0xFFC77DFF)
val CyberNeonPurpleContainer = Color(0xFF38006B)

// ============================================================================
// PALETTE 2: SOFT TECH (Wellness, SaaS & Medical) - #F4F6F9, #FFD1BA, #059669
// (Replaced blue with soothing Mint Green & Light Peach)
// ============================================================================
val SoftTechBg = Color(0xFFF4F6F9)
val SoftTechSurface = Color(0xFFFFFFFF)
val SoftTechSurfaceVariant = Color(0xFFEBF1F7)
val SoftTechBorder = Color(0xFFD6E2EE)

// Light Peach (#FFD1BA): Warm pastel tone for friendly, human wellness vibe
val SoftTechLightPeach = Color(0xFFFFD1BA)
val SoftTechPeachDark = Color(0xFFD97706)
val SoftTechPeachContainer = Color(0xFFFFE8DC)

// Soothing Mint Sage (#10B981 & #059669): Replaced blue with fresh mint green
val SoftTechMint = Color(0xFFA7F3D0)
val SoftTechActionGreen = Color(0xFF059669)
val SoftTechGreenContainer = Color(0xFFD1FAE5)

// ============================================================================
// PALETTE 3: ORGANIC EARTH (E-commerce & Lifestyle) - #F9F6F0, #E07A5F, #4A6B4B
// (Replaced slate blue with Forest Olive Green)
// ============================================================================
val EarthBg = Color(0xFFF9F6F0)
val EarthSurface = Color(0xFFFFFFFF)
val EarthSurfaceVariant = Color(0xFFEFE8DD)
val EarthBorder = Color(0xFFDDD4C5)

// Terracotta Clay (#E07A5F): Warm brown-orange grounded earth tone
val EarthTerracotta = Color(0xFFE07A5F)
val EarthTerracottaLight = Color(0xFFF4A261)
val EarthTerracottaContainer = Color(0xFFFFDDD2)

// Forest Olive Green (#4A6B4B): Replaced blue with mature, grounded botanical green
val EarthForestGreen = Color(0xFF4A6B4B)
val EarthForestGreenLight = Color(0xFF6B8F6C)
val EarthForestContainer = Color(0xFFE2EBD8)

// ============================================================================
// PALETTE 4: NEON POP (Fintech & Crypto) - #000000, #00E676, #9D4EDD
// ============================================================================
val NeonPopBg = Color(0xFF000000)
val NeonPopSurface = Color(0xFF0D1117)
val NeonPopSurfaceVariant = Color(0xFF161B22)
val NeonPopBorder = Color(0xFF30363D)

// Emerald Green (#00E676): Radium-style bright green
val NeonPopEmerald = Color(0xFF00E676)
val NeonPopEmeraldContainer = Color(0xFF003816)

// Deep Violet (#2E1065 & #A855F7): Replaced navy blue with deep obsidian violet
val NeonPopDeepViolet = Color(0xFF2E1065)
val NeonPopVioletAccent = Color(0xFFA855F7)
val NeonPopVioletContainer = Color(0xFF3B0764)

// ============================================================================
// UNIVERSAL STATE & ACCENT COLORS (NO BLUES)
// ============================================================================
val EmeraldSuccess = Color(0xFF00E676)
val EmeraldSuccessLight = Color(0xFF69F0AE)
val AmberWarning = Color(0xFFFFB703)
val RoseDanger = Color(0xFFFF3366)
val VioletFocus = Color(0xFF9D4EDD)

// Brand Defaults mapped to Emerald & Violet
val PrimaryIndigo = CyberEmerald
val PrimaryIndigoLight = CyberEmerald
val PrimaryIndigoContainerDark = CyberEmeraldContainer
val PrimaryIndigoContainerLight = Color(0xFFD1FAE5)

val SecondaryAzure = CyberNeonPurple
val SecondaryAzureLight = CyberNeonPurple
val SecondaryAzureContainerDark = CyberNeonPurpleContainer
val SecondaryAzureContainerLight = Color(0xFFF3E5F5)

// Standard Aliases (Replaced skyblue/cyan and blues with Emerald, Purple, Amber)
val CyanAccent = CyberEmerald // Replaced skyblue with vibrant Radium Emerald
val CyanAccentGlow = Color(0x4000E676)
val PurpleAccent = CyberNeonPurple
val AmberAccent = AmberWarning
val EmeraldAccent = EmeraldSuccess
val BlueAccent = AmberWarning // Replaced blue with Golden Amber
val RoseAccent = RoseDanger

// Text Colors (High Contrast & Legibility across Dark and Light modes)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFFA0AEC0)
val TextMutedDark = Color(0xFF718096)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextMutedLight = Color(0xFF94A3B8)

// Default text colors
val TextPrimary = TextPrimaryDark
val TextSecondary = TextSecondaryDark
val TextMuted = TextMutedDark

// Backwards compatibility mappings for dialogs and components
val BgDark = CyberBg
val SurfaceDark = CyberSurface
val SurfaceVariantDark = CyberSurfaceVariant
val SurfaceBorderDark = CyberBorder

val BgLight = SoftTechBg
val SurfaceLight = SoftTechSurface
val SurfaceVariantLight = SoftTechSurfaceVariant
val SurfaceBorderLight = SoftTechBorder

val ActiveCardBgDark = Color(0xFF1E241E) // Neutral charcoal with hint of emerald
val ActiveCardBgLight = Color(0xFFF2F7F2)
val ActiveCardBg = ActiveCardBgDark
val ActiveCardBorder = CyberEmerald // Replaced skyblue with Emerald
