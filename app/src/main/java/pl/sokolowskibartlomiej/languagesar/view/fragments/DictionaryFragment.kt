package pl.sokolowskibartlomiej.languagesar.view.fragments

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import kotlinx.android.synthetic.main.content_dictionary.view.*
import kotlinx.android.synthetic.main.dialog_list.view.*
import kotlinx.android.synthetic.main.fragment_dictionary.view.*
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.languagesar.BuildConfig
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.db.entities.Word
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_KNOWN
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_NONE
import pl.sokolowskibartlomiej.languagesar.db.entities.Word.Companion.WORD_STATUS_SAVED
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import pl.sokolowskibartlomiej.languagesar.utils.showShortToast
import pl.sokolowskibartlomiej.languagesar.view.adapters.DictionaryRecyclerAdapter
import pl.sokolowskibartlomiej.languagesar.view.adapters.LanguageRecyclerAdapter
import pl.sokolowskibartlomiej.languagesar.viewmodel.DictionaryViewModel
import java.util.*

class DictionaryFragment : Fragment() {

    private lateinit var mViewModel: DictionaryViewModel
    private lateinit var mAdapter: DictionaryRecyclerAdapter
    private lateinit var mSearchView: SearchView
    private lateinit var mFiltersBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var mAddWordBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var mTextToSpeech: TextToSpeech
    private lateinit var mLoadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dictionary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu(view.dictionaryToolbar)

        mAdapter =
            DictionaryRecyclerAdapter(view.emptyDictionaryView, ::showWordPopupMenu, ::speakWord)
        view.dictionaryRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy < 0 && !view.addWordBtn.isShown)
                        view.addWordBtn.show()
                    else if (dy > 0 && view.addWordBtn.isShown)
                        view.addWordBtn.hide()
                }
            })
        }
        mFiltersBottomSheetBehavior = BottomSheetBehavior.from(view.filtersBottomSheet)
        mFiltersBottomSheetBehavior.state = STATE_HIDDEN
        mAddWordBottomSheetBehavior = BottomSheetBehavior.from(view.addWordBottomSheet)
        mAddWordBottomSheetBehavior.state = STATE_HIDDEN

        mViewModel = ViewModelProvider(this@DictionaryFragment).get(DictionaryViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        PreferencesManager.filtersLiveData.observe(viewLifecycleOwner, Observer {
            mAdapter.setStatusFilters(it)
        })

        view.addWordBtn.setOnClickListener { mAddWordBottomSheetBehavior.state = STATE_EXPANDED }
    }

    override fun onResume() {
        super.onResume()
        mTextToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                mTextToSpeech.language = Locale(PreferencesManager.getSelectedLanguage())
                mTextToSpeech.setSpeechRate(0.9f)
            } else if (BuildConfig.DEBUG) Log.e("TextToSpeech", "Initialization failed!")
        })
        if (PreferencesManager.getSelectedLanguage() == "") showSelectLanguageDialog()
        else fetchDictionary()
    }

    override fun onStop() {
        if (mTextToSpeech.isSpeaking) {
            mTextToSpeech.stop()
            mTextToSpeech.shutdown()
        }
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
        super.onStop()
    }

    fun onBackPressed(): Boolean {
        return if (!mSearchView.isIconified) {
            mSearchView.onActionViewCollapsed()
            false
        } else if (::mFiltersBottomSheetBehavior.isInitialized && mFiltersBottomSheetBehavior.state != STATE_HIDDEN) {
            mFiltersBottomSheetBehavior.state = STATE_HIDDEN
            false
        } else if (::mAddWordBottomSheetBehavior.isInitialized && mAddWordBottomSheetBehavior.state != STATE_HIDDEN) {
            mAddWordBottomSheetBehavior.state = STATE_HIDDEN
            false
        } else true
    }

    private fun fetchDictionary() {
        if (!PreferencesManager.areWordsInDatabase()) mViewModel.insertWordsFromFileToDatabase()
        mViewModel.getWordsFromDatabase().observe(viewLifecycleOwner, Observer { words ->
            mAdapter.setWordsList(
                words.sortedWith(
                    compareBy({ -it.status }, { it.word.toLowerCase(Locale.getDefault()) })
                ),
                PreferencesManager.getFilters()
            )
            view?.dictionaryRecyclerView?.scheduleLayoutAnimation()
            view?.dictionaryLoadingIndicator?.hide()
        })
    }

    private fun speakWord(word: Word, button: View) {
        var drawable = requireContext().getDrawable(
            if (mTextToSpeech.isSpeaking) R.drawable.anim_pause_to_sound else R.drawable.anim_sound_to_pause
        )
        (button as ImageButton).setImageDrawable(drawable)
        (drawable as AnimatedVectorDrawable).start()

        mTextToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String?) {
                drawable = requireContext().getDrawable(R.drawable.anim_pause_to_sound)
                button.setImageDrawable(drawable)
                (drawable as AnimatedVectorDrawable).start()
            }

            override fun onError(utteranceId: String?) {}
            override fun onStart(utteranceId: String?) {}
        })

        if (mTextToSpeech.isSpeaking) mTextToSpeech.stop()
        else mTextToSpeech.speak(
            word.word.split(" - ")[0], TextToSpeech.QUEUE_FLUSH,
            null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
        )
    }

    private fun showWordPopupMenu(word: Word, view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.word_popup_menu, popupMenu.menu)
        popupMenu.menu.findItem(R.id.action_word_save).isVisible = word.status == WORD_STATUS_NONE
        popupMenu.menu.findItem(R.id.action_word_save_remove)
            .isVisible = word.status == WORD_STATUS_SAVED
        popupMenu.menu.findItem(R.id.action_word_known).isVisible = word.status == WORD_STATUS_NONE
        popupMenu.menu.findItem(R.id.action_word_known_remove)
            .isVisible = word.status == WORD_STATUS_KNOWN

        popupMenu.setOnMenuItemClickListener { menuItem ->
            lifecycleScope.launch {
                mLoadingDialog.show()
                when (menuItem.itemId) {
                    R.id.action_word_save ->
                        mViewModel.updateWordStatusAsync(word, WORD_STATUS_SAVED)
                    R.id.action_word_save_remove ->
                        mViewModel.updateWordStatusAsync(word, WORD_STATUS_NONE)
                    R.id.action_word_known ->
                        mViewModel.updateWordStatusAsync(word, WORD_STATUS_KNOWN)
                    R.id.action_word_known_remove ->
                        mViewModel.updateWordStatusAsync(word, WORD_STATUS_NONE)
                    R.id.action_word_delete -> mViewModel.deleteWordAsync(word)
                    else -> mViewModel.updateWordStatusAsync(word, WORD_STATUS_NONE)
                }.await()
                popupMenu.dismiss()
                mLoadingDialog.hide()
                requireContext().showShortToast(R.string.database_updated)
            }
            true
        }
        popupMenu.show()

    }

    private fun inflateToolbarMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.dictionary_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_filters -> {
                    mFiltersBottomSheetBehavior.state = STATE_EXPANDED
                    true
                }
                R.id.action_settings -> {
                    findNavController().navigate(DictionaryFragmentDirections.showSettingsFragment())
                    true
                }
                else -> true
            }
        }
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
