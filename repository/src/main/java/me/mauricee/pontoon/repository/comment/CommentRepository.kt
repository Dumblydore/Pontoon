package me.mauricee.pontoon.repository.comment

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.data.local.comment.CommentUserReplyJoin
import me.mauricee.pontoon.data.local.comment.CommentDao
import me.mauricee.pontoon.data.local.comment.CommentInteractionType
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.data.network.video.comment.ClearInteraction
import me.mauricee.pontoon.data.network.video.comment.CommentInteraction
import me.mauricee.pontoon.data.network.video.comment.CommentPost
import me.mauricee.pontoon.data.network.video.comment.Reply
import me.mauricee.pontoon.repository.PagedModel
import javax.inject.Inject

class CommentRepository @Inject constructor(private val commentDao: CommentDao,
                                            private val floatPlaneApi: FloatPlaneApi,
                                            private val pageListConfig: PagedList.Config,
                                            private val commentBoundaryCallbackFactory: CommentBoundaryCallback.Factory) {

    fun getComments(videoId: String): PagedModel<Comment> {
        val callback = commentBoundaryCallbackFactory.newInstance(videoId)
        val pagedList = RxPagedListBuilder(commentDao.getCommentsOfVideo(videoId), pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildObservable()
        return PagedModel(pagedList, callback.pagingState, callback::refresh)
    }

    fun getComment(commentId: String): Observable<CommentUserReplyJoin> = commentDao.getComment(commentId)

    fun postComment(message: String, postId: String, parentCommentId: String?) = if (parentCommentId.isNullOrEmpty())
        post(message, postId)
    else
        reply(message, postId, parentCommentId)

    private fun post(message: String, postId: String): Completable = floatPlaneApi.post(CommentPost(message, postId)).flatMapCompletable {
        Completable.fromAction { commentDao.insert(it.toEntity(null)) }
    }

    private fun reply(message: String, postId: String, parentCommentId: String): Completable = floatPlaneApi.post(Reply(parentCommentId, message, postId)).flatMapCompletable {
        Completable.fromAction { commentDao.insert(it.toEntity(null)) }
    }

    fun like(commentId: String): Completable = interact(commentId, CommentInteractionType.Like)

    fun dislike(commentId: String): Completable = interact(commentId, CommentInteractionType.Dislike)

    fun clear(commentId: String): Completable = interact(commentId, null)

    private fun interact(commentId: String, interaction: CommentInteractionType?): Completable = getComment(commentId).firstElement().flatMapCompletable { comment ->
        val interactionJson = when (interaction) {
            CommentInteractionType.Like -> CommentInteraction.Type.Like
            CommentInteractionType.Dislike -> CommentInteraction.Type.Dislike
            null -> null
        }
        if (interaction == null || comment.entity.userInteraction == interaction)
            floatPlaneApi.clearInteraction(ClearInteraction(commentId)).andThen(commentDao.setComment(commentId, null))
        else floatPlaneApi.setComment(CommentInteraction(commentId, interactionJson!!)).andThen(commentDao.setComment(commentId, interaction))
    }
}

