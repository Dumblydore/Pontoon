package me.mauricee.pontoon.repository.comment

import me.mauricee.pontoon.data.local.comment.CommentInteractionType
import me.mauricee.pontoon.repository.user.User
import org.threeten.bp.Instant

data class Comment(val id: String,
                   val video: String,
                   val editDate: Instant,
                   val likes: Int,
                   val dislikes: Int,
                   val postDate: Instant,
                   val text: String,
                   val userInteraction: CommentInteractionType?,
                   val user: User,
                   val replies: List<Comment>
)