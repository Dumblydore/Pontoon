package me.mauricee.pontoon.ui.main.player.details.replies

import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleBindingAdapter
import me.mauricee.pontoon.data.local.comment.CommentInteractionType
import me.mauricee.pontoon.databinding.FragmentRepliesBinding
import me.mauricee.pontoon.databinding.ItemCommentBinding
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.repository.comment.Comment
import me.mauricee.pontoon.ui.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RepliesDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var presenterFactory: RepliesPresenter.Factory

    @Inject
    lateinit var viewModelFactory: RepliesContract.ViewModel.Factory

    private val commentAdapter = SimpleBindingAdapter(::createCommentItem, ::bindComment)

    private val primaryColor by lazy { ContextCompat.getColor(requireContext(), R.color.md_grey_600) }
    private val positiveColor by lazy { ContextCompat.getColor(requireContext(), R.color.colorPositive) }
    private val negativeColor by lazy { ContextCompat.getColor(requireContext(), R.color.colorNegative) }

    private val args by navArgs<RepliesDialogFragmentArgs>()
    private val viewModel by assistedViewModel {
        viewModelFactory.create(presenterFactory.create(RepliesContract.Args(args.commentId)))
    }
    private val binding by viewBinding(FragmentRepliesBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_replies, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.repliesList.adapter = commentAdapter

        viewModel.state.mapDistinct { it.uiState.lazyState() }.observe(viewLifecycleOwner) {
            binding.repliesLazy.state = it
        }
        viewModel.state.mapDistinct(RepliesContract.State::parentComment).notNull().observe(viewLifecycleOwner, ::displayParentComment)
        viewModel.state.mapDistinct(RepliesContract.State::comments).observe(viewLifecycleOwner, commentAdapter::submitList)
    }

    private fun displayParentComment(comment: Comment) {
        binding.repliesHeaderText.text = getString(R.string.replies_header, comment.user.username)
    }


    private fun bindComment(it: ItemCommentBinding, comment: Comment) {
        val commentScore = comment.score
        it.itemTitle.text = comment.user.username
        it.itemComment.text = comment.text
        LinkifyCompat.addLinks(it.itemComment, Linkify.WEB_URLS)
        it.itemThumbText.isVisible = commentScore != 0
        it.itemThumbText.text = "${if (commentScore > 0) "+" else ""} $commentScore"
        it.itemViewReplies.isGone = true

        val (likeTint, dislikeTint) = when (comment.userInteraction) {
            CommentInteractionType.Like -> positiveColor to primaryColor
            CommentInteractionType.Dislike -> primaryColor to negativeColor
            else -> primaryColor to primaryColor
        }

        DrawableCompat.setTint(it.itemThumbUp.icon.mutate(), likeTint)
        DrawableCompat.setTint(it.itemThumbDown.icon.mutate(), dislikeTint)

        GlideApp.with(it.root).load(comment.user.profileImage)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(it.itemIconSmall)
    }

    private fun createCommentItem(inflater: LayoutInflater, group: ViewGroup?, root: Boolean): ItemCommentBinding {
        return ItemCommentBinding.inflate(inflater, group, root)
    }
}