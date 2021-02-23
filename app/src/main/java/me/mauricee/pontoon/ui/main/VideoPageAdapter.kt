package me.mauricee.pontoon.ui.main

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.ItemVideoCardBinding
import me.mauricee.pontoon.ext.getActivity
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.ui.util.diff.DiffableItemCallback
import javax.inject.Inject

open class VideoPageAdapter @Inject constructor() : PagedListAdapter<Video, VideoPageAdapter.ViewHolder>(DiffableItemCallback()), Disposable {

    internal val subscriptions = CompositeDisposable()

    var contextVideo: Video? = null
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ItemVideoCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
    ).let(::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    override fun isDisposed(): Boolean = subscriptions.isDisposed

    override fun dispose() = subscriptions.dispose()

    private val relay = PublishRelay.create<Video>()
    open val actions: Observable<Video>
        get() = relay

    inner class ViewHolder(val binding: ItemVideoCardBinding) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        init {
            subscriptions += binding.item.clicks().subscribe { getItem(bindingAdapterPosition)?.let(relay::accept) }
            binding.itemMenu.apply {
                subscriptions += clicks().subscribe {
                    binding.root.getActivity()?.openContextMenu(this)
                    contextVideo = getItem(bindingAdapterPosition)
                }
            }
            binding.itemMenu.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            v.getActivity()?.menuInflater?.inflate(R.menu.menu_video, menu)
        }

        fun bind(video: Video) {
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