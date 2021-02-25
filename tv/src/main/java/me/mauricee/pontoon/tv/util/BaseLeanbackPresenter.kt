package me.mauricee.pontoon.tv.util

import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import androidx.viewbinding.ViewBinding

abstract class BaseLeanbackPresenter<T : Any, VB : ViewBinding> : Presenter() {


    final override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return Holder(createViewBinding(parent))
    }

    final override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        bind(item as T, (viewHolder as BaseLeanbackPresenter<T, VB>.Holder).binding)
    }

    final override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?, payloads: MutableList<Any>?) {
        super.onBindViewHolder(viewHolder, item, payloads)
    }

    final override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        unbind((viewHolder as BaseLeanbackPresenter<T, VB>.Holder).binding)
    }

    final override fun setOnClickListener(holder: ViewHolder, listener: View.OnClickListener) {
        super.setOnClickListener(holder, listener)
    }

    protected abstract fun createViewBinding(parent: ViewGroup): VB

    protected abstract fun bind(item: T, binding: VB)

    protected abstract fun unbind(binding: VB)

    inner class Holder(val binding: VB) : ViewHolder(binding.root)

}