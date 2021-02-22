package me.mauricee.pontoon.repository.comment

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.data.local.comment.*
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.data.network.video.comment.*
import me.mauricee.pontoon.repository.PagedModel
import me.mauricee.pontoon.repository.user.toModel
import javax.inject.Inject

class CommentRepository @Inject constructor(private val commentDao: CommentDao,
                                            private val floatPlaneApi: FloatPlaneApi,
                                            private val pageListConfig: PagedList.Config,
                                            private val commentBoundaryCallbackFactory: CommentBoundaryCallback.Factory) {

    fun getComments(videoId: String): PagedModel<Comment> {
        val callback = commentBoundaryCallbackFactory.newInstance(videoId)
        val pagedList = RxPagedListBuilder(commentDao.getCommentsOfVideo(videoId).map { it.toModel() }, pageListConfig)
                .setFetchScheduler(Schedulers.io())
                .setNotifyScheduler(AndroidSchedulers.mainThread())
                .setBoundaryCallback(callback)
                .buildFlowable(BackpressureStrategy.LATEST)
        return PagedModel(pagedList, callback.pagingState.toFlowable(BackpressureStrategy.LATEST), callback::refresh)
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

internal fun ChildComment.toModel(): Comment {
    return Comment(
            entity.id,
            entity.editDate,
            entity.likes,
            entity.dislikes,
            entity.postDate,
            entity.text,
            entity.userInteraction,
            user.toModel(),
            emptyList()
    )
}

internal fun CommentUserReplyJoin.toModel(): Comment {
    return Comment(entity.id,
            entity.editDate,
            entity.likes,
            entity.dislikes,
            entity.postDate,
            entity.text,
            entity.userInteraction,
            user.toModel(),
            replies.map(ChildComment::toModel))
}

internal fun CommentInteraction.Type.toEntity() = when (this) {
    CommentInteraction.Type.Like -> CommentInteractionType.Like
    CommentInteraction.Type.Dislike -> CommentInteractionType.Dislike
}

internal fun CommentJson.toEntity(userInteraction: CommentInteraction.Type?): CommentEntity = CommentEntity(id, video, replying, user, editDate, interactionCounts.like, interactionCounts.dislike, postDate, text, userInteraction?.toEntity())

