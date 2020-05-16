package pl.sokolowskibartlomiej.languagesar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.sokolowskibartlomiej.languagesar.model.repositories.WordsRepository
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager

class MainViewModel(val app: Application) : AndroidViewModel(app) {

    private val mDatabaseRepository = WordsRepository(app)

    val isDictionaryReady =
        MutableLiveData(PreferencesManager.getSelectedLanguage() != "")
}