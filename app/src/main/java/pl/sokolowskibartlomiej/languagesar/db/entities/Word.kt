package pl.sokolowskibartlomiej.languagesar.db.entities

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words_table")
data class Word(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @ColumnInfo(name = "word") @NonNull var word: String,
    @ColumnInfo(name = "language") @NonNull var language: String,
    @ColumnInfo(name = "status") @NonNull var status: Int = 0
) {
    companion object {
        const val WORD_STATUS_NONE = 0
        const val WORD_STATUS_SAVED = 1
        const val WORD_STATUS_KNOWN = 2
    }
}