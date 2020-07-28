package com.androidhuman.example.simplegithub.extensions

import com.androidhuman.example.simplegithub.rx.AutoClearedDisposable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

//AutoClearedDisposable에 디스포저블 객체를 쉽게 추가하기 위해 += 연산자를 오버로딩한다.
operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) = this.add(disposable)