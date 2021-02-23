package me.mauricee.pontoon.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import mauricee.me.pontoon.data.common.Diffable
import me.mauricee.pontoon.ui.util.diff.DiffableItemCallback

class SimpleBindingAdapter<VM : ViewBinding, M : Diffable<*>>(
        private val onCreate: (LayoutInflater, ViewGroup, Boolean) -> VM,
        private val onBind: (VM, M) -> Unit,
) : ListAdapter<M, SimpleBindingAdapter<VM, M>.ViewHolder>(DiffableItemCallback<M>()) {

    private val _clicks = PublishSubject.create<ClickEvent<VM, M>>()
    val clicks: Observable<ClickEvent<VM, M>>
        get() = _clicks.hide()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(onCreate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { onBind(holder.binding, it) }
    }


    inner class ViewHolder(val binding: VM) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) = _clicks.onNext(ClickEvent(binding, bindingAdapterPosition, getItem(bindingAdapterPosition)))
    }

    data class ClickEvent<VM, M>(val view: VM, val position: Int, val model: M)
}