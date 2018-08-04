package me.mauricee.pontoon.model.comment

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

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg creatorEntity: CommentEntity)

    @Update
    fun update(vararg creatorEntity: CommentEntity)

    @Query("SELECT * FROM Comment WHERE id IN (:userIds)")
    fun getCommentByParent(vararg userIds: String): Single<List<CommentEntity>>

}

data class Comment(val id: String, val parent: String, val video: String, val text: String,
                   val likes: Int, val dislikes: Int, val replies: List<Comment>,
                   val user: UserRepository.User)