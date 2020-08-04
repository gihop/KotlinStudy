package com.androidhuman.example.simplegithub.ui.search

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.extensions.plusAssign
import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import com.jakewharton.rxbinding2.support.v7.widget.queryTextChangeEvents
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.startActivity
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

//AppCompatActivity 대신 DaggerAppCompatActivity를 상속한다.
class SearchActivity : DaggerAppCompatActivity(), SearchAdapter.ItemClickListener {
    internal lateinit var menuSearch: MenuItem
    internal lateinit var searchView: SearchView

    //여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화한다.
    //var searchCall: Call<RepoSearchResponse>? = null 대신해서 사용한다.
    //CompositeDisposable에서 AutoClearedDisposable로 변경한다.
    internal val disposables = AutoClearedDisposable(this)

    //viewDisposables 프로퍼티를 추가하여 뷰 이벤트의 디스포저블을 별도로 관리한다.
    //CompositeDisposable에서 AutoClearedDisposable로 변경한다.
    //viewDisposables는 onStop() 콜백 함수가 호출되더라도 액티비티가 종료되는 시점에만 관리하고 있는 디스포저블을 해제하므로,
    //alwaysClearOnStop 프로퍼티를 false로 생성한 생성자를 사용한다.
    //액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가한다.
    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    //뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언.
    lateinit var viewModel: SearchViewModel

    //대거로부터 SearchAdapter 객체를 주입받는다.
    @Inject lateinit var adapter: SearchAdapter

    //대거로부터 SearchViewModelFactory 객체를 주입받는다.
    @Inject lateinit var viewModelFactory: SearchViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //SearchViewModel의 인스턴스를 받는다.
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SearchViewModel::class.java]

        //Lifecycle.addObserver() 함쑤를 사용하여 각 객체를 옵저버로 등록한다.
        lifecycle += disposables
        lifecycle += viewDisposables

        //인스턴스 선언 없이 뷰 ID를 사용하여 인스턴스에 접근한다.
        //with() 함수를 사용하여 rcActicitySearchList 범위 내에서 작업을 수행한다.
        with(rvActivitySearchList){
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
        }

        //검색 결과 이벤트를 구독한다.
        viewDisposables += viewModel.searchResult
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ items ->
                    with(adapter){
                        if(items.isEmpty){
                            //빈 이벤트를 받으면 표시되고 있던 항목을 제거.
                            clearItems()
                        } else {
                            //유효한 이벤트를 받으면 데이터를 화면에 표시.
                            setItems(items.value)
                        }
                        notifyDataSetChanged()
                    }
                }

        //메시지 이벤트를 구독한다.
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ message ->
                    if(message.isEmpty){
                        //빈 이벤트를 ㅂ다으면 화면에 표시되고 있던 메시지를 숨긴다.
                        hideError()
                    } else {
                        //유효한 이벤트를 받으면 화면에 메시지를 표시.
                        showError(message.value)
                    }
                }

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
    }

    /*onStop() 함숫는 더 이상 오버라이드 하지 않아도 된다.
    override fun onStop() {
        super.onStop()

        //관리하고 있던 디스포저블 객체를 모두 해제한다.
        //searchCall?.run{ cancel() } 대신 사용한다.
        disposables.clear()

        //액티비티가 완전히 종료되고 있는 경우에만 관리하고 있는 디스포저블을 해제한다.
        //화면이 꺼지거나 다른 액티비티를 호출하여 액티비티가 화면에서 사라지는 경우에는 해제하지 않는다.
        if(isFinishing){
            viewDisposables.clear()
        }
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)

        menuSearch = menu.findItem(R.id.menu_activity_search_query)
        searchView = (menuSearch.actionView as SearchView)

        //SearchView에서 발생하는 이벤트를 옵저버블 형태로 받는다.
        //코틀린 확장 라이브러리를 적용하여 RxBinding에서 제공하는 함수를 각 UI 위젯의 인스턴스에서 직접 호출할 수 있다.
        viewDisposables += searchView.queryTextChangeEvents()

                //검색을 수행했을 때 발생한 이벤트만 받는다.
                .filter { it.isSubmitted }

                //이벤트에서 검색어 텍스트(CharSequence)를 추출한다.
                .map { it.queryText() }

                //빈 문자열이 아닌 검색어만 받는다.
                .filter { it.isNotEmpty() }

                //검색어를 String 형태로 변환한다.
                .map{ it.toString() }

                //이 이후에 수행되는 코드는 모두 메인 스레드에서 실행한다.
                //RxAndroid에서 제공하는 스케줄러인 AndroidSchedulers.mainThread()를 사용한다.
                .observeOn(AndroidSchedulers.mainThread())

                //옵저버블을 구독한다.
                .subscribe{ query ->
                    //검색 절차를 수행한다.
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                }

        //마지막으로 검색한 검색어 이벤트를 구독한다.
        viewDisposables += viewModel.lastSearchKeyword
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { keyword ->
                    if(keyword.isEmpty){
                        //아직 검색을 수행하지 않은 경우 SearchView를 펼친 상태로 유지.
                        menuSearch.expandActionView()
                    } else {
                        //검색어가 있는 경우 해당 검색어를 액티비티의 제목으로 표시.
                        updateTitle(keyword.value)
                    }
                }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.menu_activity_search_query == item.itemId) {
            item.expandActionView()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        //선택한 저장소 정보를 데이터베이스에 추가.
        disposables += viewModel.addToSearchHistory(repository)
        //Anko에서 제공하는 startActivity를 사용하면 부가 정보로 전달할 인자를 이 함수의 인자로 바로 전달할 수 있다.
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    private fun searchRepository(query: String) {
        //전달받은 검색어로 검색 결과를 요청.
        disposables += viewModel.searchRepository(query)
    }

    private fun updateTitle(query: String) {
        //별도의 변수 선언 없이, getSupportActionBar()의 반환값이 널이 아닌 경우에만 작업을 수행한다.
        supportActionBar?.run { subtitle = query }
    }

    private fun hideSoftKeyboard() {
        //별도의 변수 없이 획득한 인스턴스의 범위 내에서 작업을 수행한다.
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).run{
            hideSoftInputFromWindow(searchView.windowToken, 0)
        }
    }

    private fun collapseSearchView() {
        menuSearch.collapseActionView()
    }

    private fun clearResults() {
        //with() 함수를 사용하여 adapter 범위 내에서 작업을 수행한다.
        with(adapter){
            clearItems()
            notifyDataSetChanged()
        }
    }

    private fun showProgress() {
        pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String?) {
        //message가 널 값인 경우 "Unexpected error." 메시지를 표시한다.
        //with() 함수를 사용하여 tvActivitySearchMessage 범위 내에서 작업을 수행한다.
        with(tvActivitySearchMessage){
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        //with() 함수를 사용하여 tvActivitySearchMessage 범위 내에서 작업을 수행한다.
        with(tvActivitySearchMessage){
            text = ""
            visibility = View.GONE
        }
    }
}