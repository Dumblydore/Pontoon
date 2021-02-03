package me.mauricee.pontoon.ui.main.player.details

import android.os.Bundle
import android.text.format.DateUtils
import android.text.util.Linkify
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.transition.TransitionInflater
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleBindingAdapter
import me.mauricee.pontoon.databinding.FragmentDetailsBinding
import me.mauricee.pontoon.databinding.ItemDetailsPostCommentBinding
import me.mauricee.pontoon.databinding.ItemDetailsVideoBinding
import me.mauricee.pontoon.databinding.ItemVideoListBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.playback.NewPlayer
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.main.MainContract
import me.mauricee.pontoon.ui.main.player.PlayerAction
import me.mauricee.pontoon.ui.main.player.PlayerEvent
import me.mauricee.pontoon.ui.main.player.PlayerState
import me.mauricee.pontoon.ui.main.player.PlayerViewModel
import me.mauricee.pontoon.ui.main.player.details.comment.CommentDialogFragment
import me.mauricee.pontoon.ui.main.player.details.replies.RepliesDialogFragment
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragment : NewBaseFragment(R.layout.fragment_details) {

    @Inject
    lateinit var commentsAdapter: CommentAdapter

    @Inject
    lateinit var formatter: DateTimeFormatter

    private val viewModel: PlayerViewModel by viewModels({ requireActivity() })

    private val detailsAdapter = SimpleBindingAdapter(ItemDetailsVideoBinding::inflate, ::displayVideoInfo)
    private val relatedVideosAdapter = SimpleBindingAdapter(ItemVideoListBinding::inflate, ::bindRelatedVideo)
    private val postCommentAdapter = SimpleBindingAdapter(ItemDetailsPostCommentBinding::inflate, ::displayCurrentUser)
    private val qualityAdapter = SimpleBindingAdapter(ItemDetailsVideoBinding::inflate, ::displayQuality)

    private val contentAdapter by lazy { ConcatAdapter(qualityAdapter, detailsAdapter, relatedVideosAdapter, postCommentAdapter, commentsAdapter) }
    private val binding by viewBinding(FragmentDetailsBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        subscriptions += commentsAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detailsContent.adapter = contentAdapter

        subscriptions += qualityAdapter.clicks.map { PlayerAction.SetQuality(it.model) }
                .subscribe(viewModel::sendAction)
        subscriptions += relatedVideosAdapter.clicks.map { PlayerAction.PlayVideo(it.model.id) }
                .subscribe(viewModel::sendAction)
        subscriptions += postCommentAdapter.clicks.subscribe { viewModel.sendAction(PlayerAction.PostComment) }
        subscriptions += commentsAdapter.actions.subscribe(viewModel::sendAction)
        subscriptions += detailsAdapter.clicks.subscribe {
            it.view.apply {
                playerDescription.isVisible = !playerDescription.isVisible
                playerDescriptionDivider.isVisible = playerDescription.isVisible
                playerReleaseDate.isVisible = playerDescription.isVisible
            }
        }

        viewModel.events.observe(viewLifecycleOwner, ::handleEvents)

        viewModel.state.mapDistinct { it.qualityLevels.toList() }.observe(viewLifecycleOwner, qualityAdapter::submitList)
        viewModel.state.map { it.commentState.isLoading() }.observe(viewLifecycleOwner) {
            binding.detailsProgress.isVisible = it
        }
        viewModel.state.mapDistinct(PlayerState::user).notNull().map(::listOf).observe(viewLifecycleOwner, postCommentAdapter::submitList)
        viewModel.state.mapDistinct(PlayerState::video).notNull().map(::listOf).observe(viewLifecycleOwner, detailsAdapter::submitList)
        viewModel.state.mapDistinct(PlayerState::relatedVideos).observe(viewLifecycleOwner, relatedVideosAdapter::submitList)
        viewModel.state.mapDistinct(PlayerState::comments).observe(viewLifecycleOwner) {
            commentsAdapter.submitList(it) { scrollToSelectedComment() }
        }
    }

    private fun scrollToSelectedComment() {
        arguments?.getString(CommentKey, "")?.apply {
            if (isNotBlank()) {
                binding.detailsContent.smoothScrollToPosition(commentsAdapter.indexOf(this))
            }
        }
    }

    private fun displayCurrentUser(view: ItemDetailsPostCommentBinding, user: UserEntity) {
        GlideApp.with(this).load(user.profileImage).circleCrop()
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(view.playerUserSmallIcon)
    }

    private fun displayVideoInfo(view: ItemDetailsVideoBinding, info: Video) {
        binding.detailsContent.scrollToPosition(0)
        info.entity.apply {
            view.playerTitle.text = title
            view.playerSubtitle.text = info.creator.entity.name
            view.playerDescription.text = description
            view.playerReleaseDate.text = getString(R.string.details_postDate, DateUtils.getRelativeTimeSpanString(releaseDate.toEpochMilli()))
            Linkify.addLinks(view.playerDescription, Linkify.WEB_URLS)
        }
        GlideApp.with(this).load(info.creator.user.profileImage).circleCrop()
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(view.playerSmallIcon)
    }

    private fun bindRelatedVideo(itemView: ItemVideoListBinding, video: Video) {
        itemView.itemTitle.text = video.entity.title
        itemView.itemDescription.text = video.creator.entity.name
        GlideApp.with(itemView.root).load(video.entity.thumbnail)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(itemView.itemIconBig)
    }

    private fun displayQuality(itemBinding: ItemDetailsVideoBinding, item: NewPlayer.Quality) {
        itemBinding.playerTitle.text = item.label
    }

    private fun handleEvents(playerEvent: PlayerEvent) {
        when (playerEvent) {
            is PlayerEvent.PostComment -> CommentDialogFragment.newInstance(playerEvent.videoId, playerEvent.comment).also { it.show(parentFragmentManager, tag) }
            is PlayerEvent.DisplayReplies -> RepliesDialogFragment.newInstance(playerEvent.commentId).show(parentFragmentManager, tag)
            PlayerEvent.OnCommentSuccess -> Snackbar.make(requireView(), R.string.details_commentPosted, Snackbar.LENGTH_LONG).show()
            PlayerEvent.OnCommentError -> Snackbar.make(requireView(), R.string.details_error_commentPost, Snackbar.LENGTH_LONG).show()
            is PlayerEvent.DisplayUser -> {
            }//navigator.toUser(playerEvent.user.id)
        }
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