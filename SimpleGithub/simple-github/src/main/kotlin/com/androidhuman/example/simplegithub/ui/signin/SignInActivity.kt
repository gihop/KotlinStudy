package com.androidhuman.example.simplegithub.ui.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.androidhuman.example.simplegithub.BuildConfig
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.provideAuthApi
import com.androidhuman.example.simplegithub.api.model.GithubAccessToken
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.newTask
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {
    //패키지 단위 함수를 호출한다.
    //lazy 프로퍼티를 적용하기 위해 val로 바꾼다.
    //프로퍼티를 사용하기 전에 초기화를 안해도 컴파일 에러가 발생하지 않는 lateinit보다 프로퍼티를 최초로 사용하는 시점에 초기화를 수행하는 lazy로 바꾼다.
    //또한 프로퍼티 선언과 동시에 이에 들어갈 값을 넣어주므로, 타입 추론 기능을 사용할 수 있다.
    internal val api by lazy { provideAuthApi() }
    internal val authTokenProvider by lazy { AuthTokenProvider(this) }

    //여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화한다.
    //val accessTokenCall: Call<GithubAccessToken>? = null 를 대체한다.
    internal val disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //View.OnClickListener의 본체를 람다 표현식으로 작성한다.
        //버튼 인스턴스 선언 없이 코틀린 익스텐션을 써서 뷰 ID로 인스턴스에 접근한다.
        btnActivitySignInStart.setOnClickListener(View.OnClickListener {
            val authUri = Uri.Builder().scheme("https").authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                    .build()
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@SignInActivity, authUri)
        })

        if (null != authTokenProvider.token) {
            launchMainActivity()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        showProgress()

        //엘비스 연산자를 사용하여 널 값을 검사한다.
        //intent.data가 널이라면 IllegalStateException 예외를 발생시킨다.
        val uri = intent.data ?: throw IllegalArgumentException("No data exists")

        //엘비스 연산자를 사용하여 널 값을 검사한다.
        //uri.getQueryParameter("code") 반환값이 널이라면 IllegalStateException 예외를 발생시킨다.
        val code = uri.getQueryParameter("code") ?: throw IllegalStateException("No code exists")
        getAccessToken(code)
    }

    override fun onStop() {
        super.onStop()

        //관리하고 있던 디스포저블 객체를 모두 해제한다.
        //디스포저블이 해제되는 시점에 진행 중인 네트워크 요청이 있었다면 자동으로 취소된다.
        //accessTokenCall?.run { cancel() } 대신 사용한다.
        disposables.clear()
    }

    private fun getAccessToken(code: String) {
        //REST API를 통해 엑세스 토큰을 요청한다.
        disposables.add(api.getAccessToken(
                BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)

                //REST API를 통해 받은 응답에서 엑세스 토큰만 추출한다.
                .map{ it.accessToken }

                //이 이후에 수행되는 코드는 모두 메인 스레드에서 실행한다.
                //RxAndroid에서 제공하는 스케줄러인 AndroidSchedulers.mainThread()를 사용한다.
                .observeOn(AndroidSchedulers.mainThread())

                //구독할 때 수행할 작업을 구현한다.
                .doOnSubscribe{ showProgress() }

                //스트림이 종료될 때 수행할 작업을 구현한다.
                .doOnTerminate{ hideProgress() }

                //옵저버블을 구독한다.
                .subscribe({ token ->
                    //API를 통해 엑세스 토큰을 정상적으로 받았을 때 처리할 작업을 구현한다.
                    //작업 중 오류가 발생하면 이 블록은 호출되지 않는다.
                    authTokenProvider.updateToken(token)
                    launchMainActivity()
                }){
                    //에러 블록.
                    //네트워크 오류다 데이터 처리 오류 등 작업이 정상적으로 완료되지 않았을 때 호출된다.
                    showError(it)
                })
    }

    private fun showProgress() {
        //인스턴스 선언 없이 뷰 ID를 사용하여 인스턴스에 접근한다.
        btnActivitySignInStart.visibility = View.GONE
        pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        //인스턴스 선언 없이 뷰 ID를 사용하여 인스턴스에 접근한다.
        btnActivitySignInStart.visibility = View.VISIBLE
        pbActivitySignIn.visibility = View.GONE
    }

    private fun showError(throwable: Throwable) {
        //긴 시간 동안 표시되는 토스트 메시지를 출력한다.
        //longToast() 함수는 인자로 널 값을 허용하지 않으므로 throwable.message의 값이 널인 경우 대체하여 표시할
        //문자열을 지정해준다.
        longToast(throwable.message ?: "No message available")
    }

    private fun launchMainActivity() {
        //intentFor() 함수를 사용하면 MainActivity를 호출하기 위해 생성한 인텐트 객체를 더 간략하게 표현할 수 있다.
        startActivity(intentFor<MainActivity>().clearTask().newTask())
    }
}