package com.androidhuman.example.simplegithub.di

import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import com.androidhuman.example.simplegithub.ui.main.MainViewModelFactory
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter
import dagger.Module
import dagger.Provides

//MainActivity에서 사용하는 객체 중, 저장소 조회 기록을 표시할 때 사용하는 SearchAdapter와
//이 액티비티의 뷰모델 클래스를 만들 때 필요한 MainViewModelFactory의 객체를 대거를 통해 제공하기 위해 필요한 MainModule.
//SearchAdapter와 MainViewModelFactory 객체를 제공한다.
@Module
class MainModule {
    //SearchAdapter 객체를 제공한다.
    @Provides
    fun provideAdater(activity: MainActivity): SearchAdapter
            = SearchAdapter().apply{ setItemClickListener(activity) }

    //MainViewModelFactory 객체를 제공한다.
    @Provides
    fun provideViewModelFactory(searchHistoryDao: SearchHistoryDao)
            : MainViewModelFactory
            = MainViewModelFactory(searchHistoryDao)
}