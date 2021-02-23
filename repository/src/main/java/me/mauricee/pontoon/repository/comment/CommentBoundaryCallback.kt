package me.mauricee.pontoon.repository.comment

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.data.local.comment.CommentDao
import me.mauricee.pontoon.data.local.comment.CommentEntity
import me.mauricee.pontoon.data.local.user.UserDao
import me.mauricee.pontoon.data.local.user.UserEntity
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.data.network.video.comment.CommentJson
import me.mauricee.pontoon.repository.user.toEntity
import me.mauricee.pontoon.repository.util.paging.BaseBoundaryCallback
import me.mauricee.pontoon.repository.util.paging.PagingState
import javax.inject.Inject

class CommentBoundaryCallback private constructor(private val videoId: String,
                                                  private val api: FloatPlaneApi,
                                                  private val commentDao: CommentDao,
                                                  private val userDao: UserDao) : BaseBoundaryCallback<Comment>() {

    override fun clearItems(): Completable = commentDao.clearComments(videoId)

    override fun noItemsLoaded(): Single<PagingState> = api.getVideoComments(videoId, 20, null)
            .flatMap(this::processEntities)


    override fun frontItemLoaded(itemAtFront: Comment): Single<PagingState> = Single.just(PagingState.Fetched)

    override fun endItemLoaded(itemAtEnd: Comment): Single<PagingState> = api.getVideoComments(videoId, 20, itemAtEnd.id)
            .flatMap(this::processEntities)

    private fun processEntities(container: CommentJson.Container): Single<PagingState> {
        val interactions = container.interactions.map { it.comment to it.type }.toMap()
        return Observable.fromIterable(container.comments)
                .flatMap { Observable.fromIterable(it.replies).startWith(it) }
                .map { it.toEntity(interactions[it.id]?.type) }
                .toList()
                .flatMap { flatComments -> getUsers(flatComments).map { flatComments to it } }
                .map {
                    val (flatComments, users) = it
                    userDao.insert(users)
                    commentDao.insert(flatComments)
                    PagingState.Completed
                }
    }

    private fun getUsers(comments: List<CommentEntity>): Single<List<UserEntity>> = Observable.fromIterable(comments)
            .map { it.user }.distinct().toList().flatMap { users ->
                Observable.fromIterable(users.chunked(20))
                        .flatMapSingle { api.getUsers(*it.toTypedArray()) }
                        .map { it.users }.toList().map { it.flatten() }
            }.map { users -> users.mapNotNull { it.user?.toEntity() } }

    class Factory @Inject constructor(private val api: FloatPlaneApi, private val commentDao: CommentDao, private val userDao: UserDao) {
        fun newInstance(videoId: String) = CommentBoundaryCallback(videoId, api, commentDao, userDao)
    }
}