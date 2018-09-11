package me.mauricee.pontoon.preferences.baseTheme

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_color.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.BaseTheme
import javax.inject.Inject

class BaseThemeAdapter @Inject constructor() : RecyclerView.Adapter<BaseThemeAdapter.ViewHolder>() {
    private val colors = BaseTheme.values()
    private val selectedColorSubject = PublishRelay.create<BaseTheme>()
    val selectedColor: Observable<BaseTheme> = selectedColorSubject

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
                    .let(this::ViewHolder)

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val theme = colors[position]
        val color = when (theme) {
            BaseTheme.Light -> R.color.white
            BaseTheme.Dark -> R.color.md_grey_850
            BaseTheme.Black -> R.color.md_black_1000
        }
        holder.itemView.color.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context, color))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.clicks().map { colors[layoutPosition] }.subscribe(selectedColorSubject::accept)
        }
    }
}