package me.mauricee.pontoon.main

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
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

class VideoAdapter @Inject constructor() : BaseListAdapter<Video, Video, VideoAdapter.ViewHolder>(Video.ItemCallback) {

    var videos: List<Video> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = videos.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_card, parent, false)
            .let(this::ViewHolder)


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        videos[position].let(holder::bind)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            subscriptions += view.clicks().subscribe { videos[layoutPosition].let(relay::accept) }
        }

        fun bind(video: Video) {
            itemView.apply {
                itemView.item_title.text = video.title
                itemView.item_description.text = video.creator.name
                GlideApp.with(itemView).load(video.thumbnail)
                        .placeholder(R.drawable.ic_default_thumbnail)
                        .error(R.drawable.ic_default_thumbnail)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemView.item_icon_big)
                GlideApp.with(itemView).load(video.creator.user.profileImage)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemView.item_icon_small)
            }
        }
    }
}