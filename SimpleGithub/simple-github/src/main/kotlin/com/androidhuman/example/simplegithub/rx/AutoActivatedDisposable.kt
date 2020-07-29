package com.androidhuman.example.simplegithub.rx

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable

class AutoActivatedDisposable (
    //생명주기 이벤트를 받을 인스턴스.
    private val lifecycleOwner: LifecycleOwner,

    //이벤트를 받을 디스포저블 객체를 만드는 함수.
    private val func: () -> Disposable)
    :LifecycleObserver{

    private var disposable: Disposable? = null

    //onStart() 콜백 함수가 호출되면 activate() 함수를 실행한다.
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun activate(){
        //디스포저블로부터 이벤트를 받기 시작한다.
        disposable = func.invoke()
    }

    //onStop() 콜백 함수가 호출되면 deactivate() 함수를 실행한다.
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun deactivate(){
        //생명주기 이벤트를 더 이상 받지 않도록 옵저버에서 제거한다.
        lifecycleOwner.lifecycle.removeObserver(this)
    }
}