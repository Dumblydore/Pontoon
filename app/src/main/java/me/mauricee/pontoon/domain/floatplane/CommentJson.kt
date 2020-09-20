package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

data class CommentJson(@SerializedName("editDate") val editDate: Instant,
                       @SerializedName("id") val id: String,
                       @SerializedName("interactionCounts") val interactionCounts: InteractionCounts,
                       @SerializedName("postDate") val postDate: Instant,
                       @SerializedName("replies") val replies: List<CommentJson>,
                       @SerializedName("replying") val replying: String?,
                       @SerializedName("text") val text: String,
                       @SerializedName("user") val user: String,
                       @SerializedName("video") val video: String) {
    data class Container(@SerializedName("comments") val comments: List<CommentJson>, @SerializedName("userInteractions") val interactions: List<UserInteraction>)
}

data class CommentInteraction(@SerializedName("commentGUID") val id: String, @SerializedName("type") val type: Type) {
    enum class Type {
        @SerializedName("like")
        Like,
        @SerializedName("dislike")
        Dislike
    }
}

data class CommentPost(@SerializedName("text") val text: String, @SerializedName("videoGUID") val id: String)
data class Reply(@SerializedName("replyTo") val replyTo: String,
                 @SerializedName("text") val text: String,
                 @SerializedName("videoGUID") val id: String)

data class InteractionCounts(@SerializedName("like") val like: Int, @SerializedName("dislike") val dislike: Int)

data class InteractionResult(@SerializedName("comment") val string: String)

data class UserInteraction(@SerializedName("comment") val comment: String,
                           @SerializedName("id") val id: String,
                           @SerializedName("type") val type: Type) {
    data class Type(@SerializedName("displayName") val displayName: String,
                    @SerializedName("id") val id: String,
                    @SerializedName("name") val type: CommentInteraction.Type)
}

data class ClearInteraction(@SerializedName("commentGUID") val id: String)