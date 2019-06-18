package me.mauricee.pontoon.domain.floatplane

import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FloatPlaneApi {

    @get:GET("user/subscriptions")
    val subscriptions: Observable<List<Subscription>>

    @get:GET("creator/list")
    val allCreators: Observable<List<CreatorListItem>>

    @get:GET("edges")
    val edges: Observable<Edge.Response>

    @get:GET("user/self")
    val self: Observable<User>

    @POST("auth/logout")
    fun logout(): Completable

    @POST("activation/email/confirm")
    fun confirmEmail(@Body confirmationRequest: ConfirmationRequest): Completable

    @POST("auth/login")
    fun login(@Body loginCredentials: LoginRequest): Observable<User.Container>

    @POST("auth/checkFor2faLogin")
    fun login(@Body token: LoginAuthToken): Observable<User.Container>

    @GET("user/info")
    fun getUsers(@Query("id") vararg id: String): Observable<User.Response>

    @GET("creator/info")
    fun getCreator(@Query("creatorGUID") vararg creatorId: String): Observable<List<Creator>>

    @GET("creator/videos")
    fun getVideos(@Query("creatorGUID") creatorId: String, @Query("fetchAfter") startWith: Int = 0): Observable<List<Video>>

    @GET("video/related")
    fun getRelatedVideos(@Query("videoGUID") id: String): Observable<List<Video>>

    @GET("video/info")
    fun getVideoInfo(@Query("videoGUID") id: String): Observable<Video>

    @GET("video/comments")
    fun getVideoComments(@Query("videoGUID") id: String, @Query("limit") limit: Int = 20, @Query("commentGUID") afterComment: String? = null): Observable<Comment.Container>

    @GET("video/url")
    fun getVideoUrl(@Query("guid") id: String, @Query("quality") quality: String = "1080"): Observable<ResponseBody>

    @GET("user/activity")
    fun getActivity(@Query("id") id: String): Observable<Activity.Response>

    @POST("video/comment")
    fun post(@Body comment: CommentPost): Observable<Comment>

    @POST("video/comment")
    fun post(@Body comment: Reply): Observable<Comment>

    @POST("video/comment/interaction/set")
    fun setComment(@Body interaction: CommentInteraction): Observable<InteractionResult>

    @POST("video/comment/interaction/clear")
    fun clearInteraction(@Body body: ClearInteraction): Observable<Void>
}