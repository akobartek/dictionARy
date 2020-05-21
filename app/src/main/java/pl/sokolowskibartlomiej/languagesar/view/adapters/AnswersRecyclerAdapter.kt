package pl.sokolowskibartlomiej.languagesar.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_question_result.view.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.model.TestQuestion
import pl.sokolowskibartlomiej.languagesar.utils.getColorResource

class AnswersRecyclerAdapter(
    private val answeredQuestions: Array<TestQuestion>, val showPopup: (Int, View) -> Unit
) : RecyclerView.Adapter<AnswersRecyclerAdapter.AnswerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AnswerViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_question_result, parent, false)
    )

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) =
        holder.bindView(answeredQuestions[position])

    override fun getItemCount(): Int = answeredQuestions.size


    inner class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindView(answer: TestQuestion) {
            val selectedAnswer =
                if (answer.selectedAnswer > 0) answer.answers[answer.selectedAnswer - 1] else ""
            val isCorrect = answer.selectedAnswer > 0 && answer.correctAnswer == selectedAnswer
            itemView.answerLayout.setCardBackgroundColor(
                itemView.context.getColorResource(
                    if (isCorrect) R.color.colorCorrectAnswer
                    else R.color.colorWrongAnswer
                )
            )
            itemView.correctAnswerTV.text = "${answer.translation} - ${answer.correctAnswer}"
            itemView.incorrectAnswerTV.text =
                if (answer.selectedAnswer < 0) itemView.context.getString(R.string.no_answer_given)
                else itemView.context.getString(R.string.your_answer, selectedAnswer)
            itemView.incorrectAnswerTV.visibility = if (isCorrect) View.GONE else View.VISIBLE

            itemView.answerOptionsBtn.setOnClickListener { showPopup(answer.id, it) }
        }
    }
}