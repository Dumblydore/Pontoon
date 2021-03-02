package me.mauricee.pontoon.ui.main.player.details


import android.content.Context
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseListAdapter
import me.mauricee.pontoon.data.local.comment.CommentInteractionType
import me.mauricee.pontoon.databinding.ItemCommentBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.repository.comment.Comment
import me.mauricee.pontoon.ui.main.player.PlayerAction
import me.mauricee.pontoon.ui.util.diff.DiffableItemCallback
import javax.inject.Inject

class CommentAdapter @Inject constructor(context: Context) : BaseListAdapter<PlayerAction, Comment, CommentAdapter.ViewHolder>(DiffableItemCallback()) {
    private val primaryColor = ContextCompat.getColor(context, R.color.md_grey_600)
    private val positiveColor = ContextCompat.getColor(context, R.color.colorPositive)
    private val negativeColor = ContextCompat.getColor(context, R.color.colorNegative)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false).let(this::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            subscriptions += Observable.merge(binding.itemIconSmall.clicks(), binding.itemTitle.clicks())
                    .map { getItem(bindingAdapterPosition).user }
                    .map(PlayerAction::ViewUser)
                    .subscribe(relay::accept)

            subscriptions += Observable.merge(binding.itemThumbUp.clicks().map { getItem(bindingAdapterPosition) }.map(PlayerAction::Like),
                    binding.itemThumbDown.clicks().map { getItem(bindingAdapterPosition) }.map(PlayerAction::Dislike))
                    .subscribe(relay::accept)

            subscriptions += binding.itemComment.clicks().map { getItem(bindingAdapterPosition) }
                    .map(PlayerAction::Reply).subscribe(relay::accept)

            subscriptions += binding.itemViewReplies.clicks().map { getItem(bindingAdapterPosition) }
                    .map(PlayerAction::ViewReplies).subscribe(relay::accept)
        }

        fun bind(comment: Comment) {
            binding.apply {
                val commentScore = comment.score
                itemTitle.text = comment.user.username
                itemComment.text = comment.text
                LinkifyCompat.addLinks(itemComment, Linkify.WEB_URLS)
                itemThumbText.isVisible = commentScore != 0
                itemThumbText.text = "${if (commentScore > 0) "+" else ""} $commentScore"
                itemViewReplies.isVisible = comment.replies.isNotEmpty()
                itemViewReplies.text = itemView.context.resources.getQuantityString(R.plurals.details_comment_replies, comment.replies.size, comment.replies.size)

                val (likeTint, dislikeTint) = when (comment.userInteraction) {
                    CommentInteractionType.Like -> positiveColor to primaryColor
                    CommentInteractionType.Dislike -> primaryColor to negativeColor
                    else -> primaryColor to primaryColor
                }

                DrawableCompat.setTint(itemThumbUp.icon.mutate(), likeTint)
                DrawableCompat.setTint(itemThumbDown.icon.mutate(), dislikeTint)

                GlideApp.with(itemView).load(comment.user.profileImage)
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade(150))
                        .into(itemIconSmall)
            }
        }
    }
}