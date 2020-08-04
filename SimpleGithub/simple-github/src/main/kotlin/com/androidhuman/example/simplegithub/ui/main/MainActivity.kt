package com.androidhuman.example.simplegithub.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.rx.AutoActivatedDisposable
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import javax.inject.Inject

//SearchAdapter.ItemClickListener 인터페이스를 구현한다.
//AppCompatActivity 대신 DaggerAppCompatActivity를 상속한다.
class MainActivity : DaggerAppCompatActivity(), SearchAdapter.ItemClickListener {
    //디스포저블을 관리하는 프로퍼티를 추가한다.
    internal val disposables = AutoClearedDisposable(this)

    //액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가한다.
    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    //뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언한다.
    lateinit var viewModel: MainViewModel

    @Inject lateinit var adapter: SearchAdapter

    @Inject lateinit var viewModelFactory: MainViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //MainViewModel의 인스턴스를 받는다.
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]

        //생명주기 이벤트 옵저버를 등록한다.
        lifecycle += disposables

        //viewDisposables에서 이 액티비티의 생명주기 이벤트를 받도록 한다.
        lifecycle += viewDisposables

        //액티비티가 활성 상태일 때만 데이터베이스에 저장된 저장소 조회 기록을 받도록 한다.
        lifecycle += AutoActivatedDisposable(this){
            viewModel.searchHisory
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{ items ->
                        with(adapter){
                            if(items.isEmpty){
                                clearItems()
                            } else {
                                setItems(items.value)
                            }
                            notifyDataSetChanged()
                        }
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

        //메시지 이벤트를 구독한다.
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ message ->
                    if(message.isEmpty){
                        //빈 메시지를 받은 경우 표시되고 있는 메시지를 화면에서 숨긴다.
                        hideMessage()
                    } else {
                        //유효한 메시지를 받은 경우 화면에 메시지를 표시한다.
                        showMessage(message.value)
                    }
                }
    }

    //선택한 항목의 정보를 토대로 RepositoryActivity를 실행한다.
    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //'Clear all' 메뉴를 선택하면 조회했던 저장소 기록을 모두 삭제한다.
        if(R.id.menu_activity_main_clear_all == item.itemId){
            //데이터베이스에 저장된 저장소 조회 기록 데이터를 모두 삭제한다.
            disposables += viewModel.clearSearchHistory()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}