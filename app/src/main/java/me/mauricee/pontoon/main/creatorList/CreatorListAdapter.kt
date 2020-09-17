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
import me.mauricee.pontoon.common.SimpleListAdapter
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.creator.Creator
import javax.inject.Inject

class CreatorListAdapter @Inject constructor() : BaseListAdapter<CreatorListContract.Action, Creator, CreatorListAdapter.ViewHolder>(SimpleListAdapter.ItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_creator_card, parent, false).let(this::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            subscriptions += view.clicks().map { CreatorListContract.Action.CreatorSelected(getItem(adapterPosition)) }
                    .subscribe(relay::accept)
        }

        fun bind(creator: Creator) {
            itemView.item_title.text = creator.entity.name
            GlideApp.with(itemView).load(creator.user.profileImage)
                    .placeholder(R.drawable.ic_default_thumbnail)
                    .error(R.drawable.ic_default_thumbnail)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(itemView.item_icon_big)
        }
    }
}