package me.mauricee.pontoon.ui.main

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_end_user_bubble.view.item_icon_viewAll
import kotlinx.android.synthetic.main.item_video_card.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleListAdapter
import me.mauricee.pontoon.ext.getActivity
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

open class VideoPageAdapter @Inject constructor() : PagedListAdapter<Video, VideoPageAdapter.ViewHolder>(SimpleListAdapter.ItemCallback()), Disposable {

    internal val subscriptions = CompositeDisposable()

    var contextVideo: Video? = null
        private set

    override fun getItemViewType(position: Int): Int = R.layout.item_video_card

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false)
            .let(this::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let(holder::bind)
    }

    override fun isDisposed(): Boolean = subscriptions.isDisposed

    override fun dispose() = subscriptions.dispose()

    private val relay = PublishRelay.create<Video>()
    open val actions: Observable<Video>
        get() = relay

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {

        init {
            subscriptions += view.item.clicks().subscribe { getItem(adapterPosition)?.let(relay::accept) }
            view.item_menu?.apply {
                subscriptions += clicks().subscribe {
                    view.getActivity()?.openContextMenu(this)
                    contextVideo = getItem(adapterPosition)
                }
            }
            view.item_icon_viewAll?.let {
                subscriptions += it.clicks().subscribe { getItem(adapterPosition)?.let(relay::accept) }
            }
            view.item_menu.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            v.getActivity()?.menuInflater?.inflate(R.menu.menu_video, menu)
        }

        fun bind(video: Video) {
            itemView.apply {
                itemView.item_title.text = video.entity.title
                itemView.item_description.text = video.creator.entity.name
                GlideApp.with(itemView).load(video.entity.thumbnail)
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