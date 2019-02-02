package me.mauricee.pontoon.model.comment

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
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

    @Query("SELECT * FROM Comment WHERE video IS :videoId AND parent IS :videoId")
    fun getCommentsOfVideo(videoId: String): Single<List<CommentEntity>>

    @Query("SELECT * FROM Comment WHERE parent IS :commentId")
    fun getCommentsOfParent(commentId: String): Single<List<CommentEntity>>

    @Query("SELECT * FROM Comment Where id IS :commentId")
    fun getComment(commentId: String): Single<CommentEntity>

}

@Keep
class Comment(val id: String, val parent: String, val video: String, val text: String,
              val editDate: Instant, val postDate: Instant,
              val likes: Int, val dislikes: Int, val replies: List<Comment>,
              val user: UserRepository.User, val userInteraction: List<Interaction> = emptyList()) {

    fun like(): Comment = Comment(id, parent, video, text, editDate, postDate, likes + 1, dislikes, replies, user, mutableListOf(Interaction.Like, *userInteraction.toTypedArray()))
    fun dislike(): Comment = Comment(id, parent, video, text, editDate, postDate, likes, dislikes + 1, replies, user, mutableListOf(Interaction.Dislike, *userInteraction.toTypedArray()))
    fun clear(): Comment {
        val likeDelta = likes - userInteraction.filter { it == Comment.Interaction.Like }.size
        val dislikeDelta = likes - userInteraction.filter { it == Comment.Interaction.Dislike }.size
        return Comment(id, parent, video, text, editDate, postDate, likes - likeDelta,
                dislikes - dislikeDelta, replies, user, emptyList())
    }

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

    companion object {
        val ItemCallback = object : DiffUtil.ItemCallback<Comment>() {
                    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem.id == newItem.id

                    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean = newItem == oldItem
                }
    }
}