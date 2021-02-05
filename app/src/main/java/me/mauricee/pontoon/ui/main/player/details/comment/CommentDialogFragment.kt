package me.mauricee.pontoon.ui.main.player.details.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentCommentBinding
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.ui.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CommentDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var presenterFactory: CommentPresenter.Factory

    @Inject
    lateinit var viewModelFactory: CommentContract.ViewModel.Factory

    private val subscriptions = CompositeDisposable()

    private val args by navArgs<CommentDialogFragmentArgs>()
    private val viewModel: CommentContract.ViewModel by assistedViewModel {
        viewModelFactory.create(presenterFactory.create(CommentContract.Args(args.videoId, args.commentId)))
    }
    private val binding by viewBinding(FragmentCommentBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscriptions += binding.commentSubmit.clicks().map { binding.commentEdit.text.toString() }
                .map(CommentContract.Action::Comment)
                .subscribe(viewModel::sendAction)
        subscriptions += binding.commentEdit.textChanges()
                .map { it.toString().isEmpty() }
                .subscribe { binding.commentSubmit.isGone = it }

        viewModel.state.mapDistinct { it.user?.entity }.notNull().observe(viewLifecycleOwner) {
            GlideApp.with(this).load(it.profileImage).circleCrop()
                    .placeholder(R.drawable.ic_default_thumbnail)
                    .error(R.drawable.ic_default_thumbnail)
                    .into(binding.commentUserIconSmall)
            binding.commentEdit.requestFocusFromTouch()
            binding.commentHeader.text = getString(R.string.details_comment_header, it.username)
            binding.commentHeader.doOnPreDraw { startPostponedEnterTransition() }
        }
        viewModel.state.mapDistinct(CommentContract.State::replyingTo).observe(viewLifecycleOwner) {
            binding.commentReply.isGone = it.isNullOrEmpty()
            binding.commentReply.text = getString(R.string.reply_subhead, it)
        }

    }

}