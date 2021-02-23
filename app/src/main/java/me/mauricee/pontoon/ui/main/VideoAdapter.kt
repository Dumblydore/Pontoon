package me.mauricee.pontoon.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseListAdapter
import me.mauricee.pontoon.databinding.ItemVideoCardBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.ui.util.diff.DiffableItemCallback
import javax.inject.Inject

class VideoAdapter @Inject constructor() : BaseListAdapter<Video, Video, VideoAdapter.ViewHolder>(DiffableItemCallback()) {

    var videos: List<Video> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = videos.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ItemVideoCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    .let(::ViewHolder)


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        videos[position].let(holder::bind)
    }

    inner class ViewHolder(private val binding: ItemVideoCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            subscriptions += itemView.clicks().subscribe { videos[bindingAdapterPosition].let(relay::accept) }
        }

        fun bind(video: Video) {
            itemView.apply {
                binding.itemTitle.text = video.title
                binding.itemDescription.text = video.creator.name
                GlideApp.with(itemView).load(video.thumbnail)
                        .placeholder(R.drawable.ic_default_thumbnail)
                        .error(R.drawable.ic_default_thumbnail)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.itemIconBig)
                GlideApp.with(itemView).load(video.creator.owner.profileImage)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.itemIconSmall)
            }
        }
    }
}