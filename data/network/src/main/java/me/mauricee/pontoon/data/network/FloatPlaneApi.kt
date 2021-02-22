package me.mauricee.pontoon.data.network

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.data.network.activation.email.confirm.ConfirmationRequest
import me.mauricee.pontoon.data.network.creator.info.CreatorJson
import me.mauricee.pontoon.data.network.creator.list.CreatorListItem
import me.mauricee.pontoon.data.network.user.ActivityJson
import me.mauricee.pontoon.data.network.user.UserJson
import me.mauricee.pontoon.data.network.user.subscription.SubscriptionJson
import me.mauricee.pontoon.data.network.video.VideoJson
import me.mauricee.pontoon.data.network.video.comment.*
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FloatPlaneApi {

    @get:GET("user/subscriptions")
    val subscriptions: Single<List<SubscriptionJson>>

    @get:GET("creator/list")
    val allCreators: Single<List<CreatorListItem>>

    @get:GET("user/self")
    val self: Single<UserJson>

    @POST("auth/logout")
    fun logout(): Completable

    @POST("activation/email/confirm")
    fun confirmEmail(@Body confirmationRequest: ConfirmationRequest): Completable

    @POST("auth/login")
    fun login(@Body loginCredentials: LoginRequest): Single<UserJson.Container>

    @POST("auth/checkFor2faLogin")
    fun login(@Body token: LoginAuthToken): Single<UserJson.Container>

    @GET("user/info")
    fun getUsers(@Query("id") vararg id: String): Single<UserJson.Response>

    @GET("creator/info")
    fun getCreators(@Query("creatorGUID") vararg creatorId: String): Single<List<CreatorJson>>

    @GET("creator/videos")
    fun getVideos(@Query("creatorGUID") creatorId: String, @Query("fetchAfter") startWith: Int = 0): Single<List<VideoJson>>

    @GET("creator/videos")
    fun searchVideos(@Query("creatorGUID") id: String, @Query("search") query: String, @Query("fetchAfter") startWith: Int = 0): Single<List<VideoJson>>

    @GET("video/related")
    fun getRelatedVideos(@Query("videoGUID") id: String): Single<List<VideoJson>>

    @GET("video/info")
    fun getVideo(@Query("videoGUID") id: String): Single<VideoJson>

    @GET("video/comments")
    fun getVideoComments(@Query("videoGUID") id: String, @Query("limit") limit: Int, @Query("fetchAfter") fetchAfter: String?): Single<CommentJson.Container>

    @GET("video/url")
    fun getVideoUrl(@Query("guid") id: String, @Query("quality") quality: String = "1080"): Observable<ResponseBody>
    
    @GET("v2/cdn/delivery")
    fun getVideoContent(@Query("guid") videoId: String, @Query("type") type: ContentType): Single<VideoContentJson>

    @GET("user/activity")
    fun getActivity(@Query("id") id: String): Single<ActivityJson.Response>

    @POST("video/comment")
    fun post(@Body comment: CommentPost): Observable<CommentJson>

    @POST("video/comment")
    fun post(@Body comment: Reply): Observable<CommentJson>

    @POST("video/comment/interaction/set")
    fun setComment(@Body interaction: CommentInteraction): Completable

    @POST("video/comment/interaction/clear")
    fun clearInteraction(@Body body: ClearInteraction): Completable
}