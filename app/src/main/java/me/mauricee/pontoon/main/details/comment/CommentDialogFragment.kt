package me.mauricee.pontoon.main.details.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.fragment_comment.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.model.comment.Comment

class CommentDialogFragment : BottomSheetDialogFragment(), Disposable {

    private val subscriptions = CompositeDisposable()
    private val replyingTo: String
        get() = arguments!!.getString(NameKey, NoReply)
    private val submitRelay = PublishRelay.create<String>()
    val submit: Observable<String>
        get() = submitRelay


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscriptions += view.comment_submit.clicks().map { comment_edit.text.toString() }.subscribe(submitRelay::accept)
        subscriptions += comment_edit.textChanges().subscribe { comment_submit.isVisible = it.isNotBlank() }

//        GlideApp.with(this).load(profile).circleCrop()
//                .placeholder(R.drawable.ic_default_thumbnail)
//                .error(R.drawable.ic_default_thumbnail)
//                .into(comment_user_icon_small)
//
//        comment_header.text = getString(R.string.details_comment_header, name)
//        comment_edit.requestFocusFromTouch()
    }

    override fun isDisposed(): Boolean = subscriptions.isDisposed

    override fun dispose() = subscriptions.dispose()

    companion object {
        private const val NameKey: String = "NAME"
        private const val ProfileKey: String = "PROFILE_IMAGE"

        private const val NoReply = "NO_REPLY"

        fun newInstance(replyingTo: Comment? = null) = CommentDialogFragment().apply {
            arguments = Bundle().apply { putString(NameKey, replyingTo?.user?.username ?: NoReply) }
        }
    }
}