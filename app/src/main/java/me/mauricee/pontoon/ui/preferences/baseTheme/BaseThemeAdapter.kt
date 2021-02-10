package me.mauricee.pontoon.ui.preferences.baseTheme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_color_list.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseAdapter
import me.mauricee.pontoon.common.theme.BaseTheme
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.ui.preferences.CircleHelper
import javax.inject.Inject

class BaseThemeAdapter @Inject constructor(private val circleHelper: CircleHelper, private val themeManager: ThemeManager) : BaseAdapter<BaseTheme, BaseThemeAdapter.ViewHolder>() {
    private val themes = BaseTheme.values()
    private var newSelectedTheme: BaseTheme? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_color_grid, parent, false)
                    .let(this::ViewHolder)

    override fun getItemId(position: Int): Long = themes[position].ordinal.toLong()

    override fun getItemCount(): Int = themes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isOriginal = themeManager.baseTheme == themes[position]
        val isChecked = newSelectedTheme == themes[position]
        val context = holder.itemView.context
        val color = when (themes[position]) {
            BaseTheme.Light -> R.color.white
            BaseTheme.Black -> R.color.md_black_1000
        }.let { ContextCompat.getColor(context, it) }

        val drawable = when {
            isOriginal -> circleHelper.buildSelectedCircle(color, ContextCompat.getColor(context, R.color.colorThemeSelection))
            isChecked -> circleHelper.buildSelectedCircle(color, ContextCompat.getColor(context, R.color.md_green_A400))
            else -> circleHelper.buildCircle(color)
        }
        holder.itemView.color.setImageDrawable(drawable)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            subscriptions += itemView.clicks().map { themes[adapterPosition] }
                    .subscribe {
                        newSelectedTheme?.let {
                            notifyItemChanged(themes.indexOf(it))
                        }
                        newSelectedTheme = it
                        relay.accept(it)
                        notifyItemChanged(adapterPosition)
                    }
        }
    }

}