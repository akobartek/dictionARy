package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_test_results.view.*
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_KNOWN
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_NONE
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_SAVED
import pl.sokolowskibartlomiej.languagesar.utils.showShortToast
import pl.sokolowskibartlomiej.languagesar.view.adapters.AnswersRecyclerAdapter
import pl.sokolowskibartlomiej.languagesar.viewmodel.TestResultsViewModel

class TestResultsFragment : Fragment() {

    private lateinit var mViewModel: TestResultsViewModel
    private lateinit var mAdapter: AnswersRecyclerAdapter
    private lateinit var mLoadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_test_results, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.resultsToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        view.resultsToolbar.setNavigationOnClickListener { onBackPressed() }

        mViewModel =
            ViewModelProvider(this@TestResultsFragment).get(TestResultsViewModel::class.java)
        arguments?.let { bundle ->
            mViewModel.answeredQuestions = TestResultsFragmentArgs.fromBundle(bundle).questions
        }
        view.results.text = mViewModel.getResult()
        mAdapter = AnswersRecyclerAdapter(mViewModel.answeredQuestions, ::showAnswerPopupMenu)
        view.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
            scheduleLayoutAnimation()
        }
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
    }

    override fun onStop() {
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
        super.onStop()
    }

    fun onBackPressed() =
        findNavController().navigate(TestResultsFragmentDirections.showTestStartFragment())

    private fun showAnswerPopupMenu(id: Int, view: View) {
        val status = mViewModel.getStatusById(id)
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.answer_popup_menu, popupMenu.menu)
        popupMenu.menu.findItem(R.id.action_word_save).isVisible = status == WORD_STATUS_NONE
        popupMenu.menu.findItem(R.id.action_word_save_remove).isVisible =
            status == WORD_STATUS_SAVED
        popupMenu.menu.findItem(R.id.action_word_known).isVisible = status == WORD_STATUS_NONE
        popupMenu.menu.findItem(R.id.action_word_known_remove).isVisible =
            status == WORD_STATUS_KNOWN

        popupMenu.setOnMenuItemClickListener { menuItem ->
            lifecycleScope.launch {
                mLoadingDialog.show()
                when (menuItem.itemId) {
                    R.id.action_word_save ->
                        mViewModel.updateWordStatusAsync(id, WORD_STATUS_SAVED)
                    R.id.action_word_save_remove ->
                        mViewModel.updateWordStatusAsync(id, WORD_STATUS_NONE)
                    R.id.action_word_known ->
                        mViewModel.updateWordStatusAsync(id, WORD_STATUS_KNOWN)
                    R.id.action_word_known_remove ->
                        mViewModel.updateWordStatusAsync(id, WORD_STATUS_NONE)
                    else -> mViewModel.updateWordStatusAsync(id, WORD_STATUS_NONE)
                }.await()
                popupMenu.dismiss()
                mLoadingDialog.hide()
                requireContext().showShortToast(R.string.database_updated)
            }
            true
        }
        popupMenu.show()
    }
}
