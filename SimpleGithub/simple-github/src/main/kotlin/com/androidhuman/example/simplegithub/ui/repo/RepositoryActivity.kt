package com.androidhuman.example.simplegithub.ui.repo

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewOutlineProvider
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.api.provideGithubApi
import com.androidhuman.example.simplegithub.ui.GlideApp
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_repository.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

//AppCompatActivity 대신 DaggerAppCompatActivity를 상속한다.
class RepositoryActivity : DaggerAppCompatActivity() {
    //정적 필드로 정의되어 있던 항목은 동반 객체 내부에 정의된다.
    //액티비티 호출 시 필요한 데이터를 전달할 때 사용하는 키 값들을 정의한 동반 객체 내 프로퍼티는 클래스 가장 위로 위치를
    //옮겨주고, 각 프로퍼티에 const 키워드를 추가한다.
    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    //여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화한다.
    //var repoCall: Call<GithubRepo>? = null 대신 사용한다.
    //CompositeDisposable에서 AutoClearedDisposable로 타입을 변경한다.
    internal val disposable = AutoClearedDisposable(this)

    //액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가한다.
    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    //뷰모델 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언한다.
    lateinit var viewModel: RepositoryViewModel

    //두 프로퍼티는 객체를 한번 생성하고 나면 이후에 변경할 일이 없기 때문에 변수가 아닌 값으로 바꿔준다.
    internal val dateFormatInResponse = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
    internal val dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    internal val viewModelFactory by lazy{
        //대거를 통해 주입받은 객체를 생성자의 인자로 전달한다.
        RepositoryViewModelFactory(githubApi)
    }

    //대거를 통해 GithubApi를 주입받는 프로퍼티를 선언한다.
    @Inject lateinit var githubApi: GithubApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        //RepositoryViewModel의 인스턴스를 받는다.
        viewModel = ViewModelProviders.of(this, viewModelFactory)[RepositoryViewModel::class.java]

        //Lifecycle.addObserver() 함수를 사용하여 AutoClearedDisposable 객체를 옵저버로 등록한다.
        lifecycle += disposable

        //viewDisposables에서 이 액티비티의 생명주기 이벤트를 받도록 한다.
        lifecycle += viewDisposables

        //저장소 정보 이벤트를 구독한다.
        viewDisposables += viewModel.repository
                //유효한 저장쏘 이벤트만 받도록 한다.
                .filter { !it.isEmpty }
                .map {it.value}
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repository ->
                    GlideApp.with(this@RepositoryActivity)
                            .load(repository.owner.avatarUrl)
                            .into(ivActivityRepositoryProfile)

                    tvActivityRepositoryName.text = repository.fullName
                    tvActivityRepositoryStars.text = resources
                            .getQuantityString(R.plurals.star,
                                    repository.stars, repository.stars)

                    if(null == repository.description){
                        tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                    } else {
                        tvActivityRepositoryDescription.text = repository.description
                    }
                    if(null == repository.language){
                        tvActivityRepositoryLanguage.setText(R.string.no_language_specified)
                    } else {
                        tvActivityRepositoryLanguage.text = repository.language
                    }

                    try{
                        val lastUpdate = dateFormatInResponse.parse(repository.updatedAt)
                        tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(lastUpdate)
                    } catch (e: ParseException){
                        tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                    }
                }

        //메시지 이벤트를 구독한다.
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())

                //메시지를 이벤트를 받으면 화면에 해당 메시지를 표시.
                .subscribe { message -> showError(message) }

        //저장소 정보를 보여주는 뷰의 표시 유무를 결정하는 이벤트를 구독.
        viewDisposables += viewModel.isContentVisible
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ visible -> setContentVisibility(visible) }

        //작업 진행 여부 이벤트를 구독한다.
        viewDisposables += viewModel.isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ isLoading ->
                    //작업 진행 여부 이벤트에 따라 프로그레스바의 표시 상태를 변경.
                    if(isLoading){
                        showProgress()
                    } else {
                        hideProgress()
                    }
                }

        val login = intent.getStringExtra(KEY_USER_LOGIN)
                ?: throw IllegalArgumentException("No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME)
                ?: throw IllegalArgumentException("No repo info exists in extras")

        //저장소 정보를 요청.
        disposable += viewModel.requestRepositoryInfo(login, repo)
    }

    /* onStop() 함수는 더 이상 오버라이드하지 않아도 된다.
    override fun onStop() {
        super.onStop()

        //관리하고 있던 디스포저블 객체를 모두 해제한다.
        //repoCall?.run{ cancel() } 대신 사용한다.
        disposable.clear()
    }*/

    private fun setContentVisibility(show: Boolean) {
        llActivityRepositoryContent.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showProgress() {
        pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbActivityRepository.visibility = View.GONE
    }

    private fun showError(message: String?) {
        //message가 널 값인 경우 "Unexpected error" 메시지를 표시한다.
        //with() 함수를 사용하여 에러 메시지를 표시하는 뷰 객체에 연속으로 접근하는 코드를 간략하게 표현한다.
        with(tvActivityRepositoryMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }
}