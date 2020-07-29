package com.androidhuman.example.simplegithub.ui.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.data.providerSearchHistoryDao
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.extensions.runOnIoScheduler
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

//SearchAdapter.ItemClickListener 인터페이스를 구현한다.
class MainActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {
    //어댑터 프로퍼티를 추가한다.
    internal val adapter by lazy{
        SearchAdapter().apply { setItemClickListener(this@MainActivity) }
    }

    //최근 조회한 저장소를 담당하는 데이터 접근 객체 프로퍼티를 추가한다.
    internal val searchHistoryDao by lazy { providerSearchHistoryDao(this) }

    //디스포저블을 관리하는 프로퍼티를 추가한다.
    internal val disposables = AutoClearedDisposable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //생명주기 이벤트 옵저버를 등록한다.
        lifecycle += disposables
        lifecycle += object: LifecycleObserver{
            //onStart() 콜백 함수가 호출되면 fetchSearchHistory() 함수를 호출한다.
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun fetch(){
                fetchSearchHistory()
            }
        }

        //인스턴스 선언 없이 뷰 ID를 사용하여 인스턴스에 접근한다.
        btnActivityMainSearch.setOnClickListener(View.OnClickListener {
            //단순히 호출할 액티비티만을 명시하는 코드는 다음과 같이 간략하게 표현할 수 있다.
            startActivity<SearchActivity>()
        })

        //리사이클러뷰에 어댑터를 설정한다.
        with(rvActivityMainList){
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    //선택한 항목의 정보를 토대로 RepositoryActivity를 실행한다.
    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    //데이터베이스에 저장되어 있는 저장소 목록을 불러오는 작업을 반환한다.
    //SearchHistoryDao.getHistory() 함수는 Flowable 형태로 데이터를 전달하므로,
    //데이터베이스에 저장된 자료가 바뀌면 즉시 업데이트된 정보가 새로 전달된다.
    private fun fetchSearchHistory(): Disposable
            = searchHistoryDao.getHistory()

            //메인 스레드에서 호출하면 Room에서 오류를 발생시키므로 IO 스레드에서 작업을 수행한다.
            .subscribeOn(Schedulers.io())

            //결과를 받아 뷰에 업데이트해야 하므로 메인 스레드(UI 스레드)에서 결과를 처리한다.
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->

                //어댑터를 갱신한다.
                with(adapter){
                    setItems(items)
                    notifyDataSetChanged()
                }

                //저장된 데이터의 유무에 따라 오류 메시지를 표시하거나 감춘다.
                if(items.isEmpty()){
                    showMessage(getString(R.string.no_recent_repositories))
                }
                else{
                    hideMessage()
                }
            }){
                showMessage(it.message)
            }

    private fun clearAll(){
        //메인 스레드에서 실행하면 오류가 발생하므로,
        //앞에서 작성한 runOnIoScheduler() 함수를 사용하여 IO 스레드에서 작업을 실행한다.
        disposables += runOnIoScheduler { searchHistoryDao.clearAll() }
    }

    private fun showMessage(message: String?){
        with(tvActivityMainMessage){
            text = message ?: "Unexpected error"
            visibility = View.VISIBLE
        }
    }

    private fun hideMessage(){
        with(tvActivityMainMessage){
            text = ""
            visibility = View.GONE
        }
    }

    //앞에서 생성한 메뉴 리소스를 액티비티의 메뉴로 표시하도록 한다.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //'Clear all' 메뉴를 선택하면 조회했던 저장소 기록을 모두 삭제한다.
        if(R.id.menu_activity_main_clear_all == item.itemId){
            clearAll()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}