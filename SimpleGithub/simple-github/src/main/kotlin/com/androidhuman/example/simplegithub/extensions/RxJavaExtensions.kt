package com.androidhuman.example.simplegithub.extensions

import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

//AutoClearedDisposable에 디스포저블 객체를 쉽게 추가하기 위해 += 연산자를 오버로딩한다.
operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) = this.add(disposable)

//데이터베이스에 저장소를 추가한다.
//데이터 조작 코드를 메인 스레드에서 호출하면 에러가 발생하므로,
//RxJava의 Completable을 사용하여 IO 스레드에서 데이터 추가 작업을 수행하도록 한다.
fun runOnIoScheduler(func: () -> Unit): Disposable
        = Completable.fromCallable(func)
        .subscribeOn(Schedulers.io())
        .subscribe()
//Completable은 옵저버블의 한 종류이며, 일반적인 Observable과 달리 이벤트 스트림에 자료를 전달하지 않는다.
//따라서 SearchHistoryDao.add() 함수처럼 반환하는 값이 없는 작업을 옵저버블 형태로 표현할 때 유용하다.