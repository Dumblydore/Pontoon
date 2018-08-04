package me.mauricee.pontoon.main.details


//import me.mauricee.pontoon.glide.GlideApp
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_video_card.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.glide.GlideApp
import javax.inject.Inject

class RelatedVideoAdapter @Inject constructor() : RecyclerView.Adapter<RelatedVideoAdapter.ViewHolder>() {
    private val relay = PublishRelay.create<DetailsContract.Action>()
    val actions: Observable<DetailsContract.Action>
        get() = relay

    var videos: List<me.mauricee.pontoon.model.video.Video> = emptyList()
        set(value) {
            Observable.fromCallable {
                DiffUtil.calculateDiff(DiffCallback(field, value))
            }.compose(RxHelpers.applyObservableSchedulers()).subscribe {
                field = value
                it.dispatchUpdatesTo(this)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_video_list, parent, false).let(this::ViewHolder)

    override fun getItemCount(): Int = videos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(videos[position])


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.clicks().map { DetailsContract.Action.PlayVideo(videos[layoutPosition].id) }
                    .subscribe(relay::accept)
        }


        fun bind(video: me.mauricee.pontoon.model.video.Video) {
            itemView.let {
                itemView.item_title.text = video.title
                itemView.item_description.text = video.creator.name
                GlideApp.with(itemView).load(video.thumbnail)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.ic_default_thumbnail)
                        .error(R.drawable.ic_default_thumbnail)
                        .into(itemView.item_icon_big)

            }
        }
    }

    inner class DiffCallback(private val old: List<me.mauricee.pontoon.model.video.Video>,
                             private val new: List<me.mauricee.pontoon.model.video.Video>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                old[oldItemPosition].id === new[newItemPosition].id

        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                old[oldItemPosition] == new[newItemPosition]
    }
}