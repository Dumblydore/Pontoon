package me.mauricee.pontoon.main.details.comment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_replies.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.main.details.CommentAdapter
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.comment.CommentRepository
import javax.inject.Inject

class RepliesDialogFragment : BottomSheetDialogFragment(), Disposable {

    private val subscriptions = CompositeDisposable()
    private val name: String
        get() = arguments!!.getString(NameKey, "")
    private val parent: String
        get() = arguments!!.getString(ParentKey, "")
    val replyTo
        get() = adapter.replyTo
    val actions
        get() = adapter.actions

    @Inject
    lateinit var adapter: CommentAdapter
    @Inject
    lateinit var commentRepository: CommentRepository

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_replies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replies_list.layoutManager = LinearLayoutManager(requireContext())
        replies_list.adapter = adapter
        replies_header.title = "$name's replies"
        subscriptions += RxToolbar.navigationClicks(replies_header).subscribe { dismiss() }
        subscriptions += commentRepository.getReplies(parent).subscribe({ it ->
            adapter.comments = it.toMutableList()
            replies_lazy.state = LazyLayout.SUCCESS
        }, { replies_lazy.state = LazyLayout.ERROR })
        replies_lazy.state = LazyLayout.LOADING
    }

    override fun isDisposed(): Boolean = subscriptions.isDisposed

    override fun dispose() = subscriptions.dispose()

    companion object {
        private const val NameKey: String = "NAME"
        private const val ParentKey: String = "PARENT"

        fun newInstance(comment: Comment) = RepliesDialogFragment().apply {
            arguments = Bundle().apply { putString(NameKey, comment.user.username); putString(ParentKey, comment.id) }
        }
    }
}