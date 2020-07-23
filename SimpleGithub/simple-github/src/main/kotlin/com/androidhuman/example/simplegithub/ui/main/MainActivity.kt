package com.androidhuman.example.simplegithub.ui.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.ui.search.SearchActivity

class MainActivity : AppCompatActivity() {
    var btnSearch: FloatingActionButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSearch = findViewById(R.id.btnActivityMainSearch)
        btnSearch.setOnClickListener(View.OnClickListener { startActivity(Intent(this@MainActivity, SearchActivity::class.java)) })
    }
}