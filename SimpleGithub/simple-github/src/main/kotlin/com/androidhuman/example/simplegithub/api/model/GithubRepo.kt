package com.androidhuman.example.simplegithub.api.model

import com.google.gson.annotations.SerializedName

class GithubRepo(
        val name: String,
        @SerializedName("full_name") val fullName: String,
        val owner: GithubOwner,
        //널 값을 허용할 수 있는 타입으로 변경.
        val description: String?, val language: String?,
        @SerializedName("updated_at") val updatedAt: String,
        @SerializedName("stargazers_count") val stars: Int
)