package me.mauricee.pontoon.preferences.primaryColor

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_color.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.PrimaryColor
import me.mauricee.pontoon.common.theme.primaryColor
import javax.inject.Inject

class PrimaryColorAdapter @Inject constructor() : RecyclerView.Adapter<PrimaryColorAdapter.ViewHolder>() {
    private val colors = PrimaryColor.values()
    private val selectedColorSubject = PublishRelay.create<PrimaryColor>()
    val selectedColor: Observable<PrimaryColor> = selectedColorSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
                    .let(this::ViewHolder)

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.color.imageTintList = ColorStateList.valueOf(colors[position].theme(holder.itemView.context).primaryColor)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.clicks().map { colors[layoutPosition] }.subscribe(selectedColorSubject::accept)
        }
    }
}