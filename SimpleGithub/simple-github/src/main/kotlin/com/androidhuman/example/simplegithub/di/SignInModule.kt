package com.androidhuman.example.simplegithub.di

import com.androidhuman.example.simplegithub.api.AuthApi
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import com.androidhuman.example.simplegithub.ui.signin.SignInViewModelFactory
import dagger.Module
import dagger.Provides

//모듈 클래스로 표시한다.
//SignInActivity의 뷰모델 클래스인 SignInViewModel의 객체를 얻으려면 SignInViewModelFactory 객체가 필요하다.
//대거에서 SignInViewModelFactory 객체를 제공하려면 이를 제공하는 SignInModule이 필요하다.
@Module
class SignInModule {
    //SignInViewModelFactory 객체를 제공한다.
    @Provides
    fun provideViewModelFactory(authApi: AuthApi, authTokenProvider: AuthTokenProvider)
            : SignInViewModelFactory
            = SignInViewModelFactory(authApi, authTokenProvider)
}