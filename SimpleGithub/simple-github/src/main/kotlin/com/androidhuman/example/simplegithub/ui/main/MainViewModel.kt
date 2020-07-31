package com.androidhuman.example.simplegithub.ui.main

import android.arch.lifecycle.ViewModel
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.data.SearchHistoryDao
import com.androidhuman.example.simplegithub.extensions.runOnIoScheduler
import com.androidhuman.example.simplegithub.util.SupportOptional
import com.androidhuman.example.simplegithub.util.emptyOptional
import com.androidhuman.example.simplegithub.util.optionalOf
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

//MainViewModel에서 저장소 조회 기록 데이터베이스에 접근해야 하므로 SearchHistoryDao를 생성자의 인자로 받는다.
class MainViewModel(val searchHistoryDao: SearchHistoryDao): ViewModel() {
    //데이터베이스에 저장되어 있는 저장소 조회 기록을 Flowable 형태로 제공한다.
    //searchHistory 자체가 값을 갖지 않고, searchHistoryDao를 통해 데이터를 가져오므로
    //지원 프로퍼티(backing property) 형태로 선언한다.
    val searchHisory: Flowable<SupportOptional<List<GithubRepo>>>
    get() = searchHistoryDao.getHistory()

            //SupportOptional 형태로 데이터를 감싸준다.
            .map { optionalOf(it) }

            //매 이벤트가 발생할 때마다 함수 블록을 호출한다.
            .doOnNext { optional ->
                if(optional.value.isEmpty()){
                    //표시할 데이터가 없는 경우, message 서브젝트를 통해 표시할 메시지를 전달한다.
                    message.onNext(optionalOf("No recent repositories."))
                } else {
                    //데이터가 있는 경우, 메시지를 표시하지 않는다.
                    message.onNext(emptyOptional())
                }
            }

            //에러가 발생했을 때 실행할 함수 블록을 정의한다.
            .doOnError{
                //에러 메시지를 message 서브젝트를 통해 전달한다.
                message.onNext(optionalOf(it.message ?: "Unexpected error"))
            }

            //에러가 발생한 경우 빈 데이터를 반환한다.
            .onErrorReturn{ emptyOptional() }

    //메시지를 전달할 서브젝트.
    val message: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.create()

    //데이터베이스에 저장된 저장소 조회 기록을 모두 삭제한다.
    fun clearSearchHistory(): Disposable = runOnIoScheduler { searchHistoryDao.clearAll() }
}