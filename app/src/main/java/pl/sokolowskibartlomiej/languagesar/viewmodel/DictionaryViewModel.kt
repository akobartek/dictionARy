package pl.sokolowskibartlomiej.languagesar.viewmodel

import android.app.Application
import android.content.res.AssetManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.db.entities.Word
import pl.sokolowskibartlomiej.languagesar.model.repositories.WordsRepository
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import java.io.BufferedReader
import java.io.InputStreamReader

class DictionaryViewModel(val app: Application) : AndroidViewModel(app) {

    private val mWordsRepository = WordsRepository(app)

    fun getWordsFromDatabase() =
        mWordsRepository.getWordsByLanguage(PreferencesManager.getSelectedLanguage())

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
}