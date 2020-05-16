package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_dictionary.view.*
import kotlinx.android.synthetic.main.dialog_list.view.*
import kotlinx.android.synthetic.main.fragment_dictionary.view.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.db.entities.Word
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import pl.sokolowskibartlomiej.languagesar.view.adapters.DictionaryRecyclerAdapter
import pl.sokolowskibartlomiej.languagesar.view.adapters.LanguageRecyclerAdapter
import pl.sokolowskibartlomiej.languagesar.viewmodel.DictionaryViewModel

class DictionaryFragment : Fragment() {

    private lateinit var mViewModel: DictionaryViewModel
    private lateinit var mAdapter: DictionaryRecyclerAdapter
    private lateinit var mSearchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dictionary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu(view.dictionaryToolbar)

        mAdapter = DictionaryRecyclerAdapter(view.emptyDictionaryView, ::showWordPopupMenu)
        view.dictionaryRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }

        mViewModel = ViewModelProvider(this@DictionaryFragment).get(DictionaryViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        if (PreferencesManager.getSelectedLanguage() == "") showSelectLanguageDialog()
        else fetchDictionary()
    }

    fun onBackPressed(): Boolean {
        return if (!mSearchView.isIconified) {
            mSearchView.onActionViewCollapsed()
            false
        } else true
    }

    private fun fetchDictionary() {
        mViewModel.getWordsFromDatabase().observe(viewLifecycleOwner, Observer { words ->
            mAdapter.setWordsList(words)
            view?.dictionaryRecyclerView?.scheduleLayoutAnimation()
            view?.dictionaryLoadingIndicator?.hide()
            view?.emptyDictionaryView?.visibility =
                if (words.isEmpty()) View.VISIBLE else View.INVISIBLE
        })
    }

    private fun showWordPopupMenu(word: Word, view: View) {
        // TODO() -> Options
    }

    private fun inflateToolbarMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.dictionary_menu)
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView = toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        mSearchView.maxWidth = Integer.MAX_VALUE

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mAdapter.filter.filter(newText)
                return false
            }
        })
    }

    @SuppressLint("InflateParams")
    private fun showSelectLanguageDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_list, null)
        val languageAdapter = LanguageRecyclerAdapter()
        dialogView.dialogRecyclerView.apply {
            layoutManager = LinearLayoutManager(dialogView.context)
            itemAnimator = DefaultItemAnimator()
            adapter = languageAdapter
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_language_dialog_title)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                PreferencesManager.setSelectedLanguage(languageAdapter.getSelectedLanguage())
                if (!PreferencesManager.areWordsInDatabase()) mViewModel.insertWordsFromFileToDatabase()
                fetchDictionary()
                dialog?.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog?.dismiss()
                requireActivity().finish()
            }
            .create()
            .show()
    }
}
