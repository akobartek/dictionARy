package pl.sokolowskibartlomiej.languagesar.utils

import android.content.SharedPreferences
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

    // TODO() -> Set default language to ""
    fun getSelectedLanguage() = sharedPref.getString(SELECTED_LANGUAGE, "it")!!

    fun setSelectedLanguage(newValue: String) {
        sharedPref.edit()
            .putString(SELECTED_LANGUAGE, newValue)
            .apply()
    }
}