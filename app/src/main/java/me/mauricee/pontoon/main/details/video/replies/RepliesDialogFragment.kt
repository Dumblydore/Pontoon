package me.mauricee.pontoon.main.details.video.replies

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_replies.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.main.details.CommentAdapter
import me.mauricee.pontoon.main.details.video.DetailsContract
import me.mauricee.pontoon.model.comment.Comment
import java.lang.RuntimeException
import javax.inject.Inject

class RepliesDialogFragment : BottomSheetDialogFragment(), RepliesContract.View {

    private val subscriptions = CompositeDisposable()
    private val parent: String
        get() = arguments!!.getString(ParentKey, "")
    override val actions: Observable<RepliesContract.Action>
        get() = translateAdapterActions().startWith(RepliesContract.Action.Parent(parent))
    @Inject
    lateinit var adapter: CommentAdapter
    @Inject
    lateinit var presenter: RepliesPresenter

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_replies, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replies_list.adapter = adapter
        replies_list.layoutManager = LinearLayoutManager(requireContext())
        subscriptions += RxToolbar.navigationClicks(replies_header).subscribe { dismiss() }
        presenter.attachView(this)
    }

    override fun updateState(state: RepliesContract.State) = when (state) {
        RepliesContract.State.Loading -> replies_lazy.state = LazyLayout.LOADING
        is RepliesContract.State.Replies -> {
            replies_header.title = getString(R.string.replies_header, state.parent.user.username)
            adapter.submitList(state.comments)
            replies_lazy.state = LazyLayout.SUCCESS
        }
        is RepliesContract.State.Liked -> {
            adapter.updateComment(state.comment)
            Snackbar.make(view!!, getString(R.string.details_commentLiked), Snackbar.LENGTH_LONG).show()
        }
        is RepliesContract.State.Disliked -> {
            adapter.updateComment(state.comment)
            Snackbar.make(view!!, getString(R.string.details_commentDisliked), Snackbar.LENGTH_LONG).show()
        }
        is RepliesContract.State.Cleared -> {
            adapter.updateComment(state.comment)
        }
        is RepliesContract.State.CurrentUser -> {
        }
        is RepliesContract.State.Error -> handleError(state.type)
    }

    private fun handleError(type: RepliesContract.ErrorType) = when (type) {
        RepliesContract.ErrorType.Like,
        RepliesContract.ErrorType.Dislike,
        RepliesContract.ErrorType.Cleared -> Snackbar.make(view!!, type.msg, Snackbar.LENGTH_LONG).show()
        else -> {
            replies_lazy.errorText = getString(type.msg)
            replies_lazy.state = LazyLayout.ERROR
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.dispose()
        presenter.detachView()
    }

    private fun translateAdapterActions() = adapter.actions.map {
        when (it) {
            is DetailsContract.Action.ViewUser -> RepliesContract.Action.ViewUser(it.user)
            is DetailsContract.Action.Like -> RepliesContract.Action.Like(it.comment)
            is DetailsContract.Action.Dislike -> RepliesContract.Action.Dislike(it.comment)
            is DetailsContract.Action.Reply -> RepliesContract.Action.Reply(it.parent)
            else -> throw RuntimeException("Invalid action")
        }
    }

    companion object {
        private const val ParentKey: String = "PARENT"

        fun newInstance(comment: Comment) = RepliesDialogFragment().apply {
            arguments = bundleOf(ParentKey to comment.id)
        }
    }
}