package me.mauricee.pontoon.main.details


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_comment.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.comment.Comment
import javax.inject.Inject

class CommentAdapter @Inject constructor() : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    private val relay = PublishRelay.create<DetailsContract.Action>()
    val actions: Observable<DetailsContract.Action>
        get() = relay


    var comments: List<Comment> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false).let(this::ViewHolder)

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(comments[position])

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            Observable.merge(view.item_icon_small.clicks(), view.item_title.clicks())
                    .map { comments[layoutPosition].user }
                    .map(DetailsContract.Action::ViewUser)
                    .subscribe(relay::accept)
        }

        fun bind(comment: Comment) {
            itemView.let {
                itemView.item_title.text = comment.user.username
                itemView.item_comment.text = comment.text
                GlideApp.with(itemView).load(comment.user.profileImage)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(itemView.item_icon_small)
            }
        }
    }
}