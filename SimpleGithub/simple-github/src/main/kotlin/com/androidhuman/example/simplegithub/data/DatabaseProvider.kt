package com.androidhuman.example.simplegithub.data

import android.arch.persistence.room.Room
import android.content.Context

//SimpleGithubDatabase의 인스턴스를 저장한다.
private var instance: SimpleGithubDatabase? = null

//저장소 조회 기록을 담당하는 데이터 접근 객체를 제공한다.
fun providerSearchHistoryDao(context: Context): SearchHistoryDao
        = provideDatabase(context).searchHistoryDao()

//SimpleGithubDatabase 룸 데이터베이스를 제공한다.
//싱글톤 패턴을 사용하여 인스턴스를 최초 1회만 생성한다.
private fun provideDatabase(context: Context):SimpleGithubDatabase{
    if(null == instance){
        //simple_github.db 데이터베이스 파일을 사용하는 룸 데이터베이스를 생성한다.
        instance = Room.databaseBuilder(context.applicationContext,
                SimpleGithubDatabase::class.java, "my_simple_github.db")
                .build()
    }

    //룸 데이터베이스 인스턴스를 반환한다.
    return instance!!
}