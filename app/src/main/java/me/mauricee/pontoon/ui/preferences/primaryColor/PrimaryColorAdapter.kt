package me.mauricee.pontoon.ui.preferences.primaryColor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseAdapter
import me.mauricee.pontoon.common.theme.PrimaryColor
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.common.theme.primaryColor
import me.mauricee.pontoon.databinding.ItemColorListBinding
import me.mauricee.pontoon.ui.preferences.CircleHelper
import me.mauricee.pontoon.ui.preferences.darken
import javax.inject.Inject

class PrimaryColorAdapter @Inject constructor(private val circleHelper: CircleHelper, private val themeManager: ThemeManager) : BaseAdapter<PrimaryColor, PrimaryColorAdapter.ViewHolder>() {

    private val colors = PrimaryColor.values()
    private var newSelectedColor: PrimaryColor? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ItemColorListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(this::ViewHolder)

    override fun getItemId(position: Int): Long = colors[position].ordinal.toLong()

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isOriginal = themeManager.primaryColor == colors[position]
        val isChecked = newSelectedColor == colors[position]
        val context = holder.itemView.context
        val color = colors[position].theme(context).primaryColor
        val drawable = when {
            isOriginal -> circleHelper.buildSelectedCircle(color, ContextCompat.getColor(context, R.color.colorThemeSelection))
            isChecked -> circleHelper.buildSelectedCircle(color, color.darken(.7f))
            else -> circleHelper.buildCircle(color)
        }
        holder.binding.color.setImageDrawable(drawable)
    }

    inner class ViewHolder(val binding: ItemColorListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            subscriptions += itemView.clicks().map { colors[bindingAdapterPosition] }
                    .subscribe {
                        newSelectedColor?.let {
                            notifyItemChanged(colors.indexOf(it))
                        }
                        newSelectedColor = it
                        relay.accept(it)
                        notifyItemChanged(bindingAdapterPosition)
                    }
        }
    }
}