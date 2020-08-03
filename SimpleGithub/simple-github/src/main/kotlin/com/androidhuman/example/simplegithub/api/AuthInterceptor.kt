package com.androidhuman.example.simplegithub.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String): Interceptor {
    @Throws
    override fun intercept(chain: Interceptor.Chain)
            : Response = with(chain){
        val newRequest = request().newBuilder().run{
            addHeader("Authorization", "token" + token)
            build()
        }
        proceed(newRequest)
    }
}