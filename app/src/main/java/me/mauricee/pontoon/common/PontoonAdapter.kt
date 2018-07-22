package me.mauricee.pontoon.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import me.mauricee.pontoon.main.videos.VideoContract

abstract class PontoonAdapter<M, S> : RecyclerView.Adapter<PontoonAdapter<M, S>.ViewHolder>() {
    private val relay = PublishRelay.create<S>()
    val actions: Observable<S>
        get() = relay

    var values: List<M> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(values[position])

    override fun getItemCount(): Int = values.size

    abstract inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(model: M)
    }
}