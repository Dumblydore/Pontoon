package me.mauricee.pontoon.ui.main.player.details

import android.os.Bundle
import android.text.format.DateUtils
import android.text.util.Linkify
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.transition.TransitionInflater
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.item_details_post_comment.view.*
import kotlinx.android.synthetic.main.item_details_video.view.*
import kotlinx.android.synthetic.main.item_video_card.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleListAdapter
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.main.player.PlayerAction
import me.mauricee.pontoon.ui.main.player.PlayerViewModel
import me.mauricee.pontoon.ui.main.player.details.comment.CommentDialogFragment
import me.mauricee.pontoon.ui.main.player.details.replies.RepliesDialogFragment
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class DetailsFragment : NewBaseFragment(R.layout.fragment_details), DetailsContract.Navigator {

    @Inject
    lateinit var commentsAdapter: CommentAdapter

    @Inject
    lateinit var formatter: DateTimeFormatter

    @Inject
    lateinit var viewModel: PlayerViewModel

    private val detailsAdapter = SimpleListAdapter(R.layout.item_details_video, ::displayVideoInfo)
    private val relatedVideosAdapter = SimpleListAdapter(R.layout.item_video_list, ::bindRelatedVideo)
    private val postCommentAdapter = SimpleListAdapter(R.layout.item_details_post_comment, ::displayCurrentUser)
    private val contentAdapter by lazy { ConcatAdapter(detailsAdapter, relatedVideosAdapter, postCommentAdapter, commentsAdapter) }

    private val videoId: String by lazy { requireArguments().getString(VideoKey, "") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        subscriptions += commentsAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        details_content.adapter = contentAdapter
        subscriptions += relatedVideosAdapter.clicks.map { PlayerAction.PlayVideo(it.model.id) }
                .subscribe(viewModel::sendAction)
        subscriptions += postCommentAdapter.clicks.subscribe { comment(videoId) }
        subscriptions += commentsAdapter.actions.subscribe { }
        subscriptions += detailsAdapter.clicks.subscribe {
            it.view.apply {
                player_description.apply { isVisible = !isVisible }
                player_description_divider.isVisible = player_description.isVisible
                player_releaseDate.isVisible = player_description.isVisible
            }
        }
        viewModel.watchStateValue { video?.let(::listOf) }.notNull().observe(viewLifecycleOwner, detailsAdapter::submitList)
        viewModel.watchStateValue { relatedVideos }.observe(viewLifecycleOwner, relatedVideosAdapter::submitList)
        viewModel.watchStateValue { comments }.observe(viewLifecycleOwner) {
            commentsAdapter.submitList(it) { scrollToSelectedComment() }
        }
    }

    private fun scrollToSelectedComment() {
        arguments?.getString(CommentKey, "")?.apply {
            if (isNotBlank()) {
                details_content.smoothScrollToPosition(commentsAdapter.indexOf(this))
            }
        }
    }

    private fun displayCurrentUser(view: View, user: User) {
        GlideApp.with(this).load(user.entity.profileImage).circleCrop()
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(view.player_user_small_icon)
    }

    private fun displayVideoInfo(view: View, info: Video) {
        requireView().details_content.scrollTo(0, 0)
        info.entity.apply {
            view.player_title.text = title
            view.player_subtitle.text = info.creator.entity.name
            view.player_description.text = description
            view.player_releaseDate.text = getString(R.string.details_postDate, DateUtils.getRelativeTimeSpanString(releaseDate.toEpochMilli()))
            Linkify.addLinks(view.player_description, Linkify.WEB_URLS)
        }
        GlideApp.with(this).load(info.creator.user.profileImage).circleCrop()
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(view.player_small_icon)
    }

    private fun bindRelatedVideo(itemView: View, video: Video) {
        itemView.item_title.text = video.entity.title
        itemView.item_description.text = video.creator.entity.name
        GlideApp.with(itemView).load(video.entity.thumbnail)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(itemView.item_icon_big)
    }

    private fun snack(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(requireView(), text, duration).show()
    }

    override fun comment(videoId: String, comment: String?) {
        CommentDialogFragment.newInstance(videoId, comment).also { it.show(childFragmentManager, tag) }
    }

    override fun displayReplies(commentId: String) {
        RepliesDialogFragment.newInstance(commentId).show(childFragmentManager, tag)
    }

    override fun onCommentSuccess() {
        Snackbar.make(requireView(), R.string.details_commentPosted, Snackbar.LENGTH_LONG).show()
    }

    override fun onCommentError() {
        Snackbar.make(requireView(), R.string.details_error_commentPost, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private const val VideoKey = "VideoKey"
        private const val CommentKey = "CommentKey"

        fun newInstance(videoId: String, commentId: String = ""): DetailsFragment =
                DetailsFragment().also {
                    it.arguments = bundleOf(VideoKey to videoId, CommentKey to commentId)
                }
    }
}