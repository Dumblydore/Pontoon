package me.mauricee.pontoon.domain.floatplane

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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

    @get:GET("edges")
    val edges: Observable<Edge.Response>

    @get:GET("user/self")
    val self: Observable<UserJson>

    @POST("auth/logout")
    fun logout() : Completable

    @POST("activation/email/confirm")
    fun confirmEmail(@Body confirmationRequest: ConfirmationRequest) : Completable

    @POST("auth/login")
    fun login(@Body loginCredentials: LoginRequest): Observable<UserJson.Container>

    @POST("auth/checkFor2faLogin")
    fun login(@Body token: LoginAuthToken): Observable<UserJson.Container>

    @GET("user/info")
    fun getUsers(@Query("id") vararg id: String): Single<UserJson.Response>

    @GET("creator/info")
    fun getCreators(@Query("creatorGUID") vararg creatorId: String): Single<List<CreatorJson>>

    @GET("creator/videos")
    fun getVideos(@Query("creatorGUID") creatorId: String, @Query("fetchAfter") startWith: Int = 0): Observable<List<VideoJson>>

    @GET("video/related")
    fun getRelatedVideos(@Query("videoGUID") id: String): Single<List<VideoJson>>

    @GET("video/info")
    fun getVideo(@Query("videoGUID") id: String): Single<VideoJson>

    @GET("video/comments")
    fun getVideoComments(@Query("videoGUID") id: String): Observable<Comment.Container>

    @GET("video/url")
    fun getVideoUrl(@Query("guid") id: String, @Query("quality") quality: String = "1080"): Observable<ResponseBody>

    @GET("user/activity")
    fun getActivity(@Query("id") id: String): Single<ActivityJson.Response>

    @POST("video/comment")
    fun post(@Body comment: CommentPost): Observable<Comment>

    @POST("video/comment")
    fun post(@Body comment: Reply): Observable<Comment>

    @POST("video/comment/interaction/set")
    fun setComment(@Body interaction: CommentInteraction): Observable<InteractionResult>

    @POST("video/comment/interaction/clear")
    fun clearInteraction(@Body body: ClearInteraction): Observable<Void>
}