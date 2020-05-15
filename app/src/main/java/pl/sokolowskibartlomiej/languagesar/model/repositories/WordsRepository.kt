package pl.sokolowskibartlomiej.languagesar.model.repositories

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import pl.sokolowskibartlomiej.languagesar.db.WordsDatabase
import pl.sokolowskibartlomiej.languagesar.db.daos.WordsDao
import pl.sokolowskibartlomiej.languagesar.db.entities.DetectedObjectEntity

class WordsRepository(application: Application) {

    private var mWordsDao: WordsDao
    private var mAllWords: LiveData<List<DetectedObjectEntity>>

    init {
        val db = WordsDatabase.getInstance(application)!!
        mWordsDao = db.wordsDao()
        mAllWords = mWordsDao.getAllDetectedObjects()
    }

    fun getAllDetectedObjects(): LiveData<List<DetectedObjectEntity>> = mAllWords

    fun getObjectsByTargetLang(targetLang: String) = mWordsDao.getObjectsByTargetLang(targetLang)

    @WorkerThread
    suspend fun insertDetectedObject(detectedObject: DetectedObjectEntity) =
        mWordsDao.insertDetectedObject(detectedObject)

    @WorkerThread
    suspend fun deleteDetectedObject(detectedObject: DetectedObjectEntity) =
        mWordsDao.deleteDetectedObject(detectedObject)
}