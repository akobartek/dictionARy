package pl.sokolowskibartlomiej.languagesar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_KNOWN
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_SAVED
import pl.sokolowskibartlomiej.languagesar.model.repositories.WordsRepository
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager

class TestStartViewModel(val app: Application) : AndroidViewModel(app) {

    private val mWordsRepository = WordsRepository(app)

    val numberOfResults = MutableLiveData<Array<Int>>(null)

    fun fetchNumberOfResults() {
        viewModelScope.launch(Dispatchers.IO) {
            val language = PreferencesManager.getSelectedLanguage()
            numberOfResults.postValue(
                arrayOf(
                    mWordsRepository.getCountOfWords(language),
                    mWordsRepository.getCountByStatus(language, WORD_STATUS_SAVED),
                    mWordsRepository.getCountByStatus(language, WORD_STATUS_KNOWN)
                )
            )
        }
    }
}