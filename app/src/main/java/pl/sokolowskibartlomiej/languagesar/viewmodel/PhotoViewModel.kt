package pl.sokolowskibartlomiej.languagesar.viewmodel

import android.app.Application
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.BuildConfig
import pl.sokolowskibartlomiej.languagesar.apicalls.RetrofitClient
import pl.sokolowskibartlomiej.languagesar.apicalls.translate.TranslateRepository
import pl.sokolowskibartlomiej.languagesar.db.entities.DetectedObject
import pl.sokolowskibartlomiej.languagesar.model.repositories.WordsRepository
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import java.util.*
import kotlin.collections.ArrayList

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
                val targetTranslation =
                    mTranslateRepository.translateText(
                        text, "en", PreferencesManager.getSelectedLanguage()
                    )
                selectedLabel = 0
                translation.postValue(Pair(userTranslation, targetTranslation))
            } catch (exc: JsonEncodingException) {
                if (BuildConfig.DEBUG) Log.e("fetchTranslation", exc.toString())
                translation.postValue(null)
            }
        }
    }

    fun insertWord() {
        val wordEntity = DetectedObject(
            label = objectsLabels.value!![selectedLabel],
            sourceLang = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].language,
            sourceTranslation = translation.value!!.first.split(", ")[selectedLabel],
            targetLang = PreferencesManager.getSelectedLanguage(),
            targetTranslation = translation.value!!.second.split(", ")[selectedLabel]
        )
        viewModelScope.launch(Dispatchers.IO) { mDatabaseRepository.insertDetectedObject(wordEntity) }
    }
}