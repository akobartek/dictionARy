package pl.sokolowskibartlomiej.languagesar.model.repositories

import android.app.Application
import androidx.annotation.WorkerThread
import pl.sokolowskibartlomiej.languagesar.db.WordsDatabase
import pl.sokolowskibartlomiej.languagesar.db.daos.WordsDao
import pl.sokolowskibartlomiej.languagesar.db.entities.Word

class WordsRepository(application: Application) {

    private var mWordsDao: WordsDao = WordsDatabase.getInstance(application)!!.wordsDao()

    fun getWordsByLanguage(language: String) =
        mWordsDao.getWordsByLanguage(language)

    @WorkerThread
    suspend fun insertWord(word: Word) = mWordsDao.insertWord(word)

    @WorkerThread
    suspend fun insertMultipleWords(words: List<Word>) =
        mWordsDao.insertMultipleWords(*words.toTypedArray())

    @WorkerThread
    suspend fun updateWord(word: Word) = mWordsDao.updateWord(word)

    @WorkerThread
    suspend fun deleteWord(word: Word) = mWordsDao.deleteWord(word)
}