package me.mauricee.pontoon.data.local.comment

import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.data.local.BaseDao
import me.mauricee.pontoon.data.local.user.UserEntity
import org.threeten.bp.Instant

@Entity(tableName = "Comments")
data class CommentEntity(@PrimaryKey val id: String,
                         val video: String,
                         val parent: String?,
                         val user: String,
                         val editDate: Instant,
                         val likes: Int,
                         val dislikes: Int,
                         val postDate: Instant,
                         val text: String,
                         val userInteraction: CommentInteractionType?)

enum class CommentInteractionType {
    Like,
    Dislike
}

data class CommentUserReplyJoin(@Embedded val entity: CommentEntity,
                                @Relation(parentColumn = "user", entityColumn = "id") val user: UserEntity,
                                @Relation(parentColumn = "id", entityColumn = "parent", entity = CommentEntity::class) val replies: List<ChildComment>)

data class ChildComment(@Embedded val entity: CommentEntity,
                        @Relation(parentColumn = "user", entityColumn = "id") val user: UserEntity)

@Dao
abstract class CommentDao : BaseDao<CommentEntity>() {
    @Query("SELECT * FROM Comments WHERE video IS :videoId AND parent IS NULL")
    abstract fun getCommentsOfVideo(videoId: String): DataSource.Factory<Int, CommentUserReplyJoin>

    @Query("SELECT * FROM Comments WHERE parent IS :commentId")
    abstract fun getCommentsOfParent(commentId: String): Observable<List<ChildComment>>

    @Query("SELECT * FROM Comments Where id IS :commentId")
    abstract fun getComment(commentId: String): Observable<CommentUserReplyJoin>

    @Query("UPDATE Comments SET userInteraction=:interaction WHERE Comments.id=:commentId")
    abstract fun setComment(commentId: String, interaction: CommentInteractionType?): Completable

    @Query("DELETE FROM Comments WHERE video=:videoId")
    abstract fun clearComments(videoId: String): Completable

    @Query("DELETE FROM Comments")
    abstract fun clearAllComments(): Completable
}
