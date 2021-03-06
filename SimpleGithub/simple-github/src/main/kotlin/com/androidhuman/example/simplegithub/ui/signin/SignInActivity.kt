package com.androidhuman.example.simplegithub.ui.signin

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.view.View
import com.androidhuman.example.simplegithub.BuildConfig
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.newTask
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

//대거 안드로이드 지원 모듈을 통해 필요한 객체를 주입할 수 있도록 DaggerAppCompatActivity를 상속하도록 변경.
class SignInActivity : DaggerAppCompatActivity() {
    //여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화한다.
    //val accessTokenCall: Call<GithubAccessToken>? = null 를 대체한다.
    //CompositeDisposable에서 AutoClearedDisposable로 변경한다.
    internal val disposables = AutoClearedDisposable(this)

    //액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가한다.
    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    //뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언.
    lateinit var viewModel: SignInViewModel

    //AuthApi와 AuthTokenProvider를 주입받아 액티비티에서 객체를 생성하는 대신,
    //대거에서 SignInViewModelFactory 객체를 직접 주입받는다.
    @Inject lateinit var viewModelFactory: SignInViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        //SignInViewModel의 인스턴스를 받는다.
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SignInViewModel::class.java]

        //Lifecycle.addObserver() 함수를 사용하여 AutoClearedDisposable 객체를 옵저버로 등록한다.
        lifecycle += disposables

        //viewDisposables에서 이 액티비티의 생명주기 이벤트를 받도록 한다.
        lifecycle += viewDisposables

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

        //엑세스 토큰 이벤트를 구독한다.
        viewDisposables += viewModel.accessToken
                //엑세스 토큰이 없는 경우는 무시한다.
                .filter { !it.isEmpty }
                .observeOn(AndroidSchedulers.mainThread())
                //엑세스 토큰이 있는 것을 확인했다면 메인 화면으로 이동한다.
                .subscribe{ launchMainActivity() }

        //에러 메시지 이벤트를 구독한다.
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message -> showError(message) }

        //작업 진행 여부 이벤트를 구독한다.
        viewDisposables += viewModel.isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isLoading ->
                    //작업 진행 여부 이벤트에 따라 프로그레스바의 표시 상태를 변경한다.
                    if(isLoading){
                        showProgress()
                    } else {
                        hideProgress()
                    }
                }

        //기기에 저장되어 있는 엑세스 토큰을 불러온다.
        disposables += viewModel.loadAccessToken()
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

    /*onStop() 함수는 더이상 오버라이드하지 않아도 된다.
    override fun onStop() {
        super.onStop()

        //관리하고 있던 디스포저블 객체를 모두 해제한다.
        //디스포저블이 해제되는 시점에 진행 중인 네트워크 요청이 있었다면 자동으로 취소된다.
        //accessTokenCall?.run { cancel() } 대신 사용한다.
        disposables.clear()
    }*/

    private fun getAccessToken(code: String) {
        //ViewModel에 정의된 함수를 사용하여 새로운 액세스 토큰을 요청한다.
        disposables += viewModel.requestAccessToken(
                BuildConfig.GITHUB_CLIENT_ID,
                BuildConfig.GITHUB_CLIENT_SECRET, code)
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

    private fun showError(message: String) {
        longToast(message)
    }

    private fun launchMainActivity() {
        //intentFor() 함수를 사용하면 MainActivity를 호출하기 위해 생성한 인텐트 객체를 더 간략하게 표현할 수 있다.
        startActivity(intentFor<MainActivity>().clearTask().newTask())
    }
}