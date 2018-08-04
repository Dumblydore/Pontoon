package me.mauricee.pontoon.main.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_video_card.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

class SearchResultAdapter @Inject constructor() : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    private val relay = PublishRelay.create<SearchContract.Action>()
    val actions: Observable<SearchContract.Action>
        get() = relay
    private var videos: List<Video> = emptyList()

    init {
        setHasStableIds(true)
    }

    fun update(state: SearchContract.State.Results) {
        videos = state.list
        state.result.dispatchUpdatesTo(this)
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
            view.clicks().subscribe { relay.accept(SearchContract.Action.PlayVideo(videos[layoutPosition])) }
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
                        .placeholder(R.drawable.ic_default_thumbnail)
                        .error(R.drawable.ic_default_thumbnail)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemView.item_icon_small)
            }
        }
    }
}