package me.mauricee.pontoon.tv.util

import androidx.leanback.widget.ArrayObjectAdapter
import androidx.recyclerview.widget.ListUpdateCallback

class ObjectAdapterListCallback<T : Any>(private val provider: ListProvider<T>, private val adapter: ArrayObjectAdapter) : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {
        adapter.addAll(position, provider.currentList?.subList(position, count))
    }

    override fun onRemoved(position: Int, count: Int) {
        adapter.removeItems(position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.move(fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        adapter.notifyArrayItemRangeChanged(position, count)
    }

    interface ListProvider<T> {
        val currentList: List<T>?
    }
}