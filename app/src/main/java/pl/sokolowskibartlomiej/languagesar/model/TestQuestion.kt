package pl.sokolowskibartlomiej.languagesar.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TestQuestion(
    val id: Int,
    val correctAnswer: String,
    val translation: String,
    val answers: Array<String>,
    var wordStatus: Int,
    var selectedAnswer: Int = -1
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestQuestion

        if (id != other.id) return false
        if (correctAnswer != other.correctAnswer) return false
        if (translation != other.translation) return false
        if (!answers.contentEquals(other.answers)) return false
        if (wordStatus != other.wordStatus) return false
        if (selectedAnswer != other.selectedAnswer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + correctAnswer.hashCode()
        result = 31 * result + translation.hashCode()
        result = 31 * result + answers.contentHashCode()
        result = 31 * result + wordStatus
        result = 31 * result + selectedAnswer
        return result
    }
}