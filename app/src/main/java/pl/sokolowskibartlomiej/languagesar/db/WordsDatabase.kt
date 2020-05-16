package pl.sokolowskibartlomiej.languagesar.db

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.sokolowskibartlomiej.languagesar.db.daos.WordsDao
import pl.sokolowskibartlomiej.languagesar.db.entities.DetectedObject
import pl.sokolowskibartlomiej.languagesar.db.entities.DictionaryWord

@Database(
    entities = [DetectedObject::class, DictionaryWord::class],
    version = 1,
    exportSchema = false
)
abstract class WordsDatabase : RoomDatabase() {

    abstract fun wordsDao(): WordsDao

    companion object {
        private var INSTANCE: WordsDatabase? = null

        @WorkerThread
        fun getInstance(context: Context): WordsDatabase? {
            if (INSTANCE == null) {
                synchronized(WordsDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        WordsDatabase::class.java,
                        "words_database.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}