package me.mauricee.pontoon.model.comment

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.*
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.ext.doOnIo
import me.mauricee.pontoon.ext.ioStream
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

private typealias CommentPojo = me.mauricee.pontoon.domain.floatplane.Comment

//TODO this is shit, rewrite this.
class CommentRepository @Inject constructor(private val commentDao: CommentDao,
                                            private val floatPlaneApi: FloatPlaneApi,
                                            private val callback: CommentBoundaryCallback.Factory,
                                            private val userRepository: UserRepository,
                                            private val pageListConfig: PagedList.Config) {

    data class CommentResult(val comments: Observable<PagedList<NewComment>>, val state: Observable<StateBoundaryCallback.State>)

    private val currentUser
        get() = userRepository.activeUser

    fun getComments(videoId: String): Single<CommentResult> = Single.fromCallable {
        val callback = callback.newInsance(videoId)
        val comments = RxPagedListBuilder(commentDao.getCommentsOfVideo(videoId).map { comment ->
            val user = userRepository.getUsers(comment.user).blockingFirst().first()
            NewComment(comment, user)
        }, pageListConfig)
                .setBoundaryCallback(callback)
                .buildObservable()
                .doOnTerminate { callback.dispose() }
                .doOnDispose { callback.dispose() }
        CommentResult(comments, callback.state)
    }.compose(RxHelpers.applySingleSchedulers())

    fun getComment(commentId: String): Observable<Comment> = commentDao.getComment(commentId).flatMapObservable { comment ->
        userRepository.getUsers(comment.user).flatMap { it.toObservable() }.map { user ->
            Comment(comment.id, comment.text, comment.parent, comment.video, comment.editDate, comment.postDate, comment.likes, comment.dislikes, emptyList(), user)
        }
    }.compose(RxHelpers.applyObservableSchedulers())

    fun getReplies(commentId: String): Single<List<Comment>> =
            commentDao.getCommentsOfParent(commentId).flatMapObservable {
                val users = it.map { it.user }.distinct().let { userRepository.getUsers(*it.toTypedArray()) }
                        .flatMap { it.toObservable() }.cache()
                val replies = it.toObservable().flatMapSingle { commentDao.getCommentsOfParent(it.id).compose(daoToModel()) }
                it.toObservable().flatMap { comment ->
                    users.filter { it.id == comment.user }.zipWith<List<Comment>, Comment>(replies,
                            BiFunction { t1, t2 ->
                                Comment(comment.id, commentId, comment.video, comment.text, comment.editDate, comment.postDate, comment.likes, comment.dislikes, t2, t1)
                            })
                }
            }.toList().onErrorReturnItem(emptyList()).ioStream()


    fun like(comment: NewComment): Observable<NewComment> = Observable.empty() // interactWithComment(comment, CommentInteraction.Type.Like)

    fun dislike(comment: NewComment): Observable<NewComment> = Observable.empty() // interactWithComment(comment, CommentInteraction.Type.Dislike)

    fun clear(comment: NewComment): Observable<NewComment> = Observable.empty() // floatPlaneApi.clearInteraction(ClearInteraction(comment.id))
//            .map { comment.clear() }

    fun comment(text: String, video: String): Observable<Comment> =
            floatPlaneApi.post(CommentPost(text, video))
                    .compose(RxHelpers.applyObservableSchedulers())
                    .doOnNext { cacheComment(it) }
                    .map {
                        Comment(it.id, it.replying
                                ?: it.video, video, it.text, it.editDate, it.postDate, it.interactionCounts.like,
                                it.interactionCounts.dislike, emptyList(), currentUser)
                    }

    fun comment(text: String, parentId: String, videoId: String): Observable<Comment> = floatPlaneApi.post(Reply(parentId, text, videoId))
            .compose(RxHelpers.applyObservableSchedulers())
            .doOnNext { cacheComment(it) }
            .map {
                Comment(it.id, it.replying
                        ?: it.video, videoId, it.text, it.editDate, it.postDate, it.interactionCounts.like,
                        it.interactionCounts.dislike, emptyList(), currentUser)
            }

//    fun interactWithComment(comment: Comment, type: CommentInteraction.Type): Observable<Comment> =
//            if (commentHasDuplicateInteractions(comment, type)) clear(comment)
//            else floatPlaneApi.setComment(CommentInteraction(comment.id, type)).map {
//                (if (type == CommentInteraction.Type.Like) comment.like() else comment.dislike())
//                        .apply { commentDao.update(comment.toEntity()) }
//            }

    private fun commentHasDuplicateInteractions(comment: Comment, type: CommentInteraction.Type): Boolean =
            (comment.userInteraction.contains(Comment.Interaction.Like) && type == CommentInteraction.Type.Like) ||
                    (comment.userInteraction.contains(Comment.Interaction.Dislike) && type == CommentInteraction.Type.Dislike)


    private fun getApiComments(videoId: String): Single<List<Comment>> = floatPlaneApi.getVideoComments(videoId)
            .flatMapSingle {
                val commentOb = it.comments.toObservable().flatMap { it.replies.toObservable().startWith(it) }
                        .doOnNext(::cacheComment)
                        .cache()
                commentOb.map { it.user }.distinct().toList().map { it.toTypedArray() }
                        .flatMap { it2 -> userRepository.getUsers(*it2).lastOrError() }
                        .zipWith<List<CommentPojo>, List<Comment>>(commentOb.toList(), BiFunction { users, comments ->
                            fun createComment(comment: CommentPojo, parent: String = videoId): Comment {
                                val user = users.first { it.id == comment.user }
                                val replies = comments.filter { it.replying == comment.id }.map {
                                    createComment(it, it.replying ?: videoId)
                                }
                                val interactions = it.interactions.filter { it.comment == comment.id }.map {
                                    when (it.type.type) {
                                        CommentInteraction.Type.Like -> Comment.Interaction.Like
                                        CommentInteraction.Type.Dislike -> Comment.Interaction.Dislike
                                    }
                                }
                                return Comment(comment.id, parent, comment.video, comment.text, comment.editDate, comment.postDate, comment.interactionCounts.like, comment.interactionCounts.dislike, replies, user, interactions)
                            }
                            comments.map { createComment(it) }
                        })
            }.single(emptyList())

    private fun daoToModel(): SingleTransformer<List<CommentEntity>, List<Comment>> = SingleTransformer {
        it.flatMapObservable {
            val users = it.map { it.user }.distinct().let { userRepository.getUsers(*it.toTypedArray()) }
                    .flatMap { it.toObservable() }.cache()
            val replies = it.toObservable().flatMapSingle { child -> commentDao.getCommentsOfParent(child.id).compose(daoToModel()) }
            it.toObservable().flatMap { comment ->
                users.filter { it.id == comment.user }.zipWith<List<Comment>, Comment>(replies,
                        BiFunction { t1, t2 ->
                            Comment(comment.id, comment.text, comment.parent, comment.video, comment.editDate, comment.postDate, comment.likes, comment.dislikes, t2, t1)
                        })
            }
        }.toList().onErrorReturnItem(emptyList())
    }

    private fun cacheComment(comment: CommentPojo) {
        CommentEntity(comment.id, comment.video, comment.replying
                ?: "", comment.user, comment.editDate, 0, 0, comment.postDate, comment.text)
                .toObservable().flatMapCompletable { Completable.fromAction { commentDao.insert(it) } }
                .doOnIo().onErrorComplete()
                .subscribe()
    }
    data class NewComment(val id: String, val parent: String, val text: String, val replies: Int, val score: Int, val user: UserRepository.User, val userInteraction: CommentInteraction.Type?) {
        constructor(commentEntity: CommentEntity, user: UserRepository.User) : this(commentEntity.id, commentEntity.parent, commentEntity.text, 0, commentEntity.likes - commentEntity.dislikes, user, null)

        companion object {
            val ItemCallback = object : DiffUtil.ItemCallback<NewComment>() {
                override fun areItemsTheSame(oldItem: NewComment, newItem: NewComment): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: NewComment, newItem: NewComment): Boolean = newItem == oldItem
            }
        }
    }

}

