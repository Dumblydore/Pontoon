package me.mauricee.pontoon.ui.main.videos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseListAdapter
import me.mauricee.pontoon.databinding.ItemEndUserBubbleBinding
import me.mauricee.pontoon.databinding.ItemUserBubbleBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.Diffable
import me.mauricee.pontoon.model.creator.Creator
import javax.inject.Inject

class SubscriptionAdapter @Inject constructor() : BaseListAdapter<VideoAction, Creator, SubscriptionAdapter.ViewHolder>(Diffable.ItemCallback()) {

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) R.layout.item_end_user_bubble else R.layout.item_user_bubble
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = when (viewType) {
        R.layout.item_user_bubble -> ItemUserBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false).let(::ItemViewHolder)
        R.layout.item_end_user_bubble -> ItemEndUserBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false).let(::EndViewHolder)
        else -> throw RuntimeException("Invalid view type!")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(creator: Creator)
    }

    inner class ItemViewHolder(private val binding: ItemUserBubbleBinding) : ViewHolder(binding.root) {
        init {
            subscriptions += binding.itemIconSmall.clicks()
                    .map { VideoAction.Subscription(getItem(bindingAdapterPosition)) }
                    .subscribe(relay::accept)
        }

        override fun bind(creator: Creator) {
            GlideApp.with(itemView).load(creator.user.profileImage).circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.itemIconSmall)
        }
    }

    inner class EndViewHolder(private val binding: ItemEndUserBubbleBinding) : ViewHolder(binding.root) {
        init {
            subscriptions += binding.itemIconViewAll.clicks()
                    .map { VideoAction.Creators }
                    .subscribe(relay::accept)
            subscriptions += binding.itemIconSmall.clicks()
                    .map { VideoAction.Subscription(getItem(bindingAdapterPosition)) }
                    .subscribe(relay::accept)
        }

        override fun bind(creator: Creator) {
            GlideApp.with(itemView).load(creator.user.profileImage).circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.itemIconSmall)
        }
    }
}