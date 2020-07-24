package com.androidhuman.example.simplegithub.ui.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.androidhuman.example.simplegithub.BuildConfig
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.provideAuthApi
import com.androidhuman.example.simplegithub.api.model.GithubAccessToken
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_sign_in.*
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
    internal var accessTokenCall: Call<GithubAccessToken>? = null
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
        //액티비티가 화면에서 사라지는 시점에 API 호출 객체가 생성되어 있다면 API 요청을 취소한다.
        accessTokenCall?.run { cancel() }
    }

    private fun getAccessToken(code: String) {
        showProgress()
        accessTokenCall = api.getAccessToken(
                BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)

        //Call 인터페이스를 구현하는 익명 클래스의 인스턴스를 생성한다.
        //앞에서 API 호출에 필요한 객체를 받았으므로, 이 시점에서 accessTokenCall 객체의 값은 널이 아니다.
        //따라서 비 널 값 보증(!!)을 사용하여 이 객체를 사용한다.
        accessTokenCall!!.enqueue(object : Callback<GithubAccessToken?> {
            override fun onResponse(call: Call<GithubAccessToken?>,
                                    response: Response<GithubAccessToken?>) {
                hideProgress()
                val token = response.body()
                if (response.isSuccessful && null != token) {
                    authTokenProvider.updateToken(token.accessToken)
                    launchMainActivity()
                } else {
                    showError(IllegalStateException(
                            "Not successful: " + response.message()))
                }
            }

            override fun onFailure(call: Call<GithubAccessToken?>, t: Throwable) {
                hideProgress()
                showError(t)
            }
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
        Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
    }

    private fun launchMainActivity() {
        startActivity(Intent(
                this@SignInActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}