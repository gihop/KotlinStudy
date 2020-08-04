package com.androidhuman.example.simplegithub.api

import android.content.Context
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

//GithubApiProvider 클래스에 포함된 함수에서는 필요한 인자를 함수의 인자로 받기만 할 뿐, 클래스 단위에서 공용으로 사용하는 객체가 없다.
//따라서 싱글톤 클래스를 제거하고 패키지 단위 함수로 다시 선언한다.

//함수 내부에서 변수나 값을 선언하거나 연산을 수행하는 부분 없이, 생성된 객체를 반환하는 코드로만 구성되어 있는 함수들은 단일 표현식 형태로 바꾼다.
fun provideAuthApi(): AuthApi
    = Retrofit.Builder()
        .baseUrl("https://github.com/")
        //HTTP 요청에 인증 토큰을 추가하지 않는 OkHttpClient 객체를 사용한다.
        .client(provideOkHttpClient(provideLoggingInterceptor(), null))
        //Retrofit에서 받은 응답을 옵저버블 형태로 변환해주도록 RxJava2CallAdapterFactory를 API의 콜 어댑터로 추가하며,
        // 비동기 방식으로 API를 호출하도록 RxJava2CallAdapterFactory.createAsync() 메서드로 콜 어댑터를 생성한다.
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)


fun provideGithubApi(context: Context): GithubApi
    = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        //HTTP 요청에 인증 토큰을 추가하는 OkHttpClient 객체를 사용한다.
        .client(provideOkHttpClient(provideLoggingInterceptor(),
                provideAuthInterceptor(provideAuthTokenProvider(context))))
        //받은 응답을 옵저버블 형태로 변환하며, 비동기 방식으로 API를 호출한다.
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GithubApi::class.java)

//apply()나 run()과 같은 범위 지정 함수를 사용하면 함수 내부의 변수 선언을 완전히 제거할 수 있으며,
//이 경우 함수 내부에 객체를 반환하는 코드만 남게되므로 이 또한 단일 표현식으로 표현할 수 있다.
//OkHttpClient 객체를 제공한다.
//HTTP 요청과 응답을 로그로 추가해주는 HttpLoggingInterceptor는 필수이지만,
//HTTP 요청에 인증 토큰을 추가해주는 AuthInterceptor는 선택적으로 받는다.
private fun provideOkHttpClient(
        interceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor?): OkHttpClient
        = OkHttpClient.Builder()
        .run(){
            if (null != authInterceptor) {
                addInterceptor(authInterceptor)
            }
            addInterceptor(interceptor)
            build()
        }

//apply() 함수로 인스턴스 생성과 프로퍼티 값 변경을 동시에 수행한다.
private fun provideLoggingInterceptor(): HttpLoggingInterceptor
     = HttpLoggingInterceptor().apply{ level = HttpLoggingInterceptor.Level.BODY }

private fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
    val token = provider.token ?: throw IllegalStateException("authToken cannot be null.")
    return AuthInterceptor(token)
}

private fun provideAuthTokenProvider(context: Context): AuthTokenProvider
    = AuthTokenProvider(context.applicationContext)


//class AuthInterceptor(private val token: String) : Interceptor {
//    @Throws(IOException::class)
//    override fun intercept(chain: Interceptor.Chain)
//            //with() 함수와 run() 함수로 추가 변수 선언을 제거한다.
//            : Response  = with(chain){
//        val newRequest = request().newBuilder().run{
//            addHeader("Authorization", "token " + token)
//            build()
//        }
//        proceed(newRequest)
//    }
//}