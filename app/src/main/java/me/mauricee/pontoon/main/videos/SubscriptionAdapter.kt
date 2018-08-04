package me.mauricee.pontoon.main.videos

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
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

class SubscriptionAdapter @Inject constructor(): RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {
    private val relay = PublishRelay.create<VideoContract.Action>()
    val actions: Observable<VideoContract.Action>
        get() = relay

    var user: List<UserRepository.Creator> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_bubble, parent, false).let(this::ViewHolder)

    override fun getItemCount(): Int = user.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(user[position])


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.clicks().map { VideoContract.Action.Subscription(user[layoutPosition]) }
                    .subscribe(relay::accept)
        }


        fun bind(user: UserRepository.Creator) {
            itemView.apply {
                GlideApp.with(this).load(user.user.profileImage).circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemView.item_icon_small)
            }
        }
    }
}