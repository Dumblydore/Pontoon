package me.mauricee.pontoon.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import me.mauricee.pontoon.model.Diffable

class SimpleListAdapter<M : Diffable<*>>(
        private val onCreate: (inflater: LayoutInflater, parent: ViewGroup, viewType: Int) -> View,
        private val onBind: (view: View, item: M) -> Unit,
) : ListAdapter<M, SimpleListAdapter<M>.ViewHolder>(ItemCallback<M>()) {

    private val _clicks = PublishSubject.create<ClickEvent<M>>()
    val clicks: Observable<ClickEvent<M>>
        get() = _clicks.hide()

    constructor(@LayoutRes layoutId: Int, onBind: (view: View, item: M) -> Unit) : this(
            onCreate = { inflater, parent, _ -> inflater.inflate(layoutId, parent, false) },
            onBind = onBind
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(onCreate(LayoutInflater.from(parent.context), parent, viewType))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { onBind(holder.itemView, it) }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) = _clicks.onNext(ClickEvent(v, adapterPosition, getItem(adapterPosition)))
    }

    data class ClickEvent<M>(val view: View, val position: Int, val model: M)

    class ItemCallback<M : Diffable<*>> : DiffUtil.ItemCallback<M>() {
        override fun areItemsTheSame(oldItem: M, newItem: M): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: M, newItem: M): Boolean = oldItem == newItem
    }
}