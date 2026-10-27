package com.example.grimoire.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeMode(
    val title: String,
    val subtitle: String,
    val previewPrimary: Color,
    val previewBackground: Color,
    val previewSurface: Color,
    val previewAccent: Color
) {
    DARK(
        title = "Dark Mode",
        subtitle = "Deep slate tones reducing glare & eye strain",
        previewPrimary = Color(0xFFD0BCFF),
        previewBackground = Color(0xFF131316),
        previewSurface = Color(0xFF202024),
        previewAccent = Color(0xFFD4AF37)
    ),
    LIGHT(
        title = "Light Mode",
        subtitle = "Crisp bright surfaces with high ambient legibility",
        previewPrimary = Color(0xFF6650A4),
        previewBackground = Color(0xFFFCF8FF),
        previewSurface = Color(0xFFF0ECF4),
        previewAccent = Color(0xFFD4AF37)
    ),
    AMOLED(
        title = "AMOLED Mode",
        subtitle = "Pure pitch black (#000000) for OLED panels & battery efficiency",
        previewPrimary = Color(0xFFD4AF37),
        previewBackground = Color(0xFF000000),
        previewSurface = Color(0xFF0F0F0F),
        previewAccent = Color(0xFF38BDF8)
    ),
    EXPRESSIVE(
        title = "MD3E Expressive",
        subtitle = "Material 3 Expressive vibrant chroma & jewel plum undertones",
        previewPrimary = Color(0xFF9D65FF),
        previewBackground = Color(0xFF0E0A17),
        previewSurface = Color(0xFF1F1730),
        previewAccent = Color(0xFFFF5277)
    ),
    SYSTEM(
        title = "Follow System",
        subtitle = "Adapts automatically to system daylight / night schedule",
        previewPrimary = Color(0xFFD0BCFF),
        previewBackground = Color(0xFF1B1B1F),
        previewSurface = Color(0xFF2A2A2E),
        previewAccent = Color(0xFFD4AF37)
    );

    fun isDark(systemDark: Boolean): Boolean {
        return when (this) {
            DARK, AMOLED, EXPRESSIVE -> true
            LIGHT -> false
            SYSTEM -> systemDark
        }
    }
}
