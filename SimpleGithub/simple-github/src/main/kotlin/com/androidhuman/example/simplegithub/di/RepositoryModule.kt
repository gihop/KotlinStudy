package com.androidhuman.example.simplegithub.di

import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.ui.repo.RepositoryViewModelFactory
import dagger.Module
import dagger.Provides

//RepositoryActivity에서 뷰모델 클래스를 만들 때 필요한 RepositoryViewModelFactory의 객체를
//대거를 통해 주입받도록 RepositoryModule을 작성한다.
@Module
class RepositoryModule {
    //RepositoryViewModelFactory 객체를 제공한다.
    @Provides
    fun provideViewModelFactory(githubApi: GithubApi)
            : RepositoryViewModelFactory
            = RepositoryViewModelFactory(githubApi)
}