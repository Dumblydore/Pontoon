package me.mauricee.pontoon.model.comment

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.common.BaseBoundaryCallback
import me.mauricee.pontoon.common.PagingState
import me.mauricee.pontoon.domain.floatplane.CommentJson
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.loge
import me.mauricee.pontoon.model.user.UserDao
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.user.toEntity
import javax.inject.Inject

class CommentBoundaryCallback private constructor(private val videoId: String,
                                                  private val api: FloatPlaneApi,
                                                  private val commentDao: CommentDao,
                                                  private val userDao: UserDao) : BaseBoundaryCallback<Comment>() {

    override fun clearItems(): Completable = commentDao.clearComments(videoId)

    override fun noItemsLoaded(): Observable<PagingState> = api.getVideoComments(videoId, 20, null)
            .flatMapObservable(this::processEntities)


    override fun frontItemLoaded(itemAtFront: Comment): Observable<PagingState> = Observable.just(PagingState.Fetched)

    override fun endItemLoaded(itemAtEnd: Comment): Observable<PagingState> = api.getVideoComments(videoId, 20, itemAtEnd.id)
            .flatMapObservable(this::processEntities)

    private fun processEntities(container: CommentJson.Container): Observable<PagingState> {
        val interactions = container.interactions.map { it.comment to it.type }.toMap()
        return Observable.fromIterable(container.comments)
                .flatMap { Observable.fromIterable(it.replies).startWith(it) }
                .map { it.toEntity(interactions[it.id]?.type) }
                .toList()
                .flatMap { flatComments ->
                    getUsers(flatComments)
                            .map { flatComments to it }
                }
                .map {
                    val (flatComments, users) = it
                    userDao.insert(users)
                    commentDao.insert(flatComments)
//                    if (commentDao.insert(flatComments).isEmpty()) PagingState.Completed
//                    else PagingState.Fetched
                    PagingState.Completed
                }.toObservable().switchIfEmpty(Observable.just(PagingState.Completed))
                .doOnError(::loge)
    }

    private fun getUsers(comments: List<CommentEntity>): Single<List<UserEntity>> = Observable.fromIterable(comments)
            .map { it.user }.distinct().toList().flatMap { users ->
                Observable.fromIterable(users.chunked(20))
                        .flatMapSingle { api.getUsers(*it.toTypedArray()) }
                        .map { it.users }.toList().map { it.flatten() }
            }.map { users ->
                users.mapNotNull { it.user?.toEntity() }
            }

    class Factory @Inject constructor(private val api: FloatPlaneApi, private val commentDao: CommentDao, private val userDao: UserDao) {
        fun newInstance(videoId: String) = CommentBoundaryCallback(videoId, api, commentDao, userDao)
    }
}