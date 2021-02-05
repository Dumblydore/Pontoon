package me.mauricee.pontoon.ui.main.videos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_end_user_bubble.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseListAdapter
import me.mauricee.pontoon.common.SimpleListAdapter
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.creator.Creator
import javax.inject.Inject

class SubscriptionAdapter @Inject constructor() : BaseListAdapter<VideoAction, Creator, SubscriptionAdapter.ViewHolder>(SimpleListAdapter.ItemCallback()) {

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) R.layout.item_end_user_bubble else R.layout.item_user_bubble
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(viewType, parent, false).let(this::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            subscriptions += view.findViewById<View>(R.id.item_icon_small).clicks()
                    .map { VideoAction.Subscription(getItem(bindingAdapterPosition)) }
                    .subscribe(relay::accept)

            view.item_icon_viewAll?.clicks()?.map { VideoAction.Creators }?.subscribe(relay::accept)
                    ?.also { subscriptions += it }
        }

        fun bind(user: Creator) {
            itemView.apply {
                GlideApp.with(this).load(user.user.profileImage).circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemView.findViewById(R.id.item_icon_small))
            }
        }
    }
}