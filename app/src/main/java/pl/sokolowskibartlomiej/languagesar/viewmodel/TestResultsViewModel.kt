package pl.sokolowskibartlomiej.languagesar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import pl.sokolowskibartlomiej.languagesar.model.TestQuestion
import pl.sokolowskibartlomiej.languagesar.model.repositories.WordsRepository

class TestResultsViewModel(val app: Application) : AndroidViewModel(app) {

    private val mWordsRepository = WordsRepository(app)

    var answeredQuestions = arrayOf<TestQuestion>()

    fun getResult(): String = "${answeredQuestions.count {
        it.selectedAnswer > 0 && it.answers[it.selectedAnswer - 1] == it.correctAnswer
    }}/${answeredQuestions.size}"

    fun getStatusById(id: Int) = answeredQuestions.find { it.id == id }!!.wordStatus

    fun updateWordStatusAsync(id: Int, status: Int): Deferred<Unit> =
        viewModelScope.async(Dispatchers.IO) {
            mWordsRepository.updateWordById(id, status)
            answeredQuestions[answeredQuestions.indexOfFirst { it.id == id }].wordStatus = status
        }
}