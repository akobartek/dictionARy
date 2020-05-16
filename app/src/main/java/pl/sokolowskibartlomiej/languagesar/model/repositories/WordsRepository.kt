package pl.sokolowskibartlomiej.languagesar.model.repositories

import android.app.Application
import androidx.annotation.WorkerThread
import pl.sokolowskibartlomiej.languagesar.db.WordsDatabase
import pl.sokolowskibartlomiej.languagesar.db.daos.WordsDao
import pl.sokolowskibartlomiej.languagesar.db.entities.DetectedObject
import pl.sokolowskibartlomiej.languagesar.db.entities.DictionaryWord

class WordsRepository(application: Application) {

    private var mWordsDao: WordsDao = WordsDatabase.getInstance(application)!!.wordsDao()

    fun getObjectsByTargetLang(targetLang: String) =
        mWordsDao.getObjectsByTargetLang(targetLang)

    @WorkerThread
    suspend fun insertDetectedObject(detectedObject: DetectedObject) =
        mWordsDao.insertDetectedObject(detectedObject)

    @WorkerThread
    suspend fun deleteDetectedObject(detectedObject: DetectedObject) =
        mWordsDao.deleteDetectedObject(detectedObject)

    fun getWordsByLanguage(language: String) =
        mWordsDao.getWordsByLanguage(language)

    @WorkerThread
    suspend fun insertWord(word: DictionaryWord) = mWordsDao.insertWord(word)

    @WorkerThread
    suspend fun updateWord(word: DictionaryWord) = mWordsDao.updateWord(word)

    @WorkerThread
    suspend fun deleteWord(word: DictionaryWord) = mWordsDao.deleteWord(word)
}