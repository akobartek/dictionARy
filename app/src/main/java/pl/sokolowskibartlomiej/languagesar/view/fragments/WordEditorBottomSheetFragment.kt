package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_word_editor_bottom_sheet.view.*
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.showShortToast
import pl.sokolowskibartlomiej.languagesar.viewmodel.DictionaryViewModel

class WordEditorBottomSheetFragment : Fragment() {

    private lateinit var mViewModel: DictionaryViewModel
    private lateinit var mLoadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_word_editor_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel =
            ViewModelProvider(this@WordEditorBottomSheetFragment).get(DictionaryViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        view.collapseSheetBtn.setOnClickListener { requireActivity().onBackPressed() }
        view.saveWordBtn.setOnClickListener {
            view.saveWordBtn.isEnabled = false
            val word = view.wordET.text.toString().trim()
            val translation = view.translationET.text.toString().trim()
            val isSaved = view.saveWordCheckBox.isChecked

            if (!areValuesValid(word, translation)) {
                view.saveWordBtn.isEnabled = true
                return@setOnClickListener
            }
            lifecycleScope.launch {
                mLoadingDialog.show()
                mViewModel.insertWordAsync(word, translation, isSaved).await()
                view.saveWordBtn.isEnabled = true
                requireActivity().onBackPressed()
                view.wordET.setText("")
                view.translationET.setText("")
                view.saveWordCheckBox.isChecked = false
                mLoadingDialog.hide()
                requireContext().showShortToast(R.string.word_saved)
            }
        }
        view.setOnClickListener {
            (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    private fun areValuesValid(word: String, translation: String): Boolean {
        var isValid = true
        if (word.isEmpty()) {
            view?.wordET?.error = getString(R.string.word_empty_error)
            isValid = false
        }
        if (translation.isEmpty()) {
            view?.translationET?.error = getString(R.string.translation_empty_error)
            isValid = false
        }
        return isValid
    }

}
