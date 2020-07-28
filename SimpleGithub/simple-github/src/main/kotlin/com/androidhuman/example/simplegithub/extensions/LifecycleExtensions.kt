package com.androidhuman.example.simplegithub.extensions

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver

//Lifecycle를 통해 생명주기 이벤트를 추가하려면 addObserver() 함수를 사용하여 생명주기를 관찰할 LifecycleObserver를 등록해야 한다.
//이를 쉽게 사용하기 위해 Lifecycle 클래스의 '+=' 연산자를 오버로딩한다.
operator fun Lifecycle.plusAssign(observer: LifecycleObserver) = this.addObserver(observer)