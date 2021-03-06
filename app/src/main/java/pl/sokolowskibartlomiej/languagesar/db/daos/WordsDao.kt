package pl.sokolowskibartlomiej.languagesar.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pl.sokolowskibartlomiej.languagesar.db.entities.Word

@Dao
interface WordsDao {

    @Query("SELECT * FROM words_table WHERE language = :language")
    fun getWordsLiveDataByLanguage(language: String): LiveData<List<Word>>

    @Query("SELECT * FROM words_table WHERE language = :language")
    suspend fun getWordsListByLanguage(language: String): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleWords(vararg words: Word)

    @Update
    suspend fun updateWord(word: Word)

    @Query("UPDATE words_table SET status = :status WHERE id = :id")
    suspend fun updateWordById(id: Int, status: Int)

    @Delete
    suspend fun deleteWord(word: Word)

    @Query("SELECT COUNT(*) FROM words_table WHERE language = :language")
    suspend fun getCountOfWords(language: String): Int

    @Query("SELECT COUNT(*) FROM words_table WHERE language = :language AND status = :status")
    suspend fun getCountByStatus(language: String, status: Int): Int
}