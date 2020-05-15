package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_detected_objects.view.*
import kotlinx.android.synthetic.main.fragment_translation_bottom_sheet.view.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.GlideApp
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import pl.sokolowskibartlomiej.languagesar.utils.showShortToast
import pl.sokolowskibartlomiej.languagesar.view.adapters.LabelsRecyclerAdapter
import pl.sokolowskibartlomiej.languagesar.viewmodel.PhotoViewModel

class TranslationBottomSheetFragment : Fragment() {

    private lateinit var mViewModel: PhotoViewModel
    private lateinit var mLabelsRecyclerAdapter: LabelsRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_translation_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlideApp.with(this@TranslationBottomSheetFragment)
            .load("https://www.countryflags.io/${ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].country}/flat/24.png")
            .into(view.sourceFlag)
        GlideApp.with(this@TranslationBottomSheetFragment)
            .load("https://www.countryflags.io/${PreferencesManager.getSelectedLanguage()}/flat/24.png")
            .into(view.targetFlag)

        mLabelsRecyclerAdapter = LabelsRecyclerAdapter()
        mViewModel = ViewModelProvider(requireActivity()).get(PhotoViewModel::class.java)
        mViewModel.translation.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                fetchTexts()
                mLabelsRecyclerAdapter.setLabelsList(it.first.split(", "))
            }
        })

        view.collapseSheetBtn.setOnClickListener { requireActivity().onBackPressed() }
        view.saveTranslationBtn.setOnClickListener {
            mViewModel.insertWord()
            requireContext().showShortToast(R.string.word_saved)
            requireActivity().onBackPressed()
        }
        view.otherObjectsBtn.setOnClickListener { showDetectedObjectsDialog() }
    }

    private fun fetchTexts() {
        view?.sourceText?.text =
            mViewModel.translation.value!!.first.split(", ")[mViewModel.selectedLabel]
        view?.targetText?.text =
            mViewModel.translation.value!!.second.split(", ")[mViewModel.selectedLabel]
    }

    @SuppressLint("InflateParams")
    private fun showDetectedObjectsDialog() {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_detected_objects, null)
        mLabelsRecyclerAdapter.setSelectedLabel(mViewModel.selectedLabel)
        dialogView.labelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(dialogView.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mLabelsRecyclerAdapter
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.other_detected_objects)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.apply)) { dialog, _ ->
                dialog?.dismiss()
                mViewModel.selectedLabel = mLabelsRecyclerAdapter.selectedPosition
                fetchTexts()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()
            .show()
    }
}
