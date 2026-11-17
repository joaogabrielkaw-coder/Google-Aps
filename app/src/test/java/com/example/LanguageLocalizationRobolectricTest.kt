package com.example

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.model.Language
import com.example.model.ThemePreference
import com.example.ui.AppDestination
import com.example.ui.components.AppBottomNav
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DevRoutineTheme
import com.example.util.AppLocaleHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LanguageLocalizationRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun localizedResources_resolveDistinctPortugueseAndEnglishStringsFromXml() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()

        val enContext = AppLocaleHelper.createLocalizedContext(baseContext, Language.EN)
        val ptContext = AppLocaleHelper.createLocalizedContext(baseContext, Language.PT)

        assertEquals("Home", enContext.getString(R.string.nav_home))
        assertEquals("Início", ptContext.getString(R.string.nav_home))

        assertEquals("Your Dev Ecosystem", enContext.getString(R.string.login_hero_title))
        assertEquals("Seu Ecossistema Dev", ptContext.getString(R.string.login_hero_title))

        assertEquals("Study Task Progress by Language", enContext.getString(R.string.tracker_section_title))
        assertEquals("Progresso de Tarefas por Linguagem", ptContext.getString(R.string.tracker_section_title))

        assertEquals("+ New Routine", enContext.getString(R.string.home_new_routine_btn))
        assertEquals("+ Nova Rotina", ptContext.getString(R.string.home_new_routine_btn))

        assertEquals(
            "Active resource: res/values/strings.xml (en)",
            enContext.getString(R.string.settings_lang_active_res_badge)
        )
        assertEquals(
            "Recurso ativo: res/values-pt/strings.xml (pt)",
            ptContext.getString(R.string.settings_lang_active_res_badge)
        )

        assertNotEquals(
            enContext.getString(R.string.settings_title),
            ptContext.getString(R.string.settings_title)
        )
    }

    @Test
    fun wrapBaseContext_persistsSavedLanguageAndPreservesBaseContextIdentity() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val prefs = appContext.getSharedPreferences(AppLocaleHelper.PREFS_NAME, Context.MODE_PRIVATE)
        AppLocaleHelper.saveLanguage(prefs, Language.EN)

        val wrapped = AppLocaleHelper.wrapBaseContext(appContext)
        assertEquals("Home", wrapped.getString(R.string.nav_home))
        assertSame(appContext, (wrapped as ContextWrapper).baseContext)

        AppLocaleHelper.saveLanguage(prefs, Language.PT)
        val wrappedPt = AppLocaleHelper.wrapBaseContext(appContext)
        assertEquals("Início", wrappedPt.getString(R.string.nav_home))
    }

    @Test
    fun composeLanguageToggle_updatesBottomNavAndSettingsStringsDynamically() {
        composeTestRule.setContent {
            val baseContext = LocalContext.current
            val baseConfiguration = LocalConfiguration.current
            var currentLanguage by remember { mutableStateOf(Language.PT) }

            val localizedConfiguration = remember(baseConfiguration, currentLanguage) {
                AppLocaleHelper.createLocalizedConfiguration(baseConfiguration, currentLanguage)
            }
            val localizedContext = remember(baseContext, currentLanguage) {
                AppLocaleHelper.createLocalizedContext(baseContext, currentLanguage)
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext
            ) {
                DevRoutineTheme {
                    Scaffold(
                        bottomBar = {
                            AppBottomNav(
                                currentDestination = AppDestination.SETTINGS,
                                language = currentLanguage,
                                onNavigate = {}
                            )
                        }
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            SettingsScreen(
                                currentLanguage = currentLanguage,
                                currentTheme = ThemePreference.DARK,
                                onSetLanguage = { currentLanguage = it },
                                onSetTheme = {}
                            )
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Início").assertIsDisplayed()
        composeTestRule.onNodeWithTag("screen_settings")
            .performScrollToNode(hasTestTag("card_settings_language_toggle"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Idioma do Aplicativo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recurso ativo: res/values-pt/strings.xml (pt)")
            .performScrollTo()
            .assertIsDisplayed()

        // Toggle switch from PT -> EN
        composeTestRule.onNodeWithTag("switch_toggle_language").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("App Language").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Active resource: res/values/strings.xml (en)")
            .performScrollTo()
            .assertIsDisplayed()

        // Tap Portuguese card to switch back EN -> PT
        composeTestRule.onNodeWithTag("lang_option_pt").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Início").assertIsDisplayed()
        composeTestRule.onNodeWithText("Idioma do Aplicativo").performScrollTo().assertIsDisplayed()
    }
}
