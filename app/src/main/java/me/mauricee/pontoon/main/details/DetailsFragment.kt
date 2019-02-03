package me.mauricee.pontoon.main.details

import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_details.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.main.details.comment.CommentDialogFragment
import me.mauricee.pontoon.main.details.replies.RepliesDialogFragment
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class DetailsFragment : BaseFragment<DetailsPresenter>(), DetailsContract.View, DetailsContract.Navigator {

    @Inject
    lateinit var relatedVideosAdapter: RelatedVideoAdapter
    @Inject
    lateinit var commentsAdapter: CommentAdapter
    @Inject
    lateinit var formatter: DateTimeFormatter

    private lateinit var currentUser: UserRepository.User


    override fun getLayoutId(): Int = R.layout.fragment_details

    override val actions: Observable<DetailsContract.Action>
        get() = Observable.merge(commentsAdapter.actions,
                relatedVideosAdapter.actions,
                player_addComment.clicks().map { DetailsContract.Action.Comment },
                player_small_icon.clicks().map { DetailsContract.Action.ViewCreator })
                .startWith(DetailsContract.Action.PlayVideo(arguments!!.getString(VideoKey)))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        subscriptions += commentsAdapter
        subscriptions += relatedVideosAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player_relatedVideos.adapter = relatedVideosAdapter
        player_relatedVideos.layoutManager = LinearLayoutManager(requireContext())

        player_comments.adapter = commentsAdapter
        player_comments.layoutManager = LinearLayoutManager(requireContext())

        subscriptions += player_info.clicks().subscribe {
            player_description.apply { isVisible = !isVisible }
            player_description_divider.isVisible = player_description.isVisible
            player_releaseDate.isVisible = player_description.isVisible
        }
    }

    override fun updateState(state: DetailsContract.State) {
        when (state) {
            is DetailsContract.State.Loading -> {
                details.setState(LazyLayout.LOADING, false)
            }
            is DetailsContract.State.VideoInfo -> {
                details.state = LazyLayout.SUCCESS
                displayVideoInfo(state.video)
            }
            is DetailsContract.State.Error -> handleError(state.type)
            is DetailsContract.State.Comments -> {
                commentsAdapter.submitList(state.comments.toMutableList())
                scrollToSelectedComment()
            }
            is DetailsContract.State.RelatedVideos -> {
                relatedVideosAdapter.submitList(state.relatedVideos)
                player_relatedVideos_divider.isVisible = true
            }
            is DetailsContract.State.Like -> {
                commentsAdapter.submitList(listOf(state.comment))
                snack(getString(R.string.details_commentLiked, state.comment.user.username))
            }
            is DetailsContract.State.Dislike -> {
                commentsAdapter.submitList(listOf(state.comment))
                snack(getString(R.string.details_commentDisliked, state.comment.user.username))
            }
            is DetailsContract.State.CurrentUser -> {
                currentUser = state.user
                GlideApp.with(this).load(state.user.profileImage).circleCrop()
                        .placeholder(R.drawable.ic_default_thumbnail)
                        .error(R.drawable.ic_default_thumbnail)
                        .into(player_user_small_icon)
            }
        }
    }

    private fun handleError(type: DetailsContract.ErrorType) {
        when (type) {
            DetailsContract.ErrorType.Like,
            DetailsContract.ErrorType.Dislike -> Snackbar.make(view!!, type.message, Snackbar.LENGTH_LONG).show()
            else -> {
                details.state = LazyLayout.ERROR
                details.errorText = getString(type.message)
            }
        }
    }

    private fun scrollToSelectedComment() {
        arguments?.getString(CommentKey, "")?.apply {
            if (isNotBlank()) {
                val commentIndex = commentsAdapter.indexOf(this)
                        .let { player_comments.getChildAt(it).y.toInt() }
                player_details.smoothScrollTo(0, commentIndex)
            }
        }
    }

    private fun displayVideoInfo(info: Video) {
        player_details.scrollTo(0, 0)
        info.apply {
            player_title.text = title
            player_subtitle.text = creator.name
            player_description.text = description
            player_releaseDate.text = getString(R.string.details_postDate, formatter.format(releaseDate))
            Linkify.addLinks(player_description, Linkify.WEB_URLS)
        }
        GlideApp.with(this).load(info.creator.user.profileImage).circleCrop()
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(player_small_icon)
    }

    private fun snack(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view!!, text, duration).show()
    }

    override fun comment(video: Video, comment: Comment?) {
        CommentDialogFragment.newInstance(comment, video).also { it.show(childFragmentManager, tag) }
    }

    override fun displayReplies(parent: Comment) {
        RepliesDialogFragment.newInstance(parent).also { it.show(childFragmentManager, tag) }
    }

    override fun onCommentSuccess() {
        Snackbar.make(view!!, R.string.details_commentPosted, Snackbar.LENGTH_LONG).show()
    }

    override fun onCommentError() {
        Snackbar.make(view!!, R.string.details_error_commentPost, Snackbar.LENGTH_LONG).show()
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