package me.mauricee.pontoon.domain.floatplane

import com.squareup.moshi.Json
import org.threeten.bp.Instant

data class Comment(@Json(name = "editDate") val editDate: Instant,
                   @Json(name = "id") val id: String,
                   @Json(name = "interactionCounts") val interactionCounts: InteractionCounts,
                   @Json(name = "postDate") val postDate: Instant,
                   @Json(name = "replies") val replies: List<Comment>,
                   @Json(name = "replying") val replying: String?,
                   @Json(name = "text") val text: String,
                   @Json(name = "user") val user: String,
                   @Json(name = "video") val video: String) {
    data class Container(@Json(name = "comments") val comments: List<Comment>, @Json(name = "userInteractions") val interactions: List<UserInteraction>)
}

data class CommentInteraction(@Json(name = "commentGUID") val id: String, @Json(name = "type") val type: Type) {
    enum class Type {
        @Json(name = "like")
        Like,
        @Json(name = "dislike")
        Dislike
    }
}

data class CommentPost(@Json(name = "text") val text: String, @Json(name = "videoGUID") val id: String)
data class Reply(@Json(name = "replyTo") val replyTo: String,
                 @Json(name = "text") val text: String,
                 @Json(name = "videoGUID") val id: String)

data class InteractionCounts(@Json(name = "like") val like: Int, @Json(name = "dislike") val dislike: Int)

data class InteractionResult(@Json(name = "comment") val string: String)

data class UserInteraction(@Json(name = "comment") val comment: String,
                           @Json(name = "id") val id: String,
                           @Json(name = "type") val type: Type) {
    data class Type(@Json(name = "displayName") val displayName: String,
                    @Json(name = "id") val id: String,
                    @Json(name = "name") val type: CommentInteraction.Type)
}

data class ClearInteraction(@Json(name = "commentGUID") val id: String)