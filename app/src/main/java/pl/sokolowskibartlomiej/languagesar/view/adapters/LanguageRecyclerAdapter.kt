package pl.sokolowskibartlomiej.languagesar.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_language_select.view.*
import pl.sokolowskibartlomiej.languagesar.R
import pl.sokolowskibartlomiej.languagesar.utils.PreferencesManager

class LanguageRecyclerAdapter : RecyclerView.Adapter<LanguageRecyclerAdapter.LanguageViewHolder>() {

    private val languages =
        if (PreferencesManager.getUserLanguage().contains("en")) arrayOf("es", "fr", "it")
        else arrayOf("en", "es", "fr", "it")
    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LanguageViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_language_select, parent, false)
    )

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) =
        holder.bindView(languages[position], position)

    override fun getItemCount(): Int = languages.size

    fun getSelectedLanguage() = languages[selectedPosition]


    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(lang: String, position: Int) {
            val res = itemView.context.resources
            itemView.languageName.text = res.getString(
                res.getIdentifier("language_$lang", "string", itemView.context.packageName)
            )
            itemView.languageSelectLayout.setBackgroundColor(
                Color.parseColor(if (position == selectedPosition) "#999999" else "#FFFFFF")
            )

            itemView.languageSelectLayout.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
            }
        }
    }
}