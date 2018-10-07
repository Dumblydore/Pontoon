package me.mauricee.pontoon.main.details


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_comment.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseAdapter
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.comment.Comment
import javax.inject.Inject

//TODO Implement DiffUtil
class CommentAdapter @Inject constructor(context: Context)
    : BaseAdapter<DetailsContract.Action, CommentAdapter.ViewHolder>() {
    private val primaryColor = ContextCompat.getColor(context, R.color.md_grey_600)
    private val positiveColor = ContextCompat.getColor(context, R.color.colorPositive)
    private val negativeColor = ContextCompat.getColor(context, R.color.colorNegative)
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
            subscriptions += Observable.merge(view.item_icon_small.clicks(), view.item_title.clicks())
                    .map { comments[layoutPosition].user }
                    .map(DetailsContract.Action::ViewUser)
                    .subscribe(relay::accept)

            subscriptions += Observable.merge(view.item_thumb_up.clicks().map { comments[layoutPosition] }.map(DetailsContract.Action::Like),
                    view.item_thumb_down.clicks().map { comments[layoutPosition] }.map(DetailsContract.Action::Dislike))
                    .subscribe(relay::accept)

            subscriptions += view.item_comment.clicks().map { comments[layoutPosition] }
                    .map(DetailsContract.Action::Reply).subscribe(relay::accept)

            subscriptions += view.item_viewReplies.clicks().map { comments[layoutPosition] }
                    .map(DetailsContract.Action::ViewReplies).subscribe(relay::accept)
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
                    positiveColor
                else
                    primaryColor

                val dislikeTint = if (comment.userInteraction.contains(Comment.Interaction.Dislike))
                    negativeColor
                else
                    primaryColor

                DrawableCompat.setTint(itemView.item_thumb_up.drawable.mutate(), likeTint)
                DrawableCompat.setTint(itemView.item_thumb_down.drawable.mutate(), dislikeTint)

                GlideApp.with(itemView).load(comment.user.profileImage)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(itemView.item_icon_small)
            }
        }
    }
}