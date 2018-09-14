package me.mauricee.pontoon.main.videos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_end_user_bubble.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseAdapter
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

class SubscriptionAdapter @Inject constructor() : BaseAdapter<VideoContract.Action, SubscriptionAdapter.ViewHolder>() {

    var user: List<UserRepository.Creator> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return if (position == user.lastIndex) R.layout.item_end_user_bubble else R.layout.item_user_bubble
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(viewType, parent, false).let(this::ViewHolder)

    override fun getItemCount(): Int = user.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(user[position])


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            subscriptions += view.findViewById<View>(R.id.item_icon_small).clicks()
                    .map { VideoContract.Action.Subscription(user[layoutPosition]) }
                    .subscribe(relay::accept)

            view.item_icon_viewAll?.clicks()?.map { VideoContract.Action.Creators }?.subscribe(relay::accept)
                    ?.also { subscriptions += it }
        }

        fun bind(user: UserRepository.Creator) {
            itemView.apply {
                GlideApp.with(this).load(user.user.profileImage).circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemView.findViewById(R.id.item_icon_small))
            }
        }
    }
}