package com.androidhuman.example.simplegithub.api

import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.api.model.RepoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import io.reactivex.Observable

interface GithubApi {
    @GET("search/repositories")
    fun searchRepository(@Query("q") query: String): Observable<RepoSearchResponse>//반환 타입 변경.

    @GET("repos/{owner}/{name}")
    fun getRepository(@Path("owner") ownerLogin: String,
                      @Path("name") repoName: String): Observable<GithubRepo>//반환 타입 변경.
}