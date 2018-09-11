package me.mauricee.pontoon.preferences.primaryColor

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_color.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseAdapter
import me.mauricee.pontoon.common.theme.PrimaryColor
import me.mauricee.pontoon.common.theme.primaryColor
import javax.inject.Inject

class PrimaryColorAdapter @Inject constructor() : BaseAdapter<PrimaryColor, PrimaryColorAdapter.ViewHolder>() {

    private val colors = PrimaryColor.values()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
                    .let(this::ViewHolder)

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.color.imageTintList = ColorStateList.valueOf(colors[position].theme(holder.itemView.context).primaryColor)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            subscriptions += itemView.clicks().map { colors[layoutPosition] }.subscribe(relay::accept)
        }
    }
}