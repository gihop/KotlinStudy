package com.androidhuman.example.simplegithub.api.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

//GithubRepo 엔티티의 데이터가 저장될 테이블 이름을 repositories로 지정한다.
@Entity(tableName = "repositories")
class GithubRepo(
        val name: String,
        @SerializedName("full_name")
        //fullName 프로퍼티를 주요 키로 사용하며, 테이블 내 필드 이름은 full_name으로 지정한다.
        @PrimaryKey @ColumnInfo(name = "full_name") val fullName: String,

        //GithubOwner 내 필드를 테이블에 함께 저장한다.
        @Embedded val owner: GithubOwner,

        //널 값을 허용할 수 있는 타입으로 변경.
        val description: String?,
        val language: String?,
        @SerializedName("updated_at")
        //updatedAt 프로퍼티의 테이블 내 필드 이름을 updated_at으로 지정한다.
        @ColumnInfo(name = "updated_att") val updatedAt: String,

        @SerializedName("stargazers_count") val stars: Int)