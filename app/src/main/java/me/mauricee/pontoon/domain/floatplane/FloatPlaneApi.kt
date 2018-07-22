package me.mauricee.pontoon.domain.floatplane

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FloatPlaneApi {

    @get:GET("user/subscriptions")
    val subscriptions: Observable<List<Subscription>>

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
    fun getVideoComments(@Query("videoGUID") id: String): Observable<Comment.Container>

    @GET("video/url")
    fun getVideoUrl(@Query("guid") id: String, @Query("quality") quality: String = "1080"): Observable<ResponseBody>

    @POST("auth/login")
    fun login(@Body loginCredentials: LoginRequest): Observable<User.Container>
}