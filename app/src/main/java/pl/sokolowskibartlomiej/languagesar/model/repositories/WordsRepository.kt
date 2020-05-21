package pl.sokolowskibartlomiej.languagesar.model.repositories

import android.app.Application
import androidx.annotation.WorkerThread
import pl.sokolowskibartlomiej.languagesar.db.WordsDatabase
import pl.sokolowskibartlomiej.languagesar.db.daos.WordsDao
import pl.sokolowskibartlomiej.languagesar.db.entities.Word

class WordsRepository(application: Application) {

    private var mWordsDao: WordsDao = WordsDatabase.getInstance(application)!!.wordsDao()

    fun getWordsLiveDataByLanguage(language: String) =
        mWordsDao.getWordsLiveDataByLanguage(language)

    @WorkerThread
    suspend fun getWordsListByLanguage(language: String) =
        mWordsDao.getWordsListByLanguage(language)

    @WorkerThread
    suspend fun insertWord(word: Word) = mWordsDao.insertWord(word)

    @WorkerThread
    suspend fun insertMultipleWords(words: List<Word>) =
        mWordsDao.insertMultipleWords(*words.toTypedArray())

    @WorkerThread
    suspend fun updateWord(word: Word) = mWordsDao.updateWord(word)

    @WorkerThread
    suspend fun updateWordById(id: Int, status: Int) = mWordsDao.updateWordById(id, status)

    @WorkerThread
    suspend fun deleteWord(word: Word) = mWordsDao.deleteWord(word)

    @WorkerThread
    suspend fun getCountOfWords(language: String) = mWordsDao.getCountOfWords(language)

    @WorkerThread
    suspend fun getCountByStatus(language: String, status: Int) =
        mWordsDao.getCountByStatus(language, status)
}