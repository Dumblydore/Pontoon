package me.mauricee.pontoon.ui.main.videos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.mauricee.pontoon.databinding.ItemHeaderSubscriptionsBinding

class VideoHeaderAdapter(private val adapter: SubscriptionAdapter) : RecyclerView.Adapter<VideoHeaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ItemHeaderSubscriptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemBinding.itemSubscriptions.adapter = adapter
    }

    override fun getItemCount(): Int = 1

    inner class ViewHolder(val itemBinding: ItemHeaderSubscriptionsBinding) : RecyclerView.ViewHolder(itemBinding.root)
}