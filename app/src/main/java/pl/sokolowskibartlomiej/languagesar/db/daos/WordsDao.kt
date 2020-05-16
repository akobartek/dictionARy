package pl.sokolowskibartlomiej.languagesar.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pl.sokolowskibartlomiej.languagesar.db.entities.DetectedObject
import pl.sokolowskibartlomiej.languagesar.db.entities.DictionaryWord

@Dao
interface WordsDao {

    @Query("SELECT * FROM detected_objects_table WHERE target_lang = :targetLang")
    fun getObjectsByTargetLang(targetLang: String): LiveData<List<DetectedObject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectedObject(detectedObject: DetectedObject): Long

    @Delete
    suspend fun deleteDetectedObject(detectedObject: DetectedObject): Int

    @Query("SELECT * FROM dictionary_words_table WHERE language = :language")
    fun getWordsByLanguage(language: String): LiveData<List<DictionaryWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: DictionaryWord): Long

    @Update
    suspend fun updateWord(word: DictionaryWord): Int

    @Delete
    suspend fun deleteWord(word: DictionaryWord): Int
}