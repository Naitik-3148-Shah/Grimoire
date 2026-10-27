package com.example.grimoire

import androidx.compose.ui.graphics.Color
import com.example.grimoire.ui.theme.AmoledBackground
import com.example.grimoire.ui.theme.AmoledColorScheme
import com.example.grimoire.ui.theme.AppThemeMode
import com.example.grimoire.ui.theme.DarkColorScheme
import com.example.grimoire.ui.theme.ExpressiveColorScheme
import com.example.grimoire.ui.theme.LightBackground
import com.example.grimoire.ui.theme.LightColorScheme
import com.example.grimoire.ui.theme.Md3eBackground
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {

    @Test
    fun testDarkModeIsAlwaysDark() {
        assertTrue(AppThemeMode.DARK.isDark(systemDark = false))
        assertTrue(AppThemeMode.DARK.isDark(systemDark = true))
    }

    @Test
    fun testLightModeIsNeverDark() {
        assertFalse(AppThemeMode.LIGHT.isDark(systemDark = false))
        assertFalse(AppThemeMode.LIGHT.isDark(systemDark = true))
    }

    @Test
    fun testAmoledModeIsAlwaysDark() {
        assertTrue(AppThemeMode.AMOLED.isDark(systemDark = false))
        assertTrue(AppThemeMode.AMOLED.isDark(systemDark = true))
    }

    @Test
    fun testExpressiveModeIsAlwaysDark() {
        assertTrue(AppThemeMode.EXPRESSIVE.isDark(systemDark = false))
        assertTrue(AppThemeMode.EXPRESSIVE.isDark(systemDark = true))
    }

    @Test
    fun testSystemModeFollowsSystemDarkState() {
        assertFalse(AppThemeMode.SYSTEM.isDark(systemDark = false))
        assertTrue(AppThemeMode.SYSTEM.isDark(systemDark = true))
    }

    @Test
    fun testAllThemeModesHaveValidMetadata() {
        val allModes = AppThemeMode.values()
        assertEquals(5, allModes.size)

        for (mode in allModes) {
            assertTrue(mode.title.isNotBlank())
            assertTrue(mode.subtitle.isNotBlank())
            assertNotNull(mode.previewPrimary)
            assertNotNull(mode.previewBackground)
            assertNotNull(mode.previewSurface)
            assertNotNull(mode.previewAccent)
        }
    }

    @Test
    fun testAmoledColorsArePureBlack() {
        assertEquals(Color(0xFF000000), AmoledBackground)
        assertEquals(Color(0xFF000000), AmoledColorScheme.background)
        assertEquals(Color(0xFF000000), AmoledColorScheme.surface)
    }

    @Test
    fun testColorSchemesIntegrity() {
        assertEquals(LightBackground, LightColorScheme.background)
        assertEquals(Md3eBackground, ExpressiveColorScheme.background)
        assertNotNull(DarkColorScheme.primary)
        assertNotNull(ExpressiveColorScheme.primary)
    }
}
