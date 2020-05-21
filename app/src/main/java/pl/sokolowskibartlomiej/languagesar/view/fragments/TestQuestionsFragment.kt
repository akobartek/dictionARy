package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.content_test_questions.view.*
import kotlinx.android.synthetic.main.fragment_test_questions.view.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.animateBackgroundTintChange
import pl.sokolowskibartlomiej.languagesar.utils.getChildViewByName
import pl.sokolowskibartlomiej.languagesar.utils.getColorResource
import pl.sokolowskibartlomiej.languagesar.viewmodel.TestQuestionsViewModel

class TestQuestionsFragment : Fragment() {

    private lateinit var mViewModel: TestQuestionsViewModel
    private lateinit var mLoadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_test_questions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.questionToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        view.questionToolbar.setNavigationOnClickListener { onBackPressed() }

        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
        mLoadingDialog.show()
        mViewModel =
            ViewModelProvider(this@TestQuestionsFragment).get(TestQuestionsViewModel::class.java)
        arguments?.let { bundle ->
            val args = TestQuestionsFragmentArgs.fromBundle(bundle)
            mViewModel.fetchQuestions(args.wordsStatus, args.numberOfWords)
        }
        mViewModel.areQuestionsFetched.observe(viewLifecycleOwner, Observer { areFetched ->
            if (areFetched) {
                setQuestionValues()
                if (mLoadingDialog.isShowing) mLoadingDialog.hide()
            }
        })

        view.answer1.setOnClickListener(onAnswerClickListener)
        view.answer2.setOnClickListener(onAnswerClickListener)
        view.answer3.setOnClickListener(onAnswerClickListener)
        view.answer4.setOnClickListener(onAnswerClickListener)
        view.previousQuestionBtn.setOnClickListener {
            --mViewModel.currentQuestion
            swapQuestionLayout(view.questionLayout.width.toFloat())
        }
        view.nextQuestionBtn.setOnClickListener {
            if (mViewModel.currentQuestion < mViewModel.testQuestions.size - 1) {
                ++mViewModel.currentQuestion
                swapQuestionLayout(-view.questionLayout.width.toFloat())
            } else showFinishTestDialog()
        }
    }

    fun onBackPressed() =
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.interrupt_test_dialog_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.interrupt) { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    override fun onStop() {
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
        super.onStop()
    }

    private fun setQuestionValues() {
        view?.questionToolbar?.title =
            getString(R.string.question_x, mViewModel.currentQuestion + 1)
        view?.previousQuestionBtn?.isEnabled = mViewModel.currentQuestion != 0
        view?.nextQuestionBtn?.text = getString(
            if (mViewModel.currentQuestion < mViewModel.testQuestions.size - 1) R.string.next
            else R.string.finish
        )
        val question = mViewModel.testQuestions[mViewModel.currentQuestion]
        view?.translationTV?.text = question.translation
        for (i in question.answers.indices) {
            val answerBtn = view?.getChildViewByName("answer${i + 1}") as MaterialButton
            answerBtn.text = question.answers[i]
            answerBtn.backgroundTintList =
                if (question.selectedAnswer == i + 1)
                    ContextCompat.getColorStateList(requireContext(), R.color.colorAccent)
                else ContextCompat.getColorStateList(requireContext(), R.color.colorAnswer)
        }
    }

    private fun swapQuestionLayout(width: Float) {
        val anim = ObjectAnimator.ofFloat(view?.questionLayout, "x", 0f, width)
            .setDuration(150)
        anim.doOnEnd {
            setQuestionValues()
            ObjectAnimator.ofFloat(view?.questionLayout, "x", -width, 0f)
                .setDuration(150)
                .start()
        }
        anim.start()
    }

    private fun showFinishTestDialog() =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.finish_test_dialog_title)
            .setMessage(
                if (mViewModel.testQuestions.any { it.selectedAnswer == -1 }) R.string.finish_test_dialog_msg2
                else R.string.finish_test_dialog_msg1
            )
            .setCancelable(false)
            .setPositiveButton(R.string.finish) { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(
                    TestQuestionsFragmentDirections.showTestResultsFragment(mViewModel.testQuestions.toTypedArray())
                )
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    private val onAnswerClickListener = View.OnClickListener {
        val colorNormal = requireContext().getColorResource(R.color.colorAnswer)
        val colorSelected = requireContext().getColorResource(R.color.colorAccent)
        val tag = it.tag.toString().toInt()
        val currentQuestion = mViewModel.testQuestions[mViewModel.currentQuestion]
        when (currentQuestion.selectedAnswer) {
            -1 -> {
                mViewModel.testQuestions[mViewModel.currentQuestion].selectedAnswer = tag
                it.animateBackgroundTintChange(colorNormal, colorSelected)
            }
            tag -> {
                mViewModel.testQuestions[mViewModel.currentQuestion].selectedAnswer = -1
                it.animateBackgroundTintChange(colorSelected, colorNormal)
            }
            else -> {
                it.animateBackgroundTintChange(colorNormal, colorSelected)
                view?.getChildViewByName("answer${currentQuestion.selectedAnswer}")
                    ?.animateBackgroundTintChange(colorSelected, colorNormal)
                mViewModel.testQuestions[mViewModel.currentQuestion].selectedAnswer = tag
            }
        }
    }
}
