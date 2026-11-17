package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import com.example.model.Language
import java.util.Locale

/**
 * Helper responsible for persisting the selected [Language], resolving its [Locale],
 * and wrapping [Context] / [Configuration] so Android resource lookups (`strings.xml` vs
 * `values-pt/strings.xml`) update dynamically at runtime without breaking `findActivity()`.
 */
object AppLocaleHelper {

    const val PREFS_NAME = "devroutine_prefs"
    const val PREF_KEY_APP_LANG = "app_lang"

    fun toLocale(language: Language): Locale = when (language) {
        Language.PT -> Locale("pt", "BR")
        Language.EN -> Locale("en", "US")
    }

    fun loadSavedLanguage(prefs: SharedPreferences): Language {
        val code = prefs.getString(PREF_KEY_APP_LANG, Language.PT.code)
        return if (code.equals(Language.EN.code, ignoreCase = true)) Language.EN else Language.PT
    }

    fun saveLanguage(prefs: SharedPreferences, language: Language) {
        prefs.edit().putString(PREF_KEY_APP_LANG, language.code).apply()
        Locale.setDefault(toLocale(language))
    }

    fun createLocalizedConfiguration(
        baseConfiguration: Configuration,
        language: Language
    ): Configuration {
        val targetLocale = toLocale(language)
        return Configuration(baseConfiguration).apply {
            setLocale(targetLocale)
            setLayoutDirection(targetLocale)
        }
    }

    fun createLocalizedContext(baseContext: Context, language: Language): Context {
        val targetLocale = toLocale(language)
        Locale.setDefault(targetLocale)
        val config = createLocalizedConfiguration(baseContext.resources.configuration, language)
        val configResources = baseContext.createConfigurationContext(config).resources
        return LocalizedContextWrapper(baseContext, configResources)
    }

    fun wrapBaseContext(newBase: Context): Context {
        val prefs = newBase.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLanguage = loadSavedLanguage(prefs)
        return createLocalizedContext(newBase, savedLanguage)
    }
}

/**
 * Wraps [base] while overriding only [getResources] and [getAssets] so callers that
 * traverse `ContextWrapper.baseContext` (such as Google Play Billing or CredentialManager
 * resolving the hosting `Activity`) continue working seamlessly.
 */
private class LocalizedContextWrapper(
    base: Context,
    private val localizedResources: Resources
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
    override fun getAssets(): AssetManager = localizedResources.assets
}
