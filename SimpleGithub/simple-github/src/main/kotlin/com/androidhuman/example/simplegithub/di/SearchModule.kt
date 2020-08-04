package com.androidhuman.example.simplegithub.di

import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter
import com.androidhuman.example.simplegithub.ui.search.SearchViewModelFactory
import dagger.Module
import dagger.Provides

//SearchActicity에서는 검색 결과를 표시할 때 사용하는 SearchAdapter와
//이 액티비티의 뷰모델 클래스를 만들 때 필요한 SearchViewModelFactory의 객체를 대거를 통해 주입받도록 SearchModule 생성.
//SearchAdapter와 SearchViewModelFactory 객체를 제공한다.
@Module
class SearchModule {
    //SearchAdapter 객체를 제공한다.
    @Provides
    fun provideAdapter(activity: SearchActivity): SearchAdapter
            = SearchAdapter().apply{ setItemClickListener(activity) }

    //SearchViewModelFactory 객체를 제공한다.
    @Provides
    fun provideViewModelFactory(
            githubApi: GithubApi, searchHistoryDao: SearchHistoryDao)
            : SearchViewModelFactory
            = SearchViewModelFactory(githubApi, searchHistoryDao)
}