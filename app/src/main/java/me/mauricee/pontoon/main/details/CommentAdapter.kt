package me.mauricee.pontoon.main.details


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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

    var comments: MutableList<Comment> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false).let(this::ViewHolder)

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(comments[position])

    fun updateComment(comment: Comment) {
        val index = comments.indexOf(comment)
        if (index >= 0) {
            comments[index] = comment
            notifyItemChanged(index)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            Observable.merge(view.item_icon_small.clicks(), view.item_title.clicks())
                    .map { comments[layoutPosition].user }
                    .map(DetailsContract.Action::ViewUser)
                    .subscribe(relay::accept)

            Observable.merge(view.item_thumb_up.clicks().map { comments[layoutPosition] }.map(DetailsContract.Action::Like),
                    view.item_thumb_down.clicks().map { comments[layoutPosition] }.map(DetailsContract.Action::Dislike))
                    .subscribe(relay::accept)
        }

        fun bind(comment: Comment) {
            itemView.let {
                val commentScore = comment.likes - comment.dislikes
                itemView.item_title.text = comment.user.username
                itemView.item_comment.text = comment.text
                itemView.item_thumb_text.isVisible = commentScore != 0
                itemView.item_thumb_text.text = "${if (commentScore > 0) "+" else ""} $commentScore"
                itemView.item_viewReplies.isVisible = comment.replies.isNotEmpty()
                itemView.item_viewReplies.text = itemView.context.getString(R.string.details_comment_replies, comment.replies.size)

                val likeTint = if (comment.userInteraction.contains(Comment.Interaction.Like))
                    R.color.colorPositive
                else
                    R.color.textDarkDisabled

                val dislikeTint = if (comment.userInteraction.contains(Comment.Interaction.Dislike))
                    R.color.colorNegative
                else
                    R.color.textDarkDisabled

                itemView.item_thumb_up.drawable.setTint(ContextCompat.getColor(itemView.context, likeTint))
                itemView.item_thumb_down.drawable.setTint(ContextCompat.getColor(itemView.context, dislikeTint))

                GlideApp.with(itemView).load(comment.user.profileImage)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(itemView.item_icon_small)
            }
        }
    }
}