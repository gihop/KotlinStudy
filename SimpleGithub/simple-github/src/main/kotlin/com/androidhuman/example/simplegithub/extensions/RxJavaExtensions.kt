package com.androidhuman.example.simplegithub.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

//CompositeDisposable에 디스포저블을 추가하기 위해 CompositeDisposable.add() 함수를 사용했는데
//add() 함수를 사용하는 대신 += 연산자를 사용하여 CompositeDisposable 객체에 디스포저블 객체를 추가하도록 한다면
//더 직관적인 코드를 작성할 수 있다.
//CompositeDisposable의 '+=' 연산자 뒤에 Disposable 타입이 오는 경우를 재정의한다.
operator fun CompositeDisposable.plusAssign(disposable: Disposable){
    //CompositeDisposable.add() 함수를 호출한다.
    this.add(disposable)
}