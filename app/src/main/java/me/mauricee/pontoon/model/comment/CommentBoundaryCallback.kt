package me.mauricee.pontoon.model.comment

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.domain.floatplane.Comment
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import javax.inject.Inject

class CommentBoundaryCallback constructor(private val videoId: String, private val commentDao: CommentDao, private val floatPlaneApi: FloatPlaneApi) : StateBoundaryCallback<CommentRepository.NewComment>(), Disposable {

    private val subs = CompositeDisposable()

    init {
        onZeroItemsLoaded()
    }

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        cache(floatPlaneApi.getVideoComments(videoId, Limit))
    }

    override fun onItemAtEndLoaded(itemAtEnd: CommentRepository.NewComment) {
        super.onItemAtEndLoaded(itemAtEnd)
        cache(floatPlaneApi.getVideoComments(videoId, Limit, itemAtEnd.id))
    }

    private fun cache(stream: Observable<Comment.Container>) {
        stateRelay.accept(State.Loading)
        subs += stream.flatMapSingle { container ->
            container.comments.toObservable().map {
                CommentEntity(it.id, it.video, it.replying
                        ?: it.video, it.user, it.editDate, it.interactionCounts.like, it.interactionCounts.dislike, it.postDate, it.text)
            }.toList()
        }.subscribe({
            commentDao.insert(*it.toTypedArray())
            val newState = when {
                it.size > Limit -> State.Finished
                else -> State.Fetched
            }
            stateRelay.accept(newState)
        }) { stateRelay.accept(State.Error) }
    }

    override fun isDisposed(): Boolean = subs.isDisposed

    override fun dispose() = subs.dispose()

    class Factory @Inject constructor(private val api: FloatPlaneApi, private val commentDao: CommentDao) {
        fun newInsance(videoId: String): CommentBoundaryCallback = CommentBoundaryCallback(videoId, commentDao, api)
    }

    companion object {
        private const val Limit = 20
    }
}