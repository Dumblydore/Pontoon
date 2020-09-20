package me.mauricee.pontoon.main.details.comment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_comment.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

class CommentDialogFragment : BottomSheetDialogFragment(), CommentContract.View {

    private val subscriptions = CompositeDisposable()
    private val replyingId: String
        get() = arguments!!.getString(ReplyKey, NoReply)
    private val replyName: String
        get() = arguments!!.getString(NameKey, NoReply)
    private val videoId: String
        get() = arguments!!.getString(VideoKey, "")
    @Inject
    lateinit var presenter: CommentPresenter

    override val actions: Observable<CommentContract.Action>
        get() = comment_submit.clicks().map { comment_edit.text.toString() }.map<CommentContract.Action> {
            if (replyingId == NoReply) CommentContract.Action.Comment(it, videoId) else CommentContract.Action.Reply(it, replyingId, videoId)
        }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscriptions += comment_edit.textChanges().subscribe { comment_submit.isVisible = it.isNotBlank() }
        comment_reply.apply {
            isVisible = replyName != NoReply
            text = getString(R.string.reply_subhead, replyName)
        }
        presenter.attachView(this)
    }

    override fun updateState(state: CommentContract.State) {
        when (state) {
            is CommentContract.State.CurrentUser -> {
                GlideApp.with(this).load(state.user.entity.profileImage).circleCrop()
                        .placeholder(R.drawable.ic_default_thumbnail)
                        .error(R.drawable.ic_default_thumbnail)
                        .into(comment_user_icon_small)

                comment_header.text = getString(R.string.details_comment_header, state.user.entity.username)
                comment_edit.requestFocusFromTouch()
            }
            CommentContract.State.Close -> dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.clear()
        presenter.detachView()
    }

    companion object {
        private const val NameKey: String = "NAME"
        private const val ReplyKey: String = "REPLY"
        private const val VideoKey: String = "VIDEO"

        private const val NoReply = "NO_REPLY"

        //TODO
        fun newInstance(videoId: String, replyingTo: String? = null) = CommentDialogFragment().apply {
//            arguments = bundleOf(VideoKey to (replyingTo ?: NoReply),
//                    NameKey to (replyingTo?.user?.username ?: NoReply),
//                    VideoKey to video.id)
        }
    }
}