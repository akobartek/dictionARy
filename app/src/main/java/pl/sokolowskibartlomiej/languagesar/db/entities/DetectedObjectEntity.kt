package pl.sokolowskibartlomiej.languagesar.db.entities

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words_table")
data class DetectedObjectEntity(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "label") @NonNull var label: String,
    @ColumnInfo(name = "source_lang") @NonNull var sourceLang: String,
    @ColumnInfo(name = "source_translation") @NonNull var sourceTranslation: String,
    @ColumnInfo(name = "target_lang") @NonNull var targetLang: String,
    @ColumnInfo(name = "target_translation") @NonNull var targetTranslation: String
)