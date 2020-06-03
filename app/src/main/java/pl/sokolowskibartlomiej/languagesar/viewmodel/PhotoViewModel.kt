package pl.sokolowskibartlomiej.languagesar.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.BuildConfig
import pl.sokolowskibartlomiej.languagesar.apicalls.RetrofitClient
import pl.sokolowskibartlomiej.languagesar.apicalls.translate.TranslateRepository
import pl.sokolowskibartlomiej.languagesar.db.entities.Word
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_SAVED
import pl.sokolowskibartlomiej.languagesar.model.repositories.WordsRepository
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import java.util.*

class PhotoViewModel(val app: Application) : AndroidViewModel(app) {

    private val mTranslateRepository = TranslateRepository(RetrofitClient.translateApi)
    private val mDatabaseRepository = WordsRepository(app)

    val latestPhoto = MutableLiveData<Bitmap>()
    val objectsLabels = MutableLiveData<ArrayList<String>>()
    val translation = MutableLiveData<Pair<String, String>>()
    var selectedLabel = 0

    fun fetchLabelsTranslation(labels: ArrayList<String>) {
        val text = labels.joinToString(", ").toLowerCase(Locale.getDefault())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userLangCode = PreferencesManager.getUserLanguage()
                val userTranslation =
                    if (userLangCode.contains("en")) text
                    else mTranslateRepository.translateText(text, "en", userLangCode)
                val targetTranslation = PreferencesManager.getSelectedLanguage().let {
                    if (it != "en") mTranslateRepository.translateText(text, "en", it)
                    else text
                }
                selectedLabel = 0
                translation.postValue(Pair(userTranslation, targetTranslation))
            } catch (exc: Throwable) {
                if (BuildConfig.DEBUG) Log.e("fetchTranslation", exc.toString())
                translation.postValue(null)
            }
        }
    }

    fun insertWord() {
        val sourceText = translation.value!!.first.split(", ")[selectedLabel]
        val translation = translation.value!!.second.split(", ")[selectedLabel]
        val word = Word(
            word = "$translation - $sourceText - $sourceText",
            language = PreferencesManager.getSelectedLanguage(),
            status = WORD_STATUS_SAVED
        )
        viewModelScope.launch(Dispatchers.IO) { mDatabaseRepository.insertWord(word) }
    }
}