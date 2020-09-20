package me.mauricee.pontoon.model.comment

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.*
import me.mauricee.pontoon.model.PagedResult
import javax.inject.Inject

class CommentRepository @Inject constructor(private val commentDao: CommentDao,
                                            private val floatPlaneApi: FloatPlaneApi,
                                            private val pageListConfig: PagedList.Config,
                                            private val commentBoundaryCallbackFactory: CommentBoundaryCallback.Factory) {

    fun getComments(videoId: String): PagedResult<Comment> {
        val callback = commentBoundaryCallbackFactory.newInstance(videoId)
        val pagedList = RxPagedListBuilder(commentDao.getCommentsOfVideo(videoId), pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildObservable()
        return PagedResult(pagedList, callback.pagingState, callback::refresh)
    }

    fun getComment(commentId: String): Observable<Comment> = commentDao.getComment(commentId)

    fun postComment(message: String, postId: String): Completable = floatPlaneApi.post(CommentPost(message, postId)).flatMapCompletable {
        Completable.fromAction { commentDao.insert(it.toEntity(null)) }
    }

    fun postComment(message: String, postId: String, parentCommentId: String): Completable = floatPlaneApi.post(Reply(parentCommentId, message, postId)).flatMapCompletable {
        Completable.fromAction { commentDao.insert(it.toEntity(null)) }
    }

    fun interact(commentId: String, interaction: CommentInteraction.Type?): Completable = getComment(commentId).firstElement().flatMapCompletable { comment ->
        if (interaction == null || comment.entity.userInteraction == interaction)
            floatPlaneApi.clearInteraction(ClearInteraction(commentId)).andThen(commentDao.setComment(commentId, null))
        else floatPlaneApi.setComment(CommentInteraction(commentId, interaction)).andThen(commentDao.setComment(commentId, interaction))
    }
}

