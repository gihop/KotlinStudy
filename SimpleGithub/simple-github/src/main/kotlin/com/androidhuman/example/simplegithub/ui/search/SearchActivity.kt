package com.androidhuman.example.simplegithub.ui.search

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.provideGithubApi
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.api.model.RepoSearchResponse
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {
    internal lateinit var menuSearch: MenuItem
    internal lateinit var searchView: SearchView

    //lazy 프로퍼티로 전환.
    internal val adapter by lazy {
        //apply() 함수를 사용하여 객체 생성과 함수 호출을 한번에 수행한다.
        SearchAdapter().apply { setItemClickListener(this@SearchActivity) }
    }
    internal val api by lazy { provideGithubApi(this) }

    //여러 디스포저블 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화한다.
    //var searchCall: Call<RepoSearchResponse>? = null 대신해서 사용한다.
    internal val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //인스턴스 선언 없이 뷰 ID를 사용하여 인스턴스에 접근한다.
        //with() 함수를 사용하여 rcActicitySearchList 범위 내에서 작업을 수행한다.
        with(rvActivitySearchList){
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = this@SearchActivity.adapter
        }
    }

    override fun onStop() {
        super.onStop()

        //관리하고 있던 디스포저블 객체를 모두 해제한다.
        //searchCall?.run{ cancel() } 대신 사용한다.
        disposables.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menu_activity_search_query)

        //menuSearch.actionView를 SearchView로 캐스팅한다.
        //SearchView.OnQueryTextListener 인터페이스를 구현하는 익명 클래스의 인스턴스를 생성한다.
        //apply() 함수를 사용하여 객체 생성과 리스너 지정을 동시에 수행한다.
        searchView = (menuSearch.actionView as SearchView).apply {
            setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String): Boolean {
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }

        //with() 함수를 사용하여 menuSearch 범위 내에서 작업을 수행한다.
        with(menuSearch){
            setOnActionExpandListener(object: MenuItem.OnActionExpandListener{
                override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                    return true;
                }

                override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                    if("" == searchView.query){
                        finish()
                    }
                    return true
                }
            })
            expandActionView()
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
        //Anko에서 제공하는 startActivity를 사용하면 부가 정보로 전달할 인자를 이 함수의 인자로 바로 전달할 수 있다.
        startActivity<RepositoryActivity>(
                RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    private fun searchRepository(query: String) {
        //REST API를 통해 검색 결과를 요청한다.
        disposables.add(api.searchRepository(query)

                //Observable 형태로 결과를 바꿔주기 위해 flatMap을 사용한다.
                .flatMap {
                    if(0 == it.totalCount){
                        //검색 결과가 없을 경우, 에러를 발생시켜 에러 메시지를 표시하도록 한다.
                        //곧바로 에러 블록이 실행된다.
                        Observable.error(IllegalStateException("No search result"))
                    }
                    else{
                        //검색 결과 리스트를 다음 스트림으로 전달한다.
                        Observable.just(it.items)
                    }
                }

                //이 이후에 수행되는 코드는 모두 메인 스레드에서 실행한다.
                //RxAndroid에서 제공하는 스케줄러인 AndroidSchedulers.mainThread()를 사용한다.
                .observeOn(AndroidSchedulers.mainThread())

                //구독할 때 수행할 작업을 수행한다.
                .doOnSubscribe {
                    clearResults()
                    hideError()
                    showProgress()
                }

                //스트림이 종료될 때 수행할 작업을 구현한다.
                .doOnTerminate { hideProgress() }

                //옵저버블을 구독한다.
                .subscribe({ items ->

                    //API를 통해 검색 결과를 정상적으로 받았을 때 처리할 작업을 구현한다.
                    //작업 중 오류가 발생하면 이 블록은 호출되지 않는다.
                    with(adapter){
                        setItems(items)
                        notifyDataSetChanged()
                    }
                }){
                    //에러 블록.
                    //네트워크 오류나 데이터 처리 오류 등 작업이 정상적으로 완료되지 않았을 때 호출한다.
                    showError(it.message)
                }
        )
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