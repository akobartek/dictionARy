package pl.sokolowskibartlomiej.languagesar.utils

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.preference.PreferenceManager
import pl.sokolowskibartlomiej.languagesar.LanguagesARApplication

object PreferencesManager {

    private const val NIGHT_MODE = "night_mode"
    private const val SELECTED_LANGUAGE = "selected_language"
    private val sharedPref: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(LanguagesARApplication.instance)

    fun getNightMode() = sharedPref.getBoolean(NIGHT_MODE, false)

    fun setNightMode(newValue: Boolean) {
        sharedPref.edit()
            .putBoolean(NIGHT_MODE, newValue)
            .apply()
    }

    fun getUserLanguage(): String =
        ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].language

    fun getSelectedLanguage() = sharedPref.getString(SELECTED_LANGUAGE, "")!!

    fun setSelectedLanguage(languageCode: String) {
        sharedPref.edit()
            .putString(SELECTED_LANGUAGE, languageCode)
            .apply()
    }

    fun areWordsInDatabase() = sharedPref.getBoolean("words_$${getSelectedLanguage()}", false)

    fun wordsAddedToDatabase() {
        sharedPref.edit()
            .putBoolean("words_$${getSelectedLanguage()}", true)
            .apply()
    }
}