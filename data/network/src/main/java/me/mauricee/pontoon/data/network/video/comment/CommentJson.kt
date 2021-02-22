package me.mauricee.pontoon.data.network.video.comment

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant
import java.util.*

@JsonClass(generateAdapter = true)
data class CommentJson(@Json(name = "editDate") val editDate: Instant,
                       @Json(name = "id") val id: String,
                       @Json(name = "interactionCounts") val interactionCounts: InteractionCounts,
                       @Json(name = "postDate") val postDate: Instant,
                       @Json(name = "replies") val replies: List<CommentJson>,
                       @Json(name = "replying") val replying: String?,
                       @Json(name = "text") val text: String,
                       @Json(name = "user") val user: String,
                       @Json(name = "video") val video: String) {
    @JsonClass(generateAdapter = true)
    data class Container(@Json(name = "comments") val comments: List<CommentJson>, @Json(name = "userInteractions") val interactions: List<UserInteraction>)
}

@JsonClass(generateAdapter = true)
data class CommentInteraction(@Json(name = "commentGUID") val id: String, @Json(name = "type") val type: Type) {
    enum class Type {
        Like,
        Dislike;

        override fun toString(): String {
            return super.toString().toLowerCase(Locale.ROOT)
        }
    }
}

@JsonClass(generateAdapter = true)
data class CommentPost(@Json(name = "text") val text: String, @Json(name = "videoGUID") val id: String)

@JsonClass(generateAdapter = true)
data class Reply(@Json(name = "replyTo") val replyTo: String,
                 @Json(name = "text") val text: String,
                 @Json(name = "videoGUID") val id: String)

@JsonClass(generateAdapter = true)
data class InteractionCounts(@Json(name = "like") val like: Int, @Json(name = "dislike") val dislike: Int)

@JsonClass(generateAdapter = true)
data class InteractionResult(@Json(name = "comment") val string: String)

@JsonClass(generateAdapter = true)
data class UserInteraction(@Json(name = "comment") val comment: String,
                           @Json(name = "id") val id: String,
                           @Json(name = "type") val type: Type) {
    @JsonClass(generateAdapter = true)
    data class Type(@Json(name = "displayName") val displayName: String,
                    @Json(name = "id") val id: String,
                    @Json(name = "name") val type: CommentInteraction.Type)
}

@JsonClass(generateAdapter = true)
data class ClearInteraction(@Json(name = "commentGUID") val id: String)