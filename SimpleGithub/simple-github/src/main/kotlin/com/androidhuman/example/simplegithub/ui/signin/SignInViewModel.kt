package com.androidhuman.example.simplegithub.ui.signin

import android.arch.lifecycle.ViewModel
import com.androidhuman.example.simplegithub.api.AuthApi
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import com.androidhuman.example.simplegithub.util.SupportOptional
import com.androidhuman.example.simplegithub.util.optionalOf
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

//API 호출에 필요한 객체와 기기에 저장된 엑세스 토큰을 관리할 때 필요한 객체를 생성자의 인자로 전달받는다.
class SignInViewModel(
        val api: AuthApi,
        val authTokenProvider: AuthTokenProvider): ViewModel() {

    //엑세스 토큰을 전달할 서브젝트.
    val accessToken: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.create()

    //에러 메시지를 전달할 서브젝트.
    val message: PublishSubject<String> = PublishSubject.create()

    //작업 진행 상태를 전달할 서브젝트. 초기값으로 false를 지정.
    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    //기기에 저장된 엑세스 토큰을 불러온다.
    fun loadAccessToken(): Disposable
            //저장된 토큰이 없는 경우 authTokenProvider.token이 null을 반환한다.
            //따라서 optionalOf() 함수를 사용하여 SupportOptional로 이를 감싸준다.
            = Single.fromCallable { optionalOf(authTokenProvider.token) }
            .subscribeOn(Schedulers.io())
            .subscribe(Consumer<SupportOptional<String>>{
                //엑세스 토큰을 전달하는 서브젝트로 엑세스 토큰을 전달한다.
                accessToken.onNext(it)
            })

    //API를 통해 엑세스 토큰을 요청한다.
    fun requestAccessToken(clientId: String, clientSecret: String, code: String): Disposable
            = api.getAccessToken(clientId, clientSecret, code)

            //API 응답 중에서 엑세스 토큰만 추출한다.
            .map{ it.accessToken }

            //API 호출을 시작하면 작업 진행 상태를 true로 변경한다.
            //onNext()를 사용하여 서브젝트에 이벤트를 전달한다.
            .doOnSubscribe { isLoading.onNext(true) }

            //작업이 완료되면(오류, 정상 종료 포함) 작업 진행 상태를 false로 변경한다.
            .doOnTerminate { isLoading.onNext(false) }
            .subscribe({ token ->
                //API를 통해 엑세스 토큰을 받으면 기기에 엑세스 토큰을 저장한다.
                authTokenProvider.updateToken(token)

                //엑세스 토큰을 전달하는 서브젝트에 새로운 엑세스 토큰을 전달한다.
                accessToken.onNext(optionalOf(token))
            }) {
               //오류가 발생하면 에러 메시지를 전달하는 서브젝트에 메시지를 전달한다.
                message.onNext(it.message ?: "Unexpected error")
            }
}