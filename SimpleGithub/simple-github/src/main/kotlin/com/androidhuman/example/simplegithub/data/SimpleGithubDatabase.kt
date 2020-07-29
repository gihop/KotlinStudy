package com.androidhuman.example.simplegithub.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.androidhuman.example.simplegithub.api.model.GithubRepo

//데이터베이스에서 사용하는 엔티티와 버전을 지정한다.
@Database(entities = arrayOf(GithubRepo::class), version = 1)
abstract class SimpleGithubDatabase: RoomDatabase() {
    //데이터베이스와 연결할 데이터 접근 객체를 정의한다.
    abstract fun searchHistoryDao(): SearchHistoryDao
}