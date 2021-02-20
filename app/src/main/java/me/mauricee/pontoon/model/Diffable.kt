package me.mauricee.pontoon.model

import androidx.recyclerview.widget.DiffUtil

interface Diffable<T> {
    val id: T
    class ItemCallback<M : Diffable<*>> : DiffUtil.ItemCallback<M>() {
        override fun areItemsTheSame(oldItem: M, newItem: M): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: M, newItem: M): Boolean = oldItem == newItem
    }
}