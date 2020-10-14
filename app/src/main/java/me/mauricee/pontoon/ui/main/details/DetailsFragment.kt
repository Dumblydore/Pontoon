package me.mauricee.pontoon.ui.main.details

import android.os.Bundle
import android.text.format.DateUtils
import android.text.util.Linkify
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.transition.TransitionInflater
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.item_details_post_comment.view.*
import kotlinx.android.synthetic.main.item_details_video.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleListAdapter
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.main.details.comment.CommentDialogFragment
import me.mauricee.pontoon.ui.main.details.replies.RepliesDialogFragment
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class DetailsFragment : BaseFragment<DetailsPresenter>(), DetailsContract.View, DetailsContract.Navigator {

    @Inject
    lateinit var relatedVideosAdapter: RelatedVideoAdapter

    @Inject
    lateinit var commentsAdapter: CommentAdapter
    private val detailsAdapter = SimpleListAdapter(R.layout.item_details_video, ::displayVideoInfo)
    private val postCommentAdapter = SimpleListAdapter(R.layout.item_details_post_comment, ::displayCurrentUser)

    @Inject
    lateinit var formatter: DateTimeFormatter

    override fun getLayoutId(): Int = R.layout.fragment_details

    override val actions: Observable<DetailsContract.Action>
        get() = Observable.merge(commentsAdapter.actions,
                relatedVideosAdapter.actions,
                postCommentAdapter.clicks.map { DetailsContract.Action.PostComment }
                /*player_small_icon.clicks().map { DetailsContract.Action.ViewCreator }*/)
                .startWith(DetailsContract.Action.PlayVideo(requireArguments().getString(VideoKey)!!))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        subscriptions += commentsAdapter
        subscriptions += relatedVideosAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        details_content.adapter = ConcatAdapter(detailsAdapter, relatedVideosAdapter, postCommentAdapter, commentsAdapter)
        subscriptions += detailsAdapter.clicks.subscribe {
            it.view.apply {
                player_description.apply { isVisible = !isVisible }
                player_description_divider.isVisible = player_description.isVisible
                player_releaseDate.isVisible = player_description.isVisible
            }
        }
    }

    override fun updateState(state: DetailsContract.State) = when (state) {
        is DetailsContract.State.Loading -> {
        }
        is DetailsContract.State.VideoInfo -> detailsAdapter.submitList(listOf(state.video))
        is DetailsContract.State.Error -> handleError(state.type)
        is DetailsContract.State.Comments -> {
            commentsAdapter.submitList(state.comments)
            scrollToSelectedComment()
        }
        is DetailsContract.State.RelatedVideos -> relatedVideosAdapter.submitList(state.relatedVideos)
        is DetailsContract.State.Like -> snack(getString(R.string.details_commentLiked, state.comment.user.username))
        is DetailsContract.State.Dislike -> snack(getString(R.string.details_commentDisliked, state.comment.user.username))
        is DetailsContract.State.CurrentUser -> postCommentAdapter.submitList(listOf(state.user))
    }

    private fun handleError(type: DetailsContract.ErrorType) {
        when (type) {
            DetailsContract.ErrorType.Like,
            DetailsContract.ErrorType.Dislike -> Snackbar.make(requireView(), type.message, Snackbar.LENGTH_LONG).show()
            else -> {
//                details.state = LazyLayout.ERROR
//                details.errorText = getString(type.message)
            }
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