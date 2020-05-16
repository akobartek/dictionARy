package pl.sokolowskibartlomiej.languagesar.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pl.sokolowskibartlomiej.languagesar.db.entities.Word

@Dao
interface WordsDao {

    @Query("SELECT * FROM words_table WHERE language = :language")
    fun getWordsByLanguage(language: String): LiveData<List<Word>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleWords(vararg words: Word)

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)
}