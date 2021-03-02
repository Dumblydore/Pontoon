package me.mauricee.pontoon.ui.util.diff

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import mauricee.me.pontoon.data.common.Diffable

class DiffableItemCallback<M : Diffable<*>> : DiffUtil.ItemCallback<M>() {
    override fun areItemsTheSame(oldItem: M, newItem: M): Boolean = oldItem.id == newItem.id

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: M, newItem: M): Boolean = oldItem == newItem
}