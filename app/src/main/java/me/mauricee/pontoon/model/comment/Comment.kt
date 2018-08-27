package me.mauricee.pontoon.model.comment

import androidx.annotation.Keep
import androidx.room.*
import io.reactivex.Single
import me.mauricee.pontoon.model.user.UserRepository
import org.threeten.bp.Instant

@Entity(tableName = "Comment")
data class CommentEntity(
        @PrimaryKey val id: String,
        val video: String,
        val parent: String,
        val user: String,
        val editDate: Instant,
        val likes: Int,
        val dislikes: Int,
        val postDate: Instant,
        val text: String)

//TODO store user interactions
@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg creatorEntity: CommentEntity)

    @Update
    fun update(vararg creatorEntity: CommentEntity)

    @Query("SELECT * FROM Comment WHERE id IN (:userIds)")
    fun getCommentByParent(vararg userIds: String): Single<List<CommentEntity>>

}

@Keep
class Comment(val id: String, val parent: String, val video: String, val text: String,
              val editDate: Instant, val postDate: Instant,
              val likes: Int, val dislikes: Int, val replies: List<Comment>,
              val user: UserRepository.User, val userInteraction: List<Interaction> = emptyList()) {

    fun like(): Comment = Comment(id, parent, video, text, editDate, postDate, likes + 1, dislikes, replies, user, mutableListOf(Interaction.Like, *userInteraction.toTypedArray()))
    fun dislike(): Comment = Comment(id, parent, video, text, editDate, postDate, likes, dislikes + 1, replies, user, mutableListOf(Interaction.Dislike, *userInteraction.toTypedArray()))

    fun toEntity(): CommentEntity = CommentEntity(id, video, parent, user.username, editDate, likes, dislikes, postDate, text)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comment

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    enum class Interaction {
        Like,
        Dislike
    }
}