package pl.sokolowskibartlomiej.languagesar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.db.entities.Word
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_NONE
import pl.sokolowskibartlomiej.languagesar.model.repositories.WordsRepository
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import java.io.BufferedReader
import java.io.InputStreamReader

class DictionaryViewModel(val app: Application) : AndroidViewModel(app) {

    private val mWordsRepository = WordsRepository(app)

    fun getWordsFromDatabase() =
        mWordsRepository.getWordsLiveDataByLanguage(PreferencesManager.getSelectedLanguage())

    fun insertWordsFromFileToDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val words = arrayListOf<Word>()
            val language = PreferencesManager.getSelectedLanguage()
            val reader = BufferedReader(InputStreamReader(app.assets.open("words-${language}.txt")))
            var line = reader.readLine()
            while (line != null) {
                words.add(Word(word = line.trim(), language = language))
                line = reader.readLine()
            }
            reader.close()
            mWordsRepository.insertMultipleWords(words)
            PreferencesManager.wordsAddedToDatabase()
        }
    }

    fun insertWordAsync(word: String, translation: String, isSaved: Boolean): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            mWordsRepository.insertWord(
                Word(
                    word = "$word - $translation - $translation",
                    language = PreferencesManager.getSelectedLanguage(),
                    status = if (isSaved) Word.WORD_STATUS_SAVED else WORD_STATUS_NONE
                )
            )
        }

    fun updateWordStatusAsync(word: Word, status: Int): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            word.status = status
            mWordsRepository.updateWord(word)
        }

    fun deleteWordAsync(word: Word): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) { mWordsRepository.deleteWord(word) }
}