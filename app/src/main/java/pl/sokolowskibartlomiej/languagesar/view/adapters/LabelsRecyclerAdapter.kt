package pl.sokolowskibartlomiej.languagesar.view.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_object_detected.view.*
import pl.sokolowskibartlomiej.languagesar.R

class LabelsRecyclerAdapter : RecyclerView.Adapter<LabelsRecyclerAdapter.LabelViewHolder>() {

    private var mLabels = listOf<String>()
    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LabelViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_object_detected, parent, false)
    )

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) =
        holder.bindView(mLabels[position], position)

    override fun getItemCount(): Int = mLabels.size

    fun setLabelsList(list: List<String>) {
        mLabels = list
        notifyDataSetChanged()
    }

    fun setSelectedLabel(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    inner class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(label: String, position: Int) {
            itemView.objectLabel.text = label
            itemView.labelsLayout.setBackgroundColor(
                Color.parseColor(if (position == selectedPosition) "#1976d2" else "#FFFFFF")
            )
            itemView.objectLabel.setTextColor(
                Color.parseColor(if (position == selectedPosition) "#FFFFFF" else "#191919")
            )

            itemView.labelsLayout.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
            }
        }
    }
}