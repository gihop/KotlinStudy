package com.androidhuman.example.simplegithub.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.api.GithubApiProvider
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.api.model.RepoSearchResponse
import com.androidhuman.example.simplegithub.ui.repo.RepositoryActivity
import com.androidhuman.example.simplegithub.ui.search.SearchAdapter.ItemClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), ItemClickListener {
    var rvList: RecyclerView? = null
    var progress: ProgressBar? = null
    var tvMessage: TextView? = null
    var menuSearch: MenuItem? = null
    var searchView: SearchView? = null
    var adapter: SearchAdapter? = null
    var api: GithubApi? = null
    var searchCall: Call<RepoSearchResponse>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        rvList = findViewById(R.id.rvActivitySearchList)
        progress = findViewById(R.id.pbActivitySearch)
        tvMessage = findViewById(R.id.tvActivitySearchMessage)
        adapter = SearchAdapter()
        adapter!!.setItemClickListener(this)
        rvList.setLayoutManager(LinearLayoutManager(this))
        rvList.setAdapter(adapter)
        api = GithubApiProvider.provideGithubApi(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menu_activity_search_query)
        searchView = menuSearch.getActionView() as SearchView
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        menuSearch.expandActionView()
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
        val intent = Intent(this, RepositoryActivity::class.java)
        intent.putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login)
        intent.putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name)
        startActivity(intent)
    }

    private fun searchRepository(query: String) {
        clearResults()
        hideError()
        showProgress()
        searchCall = api!!.searchRepository(query)
        searchCall.enqueue(object : Callback<RepoSearchResponse?> {
            override fun onResponse(call: Call<RepoSearchResponse?>,
                                    response: Response<RepoSearchResponse?>) {
                hideProgress()
                val searchResult = response.body()
                if (response.isSuccessful && null != searchResult) {
                    adapter!!.setItems(searchResult.items)
                    adapter!!.notifyDataSetChanged()
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
        val ab = supportActionBar
        if (null != ab) {
            ab.subtitle = query
        }
    }

    private fun hideSoftKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchView!!.windowToken, 0)
    }

    private fun collapseSearchView() {
        menuSearch!!.collapseActionView()
    }

    private fun clearResults() {
        adapter!!.clearItems()
        adapter!!.notifyDataSetChanged()
    }

    private fun showProgress() {
        progress!!.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress!!.visibility = View.GONE
    }

    private fun showError(message: String?) {
        tvMessage!!.text = message
        tvMessage!!.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvMessage!!.text = ""
        tvMessage!!.visibility = View.GONE
    }
}