package me.mauricee.pontoon.ui.main.details

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
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

class RelatedVideoAdapter @Inject constructor() : BaseListAdapter<DetailsContract.Action, Video, RelatedVideoAdapter.ViewHolder>(SimpleListAdapter.ItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_video_list, parent, false).let(this::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            subscriptions += view.clicks().map {
                logd("Current list size: ")
                DetailsContract.Action.PlayVideo(getItem(adapterPosition).id)
            }
                    .subscribe(relay::accept)
        }

        fun bind(video: me.mauricee.pontoon.model.video.Video) {
            itemView.let {
                itemView.item_title.text = video.entity.title
                itemView.item_description.text = video.creator.entity.name
                GlideApp.with(itemView).load(video.entity.thumbnail)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.ic_default_thumbnail)
                        .error(R.drawable.ic_default_thumbnail)
                        .into(itemView.item_icon_big)
            }
        }
    }
}