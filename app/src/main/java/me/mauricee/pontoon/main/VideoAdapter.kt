package me.mauricee.pontoon.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_video_card.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

class VideoAdapter @Inject constructor() : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private val relay = PublishRelay.create<Video>()
    val actions: Observable<Video>
        get() = relay
    private var videos: List<Video> = emptyList()


    init {
        setHasStableIds(true)
    }

    operator fun plusAssign(newVideos: List<Video>) {
        videos.size.also {
            videos += newVideos
            notifyItemRangeInserted(it, videos.size)
        }
    }

    fun clear() {
        videos.apply {
            videos = emptyList()
            notifyItemRangeRemoved(0, size)
        }
    }


    override fun getItemCount(): Int = videos.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_video_card, parent, false)
                    .let(this::ViewHolder)


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        videos[position].let(holder::bind)
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.clicks().subscribe { relay.accept(videos[layoutPosition]) }
        }

        fun bind(video: Video) {
            itemView.apply {
                itemView.item_title.text = video.title
                itemView.item_description.text = video.creator.name
                GlideApp.with(itemView).load(video.thumbnail)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemView.item_icon_big)
                GlideApp.with(itemView).load(video.creator.user.profileImage)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemView.item_icon_small)
            }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean = newItem == oldItem

        }
    }
}