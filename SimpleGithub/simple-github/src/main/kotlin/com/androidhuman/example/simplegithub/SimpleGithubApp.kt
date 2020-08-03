package com.androidhuman.example.simplegithub

import com.androidhuman.example.simplegithub.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

//대거의 안드로이드 지원 기능을 편리하게 사용하기 위해 DaggerApplication을 상속한다.
class SimpleGithubApp: DaggerApplication() {
    //이 함수를 구현해야 한다.
    //DaggerAppComponent의 인스턴스를 반환한다.
    override fun applicationInjector(): AndroidInjector<out DaggerApplication>{
        return DaggerAppComponent.builder().application(this).build()
    }
}