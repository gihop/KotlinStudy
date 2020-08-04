package com.androidhuman.example.simplegithub.di

import com.androidhuman.example.simplegithub.ui.main.MainActivity
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import com.androidhuman.example.simplegithub.ui.signin.SignInActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

//모듈 클래스로 표시한다.
//필요한 객체를 자체적으로 생성하는 대신, 대거를 통해 생성된 객체를 주입받으려면 액티비티를 객체 그래프에 추가해야한다.
//특정 액티비티를 객체 그래프에 추가하려면, 새로운 모듈을 생성한 후 이 모듈에 객체 그래프에 추가할 액티비티를 추가한다.
//객체 그래프에 추가할 액티비티는 해당 액티비티를 반환하는 함수에 @ContributesAndroidInjector 어노테이션을 추가하여 선언.
@Module
abstract class ActivityBinder {
    //SignActivity를 객체 그래프에 추가.
    //SignInModule를 객체 그래프에 추가.
    @ContributesAndroidInjector(modules = arrayOf(SignInModule::class))
    abstract fun bindSignInActivity(): SignInActivity

    //MainActivity를 객체 그래프에 추가.
    //MainModule을 객체 그래프에 추가.
    @ContributesAndroidInjector(modules = arrayOf(MainModule::class))
    abstract fun bindMainActivity(): MainActivity

    //SearchActivity를 객체 그래프에 추가.
    //SearchModule을 객체 그래프에 추가.
    @ContributesAndroidInjector(modules = arrayOf(SearchModule::class))
    abstract fun bindSearchActivity(): SearchActivity

    //RepositoryActivity를 객체 그래프에 추가.
    //RepositoryModule을 객체 그래프에 추가.
    @ContributesAndroidInjector(modules = arrayOf(RepositoryModule::class))
    abstract fun bindRepositoryActivity(): RepositoryActivity
}