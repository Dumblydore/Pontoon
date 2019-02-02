package me.mauricee.pontoon.main.creatorList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_video_card.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseListAdapter
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

class CreatorListAdapter @Inject constructor() :
        BaseListAdapter<CreatorListContract.Action, UserRepository.Creator, CreatorListAdapter.ViewHolder>(UserRepository.Creator.ItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_creator_card, parent, false).let(this::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            subscriptions += view.clicks().map { CreatorListContract.Action.Creator(getItem(layoutPosition)) }
                    .subscribe(relay::accept)
        }

        fun bind(creator: UserRepository.Creator) {
            itemView.item_title.text = creator.name
            GlideApp.with(itemView).load(creator.user.profileImage)
                    .placeholder(R.drawable.ic_default_thumbnail)
                    .error(R.drawable.ic_default_thumbnail)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(itemView.item_icon_big)
        }
    }
}