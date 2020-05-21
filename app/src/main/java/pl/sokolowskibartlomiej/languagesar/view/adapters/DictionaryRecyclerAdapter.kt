package pl.sokolowskibartlomiej.languagesar.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_dictionary.view.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.db.entities.Word
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager
import java.util.*
import kotlin.collections.ArrayList

class DictionaryRecyclerAdapter(
    val emptyView: View, val showPopup: (Word, View) -> Unit, val speakWord: (Word, View) -> Unit
) : RecyclerView.Adapter<DictionaryRecyclerAdapter.WordViewHolder>(), Filterable {

    private var mWords = listOf<Word>()
    private var mWordsFiltered = listOf<Word>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WordViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_dictionary, parent, false)
    )

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) =
        holder.bindView(mWordsFiltered[position])

    override fun getItemCount(): Int = mWordsFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val query = charSequence.toString()
                mWordsFiltered =
                    if (query.isEmpty()) mWords
                    else {
                        val filteredList = ArrayList<Word>()
                        for (index in mWords.indices)
                            if (mWords[index].word.toLowerCase(Locale.ROOT)
                                    .contains(query.toLowerCase(Locale.ROOT))
                            ) filteredList.add(mWords[index])
                        filteredList
                    }
                val filterResults = FilterResults()
                filterResults.values = mWordsFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                mWordsFiltered = results?.values as List<Word>
                emptyView.visibility =
                    if (mWordsFiltered.isEmpty()) View.VISIBLE else View.INVISIBLE
                notifyDataSetChanged()
            }
        }
    }

    fun setWordsList(list: List<Word>, filters: String) {
        mWords = list
        mWordsFiltered = list.filter { filters.contains(it.status.toString()) }
        notifyDataSetChanged()
        emptyView.visibility = if (mWordsFiltered.isEmpty()) View.VISIBLE else View.INVISIBLE
    }

    fun setStatusFilters(filters: String) {
        mWordsFiltered = mWords.filter { filters.contains(it.status.toString()) }
        notifyDataSetChanged()
        emptyView.visibility = if (mWordsFiltered.isEmpty()) View.VISIBLE else View.INVISIBLE
    }


    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(word: Word) {
            itemView.wordTV.text = word.word.split(" - ")[0]
            itemView.translationTV.text =
                word.word.split(" - ")[if (PreferencesManager.getUserLanguage() == "pl") 1 else 2]

            itemView.wordOptionsBtn.setOnClickListener { showPopup(word, it) }

            itemView.wordSoundBtn.setOnClickListener { speakWord(word, it) }
        }
    }
}