package com.androidhuman.example.simplegithub.ui.repo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.provideGithubApi
import com.androidhuman.example.simplegithub.ui.GlideApp
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_repository.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RepositoryActivity : AppCompatActivity() {
    //정적 필드로 정의되어 있던 항목은 동반 객체 내부에 정의된다.
    //액티비티 호출 시 필요한 데이터를 전달할 때 사용하는 키 값들을 정의한 동반 객체 내 프로퍼티는 클래스 가장 위로 위치를
    //옮겨주고, 각 프로퍼티에 const 키워드를 추가한다.
    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    //lazy 프로퍼티로 전환한다.
    internal val api by lazy { provideGithubApi(this) }

    //여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화한다.
    //var repoCall: Call<GithubRepo>? = null 대신 사용한다.
    internal val disposable = CompositeDisposable()

    //두 프로퍼티는 객체를 한번 생성하고 나면 이후에 변경할 일이 없기 때문에 변수가 아닌 값으로 바꿔준다.
    internal val dateFormatInResponse = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
    internal val dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        val login = intent.getStringExtra(KEY_USER_LOGIN)
                ?: throw IllegalArgumentException("No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME)
                ?: throw IllegalArgumentException("No repo info exists in extras")
        showRepositoryInfo(login, repo)
    }

    override fun onStop() {
        super.onStop()

        //관리하고 있던 디스포저블 객체를 모두 해제한다.
        //repoCall?.run{ cancel() } 대신 사용한다.
        disposable.clear()
    }

    private fun showRepositoryInfo(login: String, repoName: String) {
        //REST API를 통해 저장소 정보를 요청한다.
        disposable.add(api.getRepository(login, repoName)
                //이 이후에 수행되는 코드는 모두 메인 스레드에서 실행한다.
                .observeOn(AndroidSchedulers.mainThread())

                //구독할 때 수행할 작업을 구현한다.
                .doOnSubscribe { showProgress() }

                //에러가 발생했을 때 수행할 작업을 구현한다.
                .doOnError{ hideProgress(false) }

                //스트림이 정상 종료되었을 때 수행할 작업을 구현한다.
                .doOnComplete{ hideProgress(true) }

                //옵저버블을 구독한다.
                .subscribe({ repo ->
                    //API를 통해 저장소 정보를 정상적으로 받았을 때 처리할 작업을 구현한다.
                    //작업 중 오류가 발생하면 이 블록은 호출되지 않는다.
                    GlideApp.with(this@RepositoryActivity)
                            .load(repo.owner.avatarUrl)
                            .into(ivActivityRepositoryProfile)

                    tvActivityRepositoryName.text = repo.fullName
                    tvActivityRepositoryStars.text = resources.getQuantityString(R.plurals.star, repo.stars)

                    if(null == repo.description){
                        tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                    }
                    else{
                        tvActivityRepositoryDescription.text = repo.description
                    }

                    if(null == repo.language){
                        tvActivityRepositoryLanguage.setText(R.string.no_description_provided)
                    }
                    else{
                        tvActivityRepositoryLanguage.text = repo.language
                    }

                    try{
                        val lastUpdate = dateFormatInResponse.parse(repo.updatedAt)
                        tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(lastUpdate)
                    }catch (e: ParseException){
                        tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                    }
                }) {
                    //에러 블록.
                    //네트워크 오류나 데이터 처리 오류 등 작업이 정상적으로 완료되지 않았을 때 호출된다.
                    showError(it.message)
                })
    }

    private fun showProgress() {
        llActivityRepositoryContent.visibility = View.GONE
        pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress(isSucceed: Boolean) {
        llActivityRepositoryContent.visibility = if (isSucceed) View.VISIBLE else View.GONE
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