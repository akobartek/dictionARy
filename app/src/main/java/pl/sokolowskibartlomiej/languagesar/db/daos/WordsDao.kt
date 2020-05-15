package pl.sokolowskibartlomiej.languagesar.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pl.sokolowskibartlomiej.languagesar.db.entities.DetectedObjectEntity

@Dao
interface WordsDao {

    @Query("SELECT * FROM words_table")
    fun getAllDetectedObjects(): LiveData<List<DetectedObjectEntity>>

    @Query("SELECT * FROM words_table WHERE target_lang = :targetLang")
    fun getObjectsByTargetLang(targetLang: String): LiveData<List<DetectedObjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectedObject(detectedObject: DetectedObjectEntity)

    @Delete
    suspend fun deleteDetectedObject(detectedObject: DetectedObjectEntity)
}