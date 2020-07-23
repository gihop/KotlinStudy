package com.androidhuman.example.simplegithub.ui.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.androidhuman.example.simplegithub.BuildConfig
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.AuthApi
import com.androidhuman.example.simplegithub.api.GithubApiProvider
import com.androidhuman.example.simplegithub.api.model.GithubAccessToken
import com.androidhuman.example.simplegithub.data.AuthTokenProvider
import com.androidhuman.example.simplegithub.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {
    internal lateinit var btnStart: Button
    internal lateinit var progress: ProgressBar
    internal lateinit var api: AuthApi
    internal lateinit var authTokenProvider: AuthTokenProvider
    internal lateinit var accessTokenCall: Call<GithubAccessToken>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        btnStart = findViewById(R.id.btnActivitySignInStart)
        progress = findViewById(R.id.pbActivitySignIn)

        //View.OnClickListener의 본체를 람다 표현식으로 작성한다.
        btnStart.setOnClickListener(View.OnClickListener {
            val authUri = Uri.Builder().scheme("https").authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                    .build()
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@SignInActivity, authUri)
        })
        api = GithubApiProvider.provideAuthApi()
        authTokenProvider = AuthTokenProvider(this)
        if (null != authTokenProvider!!.token) {
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

    private fun getAccessToken(code: String) {
        showProgress()
        accessTokenCall = api!!.getAccessToken(
                BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)

        //Call 인터페이스를 구현하는 익명 클래스의 인스턴스를 생성한다.
        accessTokenCall.enqueue(object : Callback<GithubAccessToken?> {
            override fun onResponse(call: Call<GithubAccessToken?>,
                                    response: Response<GithubAccessToken?>) {
                hideProgress()
                val token = response.body()
                if (response.isSuccessful && null != token) {
                    authTokenProvider!!.updateToken(token.accessToken)
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
        btnStart!!.visibility = View.GONE
        progress!!.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        btnStart!!.visibility = View.VISIBLE
        progress!!.visibility = View.GONE
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