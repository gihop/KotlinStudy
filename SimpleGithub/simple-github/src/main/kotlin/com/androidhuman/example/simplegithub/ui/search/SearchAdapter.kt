package com.androidhuman.example.simplegithub.ui.search

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.androidhuman.example.simplegithub.R
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import com.androidhuman.example.simplegithub.ui.GlideApp
import kotlinx.android.synthetic.main.activity_repository.view.*
import kotlinx.android.synthetic.main.item_repository.view.*
import java.util.*

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.RepositoryHolder>() {
    private var items: MutableList<GithubRepo> = ArrayList()
    private val placeholder = ColorDrawable(Color.GRAY)
    private var listener: ItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryHolder {
        return RepositoryHolder(parent)
    }

    override fun onBindViewHolder(holder: RepositoryHolder, position: Int) {
        //items.get(position) 대신 배열 인덱스 접근 연산자를 사용한다.
        val repo = items[position]
        //with() 함수를 사용하여 holder.itemView를 여러번 호출하지 않도록 한다.
        with(holder.itemView) {
            GlideApp.with(context)
                    .load(repo.owner.avatarUrl)
                    .placeholder(placeholder)
                    .into(ivActivityRepositoryProfile)
            tvItemRepositoryName.text = repo.fullName
            tvItemRepositoryLanguage.text = if (TextUtils.isEmpty(repo.language))
                context.getText(R.string.no_language_specified)
            else repo.language

            //View.OnClickListener의 본체를 람다 표현식으로 작성한다.
            setOnClickListener {
                if (null != listener) {
                    listener!!.onItemClick(repo)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: List<GithubRepo>) {
        //인자로 받은 리스트의 형태를 어댑터 내부에서 사용하는 리스트 형태(내부 자료 변경이 가능한 형태)로 변환해줘야 한다.
        this.items = items.toMutableList()
    }

    fun setItemClickListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    fun clearItems() {
        items.clear()
    }

    class RepositoryHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repository, parent, false))

    interface ItemClickListener {
        fun onItemClick(repository: GithubRepo?)
    }
}