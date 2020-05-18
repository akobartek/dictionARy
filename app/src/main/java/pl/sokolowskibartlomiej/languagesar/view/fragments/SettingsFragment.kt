package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        val toolbar = root?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.settingsToolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar?.setNavigationOnClickListener { findNavController().navigateUp() }
        return root
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        preferenceManager
            .findPreference<Preference>(getString(R.string.night_mode_key))
            ?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            activity?.let {
                AppCompatDelegate.setDefaultNightMode(
                    if (newValue as Boolean) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                PreferencesManager.setNightMode(newValue)
            }
            true
        }

        val languagePreference =
            findPreference<ListPreference>(getString(R.string.selected_language_key))!!
        languagePreference.value = PreferencesManager.getSelectedLanguage()
        languagePreference.setEntries(
            if (PreferencesManager.getUserLanguage().contains("en")) R.array.languages_labels_en
            else R.array.languages_labels_pl
        )
        languagePreference.setEntryValues(
            if (PreferencesManager.getUserLanguage().contains("en")) R.array.languages_values_en
            else R.array.languages_values_pl
        )
        languagePreference.summary =
            languagePreference.entries[languagePreference.findIndexOfValue(PreferencesManager.getSelectedLanguage())]
        languagePreference.setOnPreferenceChangeListener { _, newValue ->
            PreferencesManager.setSelectedLanguage(newValue as String)
            languagePreference.summary =
                languagePreference.entries[languagePreference.findIndexOfValue(newValue)]
            true
        }
    }
}
