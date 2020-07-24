package com.androidhuman.example.simplegithub.ui.search

import android.content.Intent
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

    //널 값을 허용하도록 한 후, 초기값을 명시적으로 null로 지정한다.
    internal var searchCall: Call<RepoSearchResponse>? = null

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
        //액티비티가 화면에서 사라지는 시점에 API 호출 객체가 생성되어 있다면 API 요청을 취소한다.
        searchCall?.run { cancel() }
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
        clearResults()
        hideError()
        showProgress()
        searchCall = api.searchRepository(query)

        //Call 인터페이스를 구현하는 익명 클래스의 인스턴스를 생성한다.
        //앞에서 API 호출에 필요한 객체를 받았으므로, 이 시점에서 searchCall 객체의 값은 널이 아니다.
        searchCall!!.enqueue(object : Callback<RepoSearchResponse?> {
            override fun onResponse(call: Call<RepoSearchResponse?>,
                                    response: Response<RepoSearchResponse?>) {
                hideProgress()
                val searchResult = response.body()
                if (response.isSuccessful && null != searchResult) {
                    //with() 함수를 사용하여 adpater 범위 내에서 작업을 수행한다.
                    with(adapter) {
                        setItems(searchResult.items)
                        notifyDataSetChanged()
                    }
                    if (0 == searchResult.totalCount) {
                        showError(getString(R.string.no_search_result))
                    }
                } else {
                    showError("Not successful: " + response.message())
                }
            }

            override fun onFailure(call: Call<RepoSearchResponse?>, t: Throwable) {
                hideProgress()
                showError(t.message)
            }
        })
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