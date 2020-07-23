package com.androidhuman.example.simplegithub.ui.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.ui.search.SearchActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //인스턴스 선언 없이 뷰 ID를 사용하여 인스턴스에 접근한다.
        btnActivityMainSearch.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@MainActivity, SearchActivity::class.java))
        })
    }
}