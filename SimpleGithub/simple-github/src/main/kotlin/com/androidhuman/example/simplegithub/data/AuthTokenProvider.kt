package com.androidhuman.example.simplegithub.data

import android.content.Context
import android.preference.PreferenceManager

class AuthTokenProvider(private val context: Context) {
    fun updateToken(token: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply()
    }

    //읽기 전용 프로퍼티로 엑세스 토큰 값을 제공한다.
    val token: String?
        get() = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_AUTH_TOKEN, null)

    //정적 필드는 동반 객체 내부의 프로퍼티로 변환된다.
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

}