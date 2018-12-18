package me.mauricee.pontoon.domain.github

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApi {
    @GET("repos/:owner/:repo/releases/latest")
    fun getLatestRelease(@Path("owner") owner: String, @Path("repo") repo: String) : Single<Release>
}