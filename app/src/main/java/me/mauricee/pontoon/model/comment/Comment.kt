package me.mauricee.pontoon.model.comment

import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.CommentInteraction
import me.mauricee.pontoon.domain.floatplane.CommentJson
import me.mauricee.pontoon.model.BaseDao
import me.mauricee.pontoon.model.Diffable
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.video.Video
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
                         val userInteraction: CommentInteraction.Type?) {
    @Ignore
    val score: Int = likes - dislikes
}

fun CommentJson.toEntity(userInteraction: CommentInteraction.Type?): CommentEntity = CommentEntity(id, video, replying, user, editDate, interactionCounts.like, interactionCounts.dislike, postDate, text, userInteraction)

data class Comment(@Embedded val entity: CommentEntity,
                   @Relation(parentColumn = "user", entityColumn = "id") val user: UserEntity,
                   @Relation(parentColumn = "id", entityColumn = "parent", entity = CommentEntity::class) val replies: List<ChildComment>) : Diffable<String> {
    @Ignore
    override val id: String = entity.id
}

data class ChildComment(@Embedded val entity: CommentEntity,
                        @Relation(parentColumn = "user", entityColumn = "id") val user: UserEntity) : Diffable<String> {
    @Ignore
    override val id: String = entity.id
}

@Dao
abstract class CommentDao : BaseDao<CommentEntity>() {
    @Query("SELECT * FROM Comments WHERE video IS :videoId AND parent IS NULL")
    abstract fun getCommentsOfVideo(videoId: String): DataSource.Factory<Int, Comment>

    @Query("SELECT * FROM Comments WHERE parent IS :commentId")
    abstract fun getCommentsOfParent(commentId: String): Observable<List<ChildComment>>

    @Query("SELECT * FROM Comments Where id IS :commentId")
    abstract fun getComment(commentId: String): Observable<Comment>

    @Query("UPDATE Comments SET userInteraction=:interaction WHERE Comments.id=:commentId")
    abstract fun setComment(commentId: String, interaction: CommentInteraction.Type?): Completable

    @Query("DELETE FROM Comments WHERE video=:videoId")
    abstract fun clearComments(videoId: String): Completable
}
