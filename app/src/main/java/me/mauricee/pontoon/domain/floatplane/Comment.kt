package me.mauricee.pontoon.domain.floatplane

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

@Keep
data class Comment(@SerializedName("editDate") val editDate: Instant, //:"2018-05-26T22:05:23.145Z"
                   @SerializedName("id") val id: String, //:"5b09da23060dfe3930a49b1d"
                   @SerializedName("interactionCounts") val interactionCounts: InteractionCounts, //:{like: 0, dislike: 0}
                   @SerializedName("interactions") val interactions: List<Any>, //:[] not sure what this is supposed to be
                   @SerializedName("postDate") val postDate: Instant, //:"2018-05-26T22:05:23.145Z"
                   @SerializedName("replies") val replies: List<Comment>, //:[]
                   @SerializedName("replying") val replying: String?,
                   @SerializedName("text") val text: String, //:"I think the 4k model seems pretty interesting for a macbook pro windows equivalent. I know it's over done but it would be interesting to see a comparison."
                   @SerializedName("user") val user: String, //:"5aa1a598bd064bc2644b9a6e"
                   @SerializedName("video") val video: String  //:"8tkQAqaOMu"
) {
    @Keep
    data class Container(@SerializedName("comments") val comments: List<Comment>)

    @Keep
    data class Interaction(@SerializedName("commentGUID") val id: String, @SerializedName("type") val type: InteractionType)

    @Keep
    data class Post(@SerializedName("text") val text: String, @SerializedName("videoGUID") val id: String)

    @Keep
    data class InteractionCounts(@SerializedName("like") val like: Int, @SerializedName("dislike") val dislike: Int)

    enum class InteractionType {
        @SerializedName("like")
        Like,
        @SerializedName("dislike")
        Dislike
    }
}