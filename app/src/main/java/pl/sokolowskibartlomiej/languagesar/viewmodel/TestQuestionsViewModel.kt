package pl.sokolowskibartlomiej.languagesar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.model.TestQuestion
import pl.sokolowskibartlomiej.languagesar.model.repositories.WordsRepository
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import pl.sokolowskibartlomiej.languagesar.utils.similarity

class TestQuestionsViewModel(val app: Application) : AndroidViewModel(app) {

    private val mWordsRepository = WordsRepository(app)

    val areQuestionsFetched = MutableLiveData(false)
    var testQuestions = arrayListOf<TestQuestion>()
    var currentQuestion = 0

    fun fetchQuestions(status: Int, numberOfQuestions: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val allWords =
                mWordsRepository.getWordsListByLanguage(PreferencesManager.getSelectedLanguage())
            val filteredWords =
                if (status == 0) allWords else allWords.filter { it.status == status }
            var finalWords = filteredWords.shuffled()
            repeat(3) { finalWords = finalWords.shuffled() }
            finalWords = finalWords.take(numberOfQuestions)

            val questions = arrayListOf<TestQuestion>()
            val answerWords = allWords.map { it.word.split(" - ")[0] }
            finalWords.forEach { questionWord ->
                val word = questionWord.word.split(" - ")
                val answers = answerWords.sortedByDescending { it.similarity(word[0]) }
                    .take(4).shuffled().toTypedArray()
                questions.add(
                    TestQuestion(
                        questionWord.id!!,
                        word[0],
                        word[if (PreferencesManager.getUserLanguage() == "pl") 1 else 2],
                        answers,
                        questionWord.status
                    )
                )
            }
            testQuestions = questions
            areQuestionsFetched.postValue(true)
        }
    }
}