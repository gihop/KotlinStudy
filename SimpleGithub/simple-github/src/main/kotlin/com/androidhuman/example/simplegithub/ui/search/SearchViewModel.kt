package com.androidhuman.example.simplegithub.ui.search

import android.arch.lifecycle.ViewModel
import com.androidhuman.example.simplegithub.api.GithubApi
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.extensions.runOnIoScheduler
import com.androidhuman.example.simplegithub.util.SupportOptional
import com.androidhuman.example.simplegithub.util.emptyOptional
import com.androidhuman.example.simplegithub.util.optionalOf
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

//생성자의 인자로 API 및 데이터베이스 접근에 필요한 인스턴스를 받는다.
class SearchViewModel(
        val api: GithubApi,
        val searchHistoryDao: SearchHistoryDao)
    : ViewModel() {
    //검색 결과를 전달할 서브젝트. 초기값으로 빈 값을 지정.
    val searchResult: BehaviorSubject<SupportOptional<List<GithubRepo>>>
            = BehaviorSubject.createDefault(emptyOptional())

    //마지막 검색어를 전달할 서브젝트. 초기값으로 빈 값을 지정.
    val lastSearchKeyword: BehaviorSubject<SupportOptional<String>>
            = BehaviorSubject.createDefault(emptyOptional())

    //화면에 표시할 메시지를 전달할 서브젝트.
    val message: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.create()

    //작업 진행 상태를 전달할 서브젝트. 초기값으로 false 지정.
    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    //검색 결과를 요청한다.
    fun searchRepository(query: String): Disposable
            = api.searchRepository(query)

            //검색어를 lastSearchKeyword 서브젝트에 전달.
            .doOnNext { lastSearchKeyword.onNext(optionalOf(query)) }
            .flatMap {
                if(0 == it.totalCount){
                    Observable.error(IllegalStateException("No Search result"))
                } else {
                    Observable.just(it.items)
                }
            }

            //검색을 시작하지 전에, 현재 화면에 표시되고 있던 검색 결과 및 메시지를 모두 제거.
            //작업 진행 상태를 true로 변경.
            .doOnSubscribe {
                searchResult.onNext(emptyOptional())
                message.onNext(emptyOptional())
                isLoading.onNext(true)
            }

            //작업이 종료되면(정상 종료, 오류 모두 포함) 작업 진행 상태를 false로 변경.
            .doOnTerminate { isLoading.onNext(false) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                //검색 결과를 searchResult 서브젝트로 전달.
                searchResult.onNext(optionalOf(items))
            }){
                //에러가 발생한 경우 message 서브젝트를 통해 에러 메시지를 전달.
                message.onNext(optionalOf(it.message ?: "Unexpected error"))
            }

    //데이터베이스에 저장소 정보를 추가.
    fun addToSearchHistory(repository: GithubRepo): Disposable
            = runOnIoScheduler { searchHistoryDao.add(repository) }
}