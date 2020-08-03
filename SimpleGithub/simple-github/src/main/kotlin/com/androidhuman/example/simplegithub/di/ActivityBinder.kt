package com.androidhuman.example.simplegithub.di

import com.androidhuman.example.simplegithub.ui.main.MainActivity
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import com.androidhuman.example.simplegithub.ui.signin.SignInActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

//모듈 클래스로 표시한다.
@Module
abstract class ActivityBinder {
    //SignActivity를 객체 그래프에 추가.
    @ContributesAndroidInjector
    abstract fun bindSignInActivity(): SignInActivity

    //MainActivity를 객체 그래프에 추가.
    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    //SearchActivity를 객체 그래프에 추가.
    @ContributesAndroidInjector
    abstract fun bindSearchActivity(): SearchActivity

    //RepositoryActivity를 객체 그래프에 추가.
    @ContributesAndroidInjector
    abstract fun bindRepositoryActivity(): RepositoryActivity
}