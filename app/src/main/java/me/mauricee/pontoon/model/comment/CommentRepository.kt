package me.mauricee.pontoon.model.comment

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.ext.loge
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

private typealias CommentPojo = me.mauricee.pontoon.domain.floatplane.Comment

class CommentRepository @Inject constructor(private val commentDao: CommentDao,
                                            private val floatPlaneApi: FloatPlaneApi,
                                            private val userRepository: UserRepository) {

    fun getComments(videoId: String): Observable<List<Comment>> =
            Single.concat(getCachedComments(videoId), getApiComments(videoId))
                    .toObservable().compose(RxHelpers.applyObservableSchedulers())
                    .filter(List<Comment>::isNotEmpty)

    private fun getCachedComments(videoId: String): Single<List<Comment>> =
            commentDao.getCommentByParent(videoId).flatMapObservable {
                val users = it.map { it.user }.distinct().let { userRepository.getUsers(*it.toTypedArray()) }
                        .flatMap { it.toObservable() }.cache()
                val replies = it.toObservable().flatMapSingle { getCachedComments(it.id) }
                it.toObservable().flatMap { comment ->
                    users.filter { it.id == comment.user }.zipWith<List<Comment>, Comment>(replies,
                            BiFunction { t1, t2 ->
                                Comment(comment.id, comment.text, comment.parent, comment.video, comment.likes, comment.dislikes, t2, t1)
                            })
                }
            }.toList().onErrorReturnItem(emptyList())

    private fun getApiComments(videoId: String): Single<List<Comment>> = floatPlaneApi.getVideoComments(videoId)
            .map { it.comments }.flatMapSingle {
                val commentOb = it.toObservable().flatMap { it.replies.toObservable().startWith(it) }
                        .doOnNext(::cacheComment)
                        .cache()
                commentOb.map { it.user }.distinct().toList().map { it.toTypedArray() }
                        .flatMap { userRepository.getUsers(*it).lastOrError() }
                        .zipWith<List<CommentPojo>, List<Comment>>(commentOb.toList(), BiFunction { users, comments ->
                            fun createComment(comment: CommentPojo, parent: String = videoId): Comment {
                                val user = users.first { it.id == comment.user }
                                val replies = comments.filter { it.replying == comment.id }.map { createComment(it, it.replying ?: videoId) }
                                return Comment(comment.id, parent, comment.video, comment.text, 0, 0, replies, user)
                            }
                            comments.map { createComment(it) }
                        })
            }.single(emptyList())

    private fun cacheComment(comment: CommentPojo) {
        Completable.fromCallable {
            CommentEntity(comment.id, comment.video, comment.replying
                    ?: "", comment.user, comment.editDate,
                    0, 0, comment.postDate, comment.text)
                    .also { commentDao.insert(it) }
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .doOnError { loge("Error caching comments", it) }
                .onErrorComplete()
                .subscribe()

    }
}

