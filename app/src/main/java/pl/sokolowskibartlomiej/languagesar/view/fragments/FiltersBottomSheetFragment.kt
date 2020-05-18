package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_filters_bottom_sheet.view.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import pl.sokolowskibartlomiej.languagesar.utils.showShortToast

class FiltersBottomSheetFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_filters_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchSubtitle()

        val filters = ArrayList(PreferencesManager.getFilters().split(", "))

        view.statusSavedCheckBox.isChecked = filters.contains("1")
        view.statusSavedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) filters.add("1") else filters.remove("1")
        }
        view.statusKnownCheckBox.isChecked = filters.contains("2")
        view.statusKnownCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) filters.add("2") else filters.remove("2")
        }
        view.statusOtherCheckBox.isChecked = filters.contains("0")
        view.statusOtherCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) filters.add("0") else filters.remove("0")
        }

        view.collapseSheetBtn.setOnClickListener { requireActivity().onBackPressed() }
        view.saveFiltersBtn.setOnClickListener {
            PreferencesManager.setFilters(filters.joinToString(", "))
            fetchSubtitle()
            requireContext().showShortToast(R.string.filters_saved)
            requireActivity().onBackPressed()
        }
    }

    private fun fetchSubtitle() {
        val filters = PreferencesManager.getFilters()
        var subtitle =
            if (filters != "")
                (if (filters.contains("1")) "${getString(R.string.saved)}, " else "") +
                        (if (filters.contains("2")) "${getString(R.string.known)}, " else "") +
                        (if (filters.contains("0")) "${getString(R.string.other)}, " else "")
            else getString(R.string.none)
        if (filters != "") subtitle = subtitle.substring(0, subtitle.length - 2)
        view?.filterStatusSubTitle?.text = getString(R.string.current_filters, subtitle)
    }
}
