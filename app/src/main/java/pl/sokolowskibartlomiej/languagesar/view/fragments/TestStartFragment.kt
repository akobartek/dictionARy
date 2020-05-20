package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_test_start.view.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.showBasicAlertDialog
import pl.sokolowskibartlomiej.languagesar.viewmodel.TestStartViewModel

class TestStartFragment : Fragment() {

    private lateinit var mViewModel: TestStartViewModel
    private lateinit var mLoadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_test_start, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
        mLoadingDialog.show()
        mViewModel = ViewModelProvider(this@TestStartFragment).get(TestStartViewModel::class.java)
        mViewModel.fetchNumberOfResults()
        mViewModel.numberOfResults.observe(viewLifecycleOwner, Observer { numberOfResults ->
            if (numberOfResults == null) return@Observer
            else if (numberOfResults[0] == 0) {
                showDatabaseEmptyDialog()
                return@Observer
            }
            view.numberOfWordsSlider.valueTo =
                if (numberOfResults[0] <= 64) numberOfResults[0].toFloat()
                else 64f
            view.statusRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.statusAllBtn ->
                        view.numberOfWordsSlider.valueTo =
                            if (numberOfResults[0] <= 50) numberOfResults[0].toFloat()
                            else 50f
                    R.id.statusSavedBtn ->
                        if (numberOfResults[1] == 0) {
                            requireContext().showBasicAlertDialog(null, R.string.no_saved_words)
                            view.statusRadioGroup.check(R.id.statusAllBtn)
                        } else view.numberOfWordsSlider.valueTo =
                            if (numberOfResults[1] <= 50) numberOfResults[1].toFloat()
                            else 50f
                    R.id.statusKnownBtn ->
                        if (numberOfResults[2] == 0) {
                            requireContext().showBasicAlertDialog(null, R.string.no_known_words)
                            view.statusRadioGroup.check(R.id.statusAllBtn)
                        } else view.numberOfWordsSlider.valueTo =
                            if (numberOfResults[2] <= 50) numberOfResults[2].toFloat()
                            else 50f
                }
            }
            mLoadingDialog.hide()
        })

        view.startTestBtn.setOnClickListener {
            view.startTestBtn.isEnabled = false
            val numberOfWords = view.numberOfWordsSlider.value.toInt()
            if (numberOfWords == 0) {
                requireContext().showBasicAlertDialog(null, R.string.number_of_words_error)
                view.testNumberOfWordsTitle.error = ""
                view.startTestBtn.isEnabled = true
                return@setOnClickListener
            }
            val status = when (view.statusRadioGroup.checkedRadioButtonId) {
                R.id.statusAllBtn -> 0
                R.id.statusSavedBtn -> 1
                R.id.statusKnownBtn -> 2
                else -> 0
            }
            findNavController().navigate(
                TestStartFragmentDirections.showQuestionsFragment(status, numberOfWords)
            )
        }
    }

    override fun onStop() {
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
        super.onStop()
    }

    private fun showDatabaseEmptyDialog() =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.no_words_title)
            .setMessage(R.string.no_words_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(TestStartFragmentDirections.showDictionaryFragment())
            }
            .create()
            .show()
}
