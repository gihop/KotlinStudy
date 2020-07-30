package com.androidhuman.example.simplegithub.ui.signin

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.androidhuman.example.simplegithub.api.AuthApi
import com.androidhuman.example.simplegithub.data.AuthTokenProvider

//SignInViewModel은 생성자에서 인자를 받으므로 SignInViewModel을 생성하는 방법을 정의한
//뷰모델 팩토리 클래스를 추가로 정의해야 한다.
class SignInViewModelFactory(
        val api: AuthApi,
        val authTokenProvider: AuthTokenProvider)
    : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SignInViewModel(api, authTokenProvider) as T
    }
}