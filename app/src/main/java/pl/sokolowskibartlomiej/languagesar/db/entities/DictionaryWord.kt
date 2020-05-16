package pl.sokolowskibartlomiej.languagesar.db.entities

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_words_table")
data class DictionaryWord(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "language") @NonNull var language: String,
    @ColumnInfo(name = "status") @NonNull var status: Int = WORD_STATUS_NONE
) {
    companion object {
        const val WORD_STATUS_NONE = 0
        const val WORD_STATUS_SAVED = 1
        const val WORD_STATUS_KNOWN = 2
    }
}