package pl.sokolowskibartlomiej.languagesar.db.entities

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detected_objects_table")
data class DetectedObject(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "label") @NonNull var label: String,
    @ColumnInfo(name = "source_lang") @NonNull var sourceLang: String,
    @ColumnInfo(name = "source_translation") @NonNull var sourceTranslation: String,
    @ColumnInfo(name = "target_lang") @NonNull var targetLang: String,
    @ColumnInfo(name = "target_translation") @NonNull var targetTranslation: String,
    @ColumnInfo(name = "status") @NonNull var status: Int = OBJECT_STATUS_NEW
) {
    companion object {
        const val OBJECT_STATUS_NEW = 0
        const val OBJECT_STATUS_KNOWN = 1
    }
}