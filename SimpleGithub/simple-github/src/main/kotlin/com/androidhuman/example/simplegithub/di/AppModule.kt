package com.androidhuman.example.simplegithub.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

//애플리케이션 모듈은 애플리케이션이 실행되는 동안 공통으로 사용할 수 있는 객체를 제공한다.
//여기에서는 애플리케이션의 컨텍스트를 제공하도록 구성.
@Module
class AppModule {
    //다른 컨텍스트와의 혼동을 방지하기 위해 "appContext" 라는 이름으로 구분한다.
    @Provides
    @Named("appContext")
    @Singleton
    fun provideContext(application: Application): Context
            = application.applicationContext
}