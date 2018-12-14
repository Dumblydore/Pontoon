package me.mauricee.pontoon.main.details


import android.content.Context
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_comment.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseAdapter
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.comment.Comment
import javax.inject.Inject

class CommentAdapter @Inject constructor(context: Context)
    : BaseAdapter<Comment, DetailsContract.Action, CommentAdapter.ViewHolder>(Comment.ItemCallback) {
    private val primaryColor = ContextCompat.getColor(context, R.color.md_grey_600)
    private val positiveColor = ContextCompat.getColor(context, R.color.colorPositive)
    private val negativeColor = ContextCompat.getColor(context, R.color.colorNegative)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false).let(this::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateComment(comment: Comment) {
        submitList(mutableListOf(comment))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            subscriptions += Observable.merge(view.item_icon_small.clicks(), view.item_title.clicks())
                    .map { getItem(layoutPosition).user }
                    .map(DetailsContract.Action::ViewUser)
                    .subscribe(relay::accept)

            subscriptions += Observable.merge(view.item_thumb_up.clicks().map { getItem(layoutPosition) }.map(DetailsContract.Action::Like),
                    view.item_thumb_down.clicks().map { getItem(layoutPosition) }.map(DetailsContract.Action::Dislike))
                    .subscribe(relay::accept)

            subscriptions += view.item_comment.clicks().map { getItem(layoutPosition) }
                    .map(DetailsContract.Action::Reply).subscribe(relay::accept)

            subscriptions += view.item_viewReplies.clicks().map { getItem(layoutPosition) }
                    .map(DetailsContract.Action::ViewReplies).subscribe(relay::accept)
        }

        fun bind(comment: Comment) {
            itemView.let {
                val commentScore = comment.likes - comment.dislikes
                it.item_title.text = comment.user.username
                it.item_comment.text = comment.text
                LinkifyCompat.addLinks(it.item_comment, Linkify.WEB_URLS)
                it.item_thumb_text.isVisible = commentScore != 0
                it.item_thumb_text.text = "${if (commentScore > 0) "+" else ""} $commentScore"
                it.item_viewReplies.isVisible = comment.replies.isNotEmpty()
                it.item_viewReplies.text = itemView.context.resources.getQuantityString(R.plurals.details_comment_replies, comment.replies.size, comment.replies.size)

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