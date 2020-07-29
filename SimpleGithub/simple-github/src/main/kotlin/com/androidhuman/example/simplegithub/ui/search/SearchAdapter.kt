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
    //ArrayList()를 사용하여 빈 MutableList를 할당하는 것보다 mutalbleListOf() 함수를 사용하여 mutalbleList를
    //초기화 하는것이 의도가 좀 더 명확하다.
    private var items: MutableList<GithubRepo> = mutableListOf()
    private val placeholder = ColorDrawable(Color.GRAY)
    private var listener: ItemClickListener? = null

    //항상 RepositoryHolder 객체만 반환하므로 단일 표현식으로 표현할 수 있다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RepositoryHolder(parent)

    override fun onBindViewHolder(holder: RepositoryHolder, position: Int) {
        //items.get(position) 대신 배열 인덱스 접근 연산자를 사용한다.
        //뷰에 표시할 데이터를 참조하기 위한 repo는, 뷰에 데이터를 반영하기 위해 사용될 뿐, 다른 곳에서는 사용하지 않는다.
        //따라서 let()를 사용하여 값이 사용되는 범위를 명시적으로 한정한다.
        items[position].let { repo ->
            //with() 함수를 사용하여 holder.itemView를 여러번 호출하지 않도록 한다.
            with(holder.itemView) {
                GlideApp.with(context)
                        .load(repo.owner.avatarUrl)
                        .placeholder(placeholder)
                        .into(ivItemRepositoryProfile)
                tvItemRepositoryName.text = repo.fullName
                tvItemRepositoryLanguage.text = if (TextUtils.isEmpty(repo.language))
                    context.getText(R.string.no_language_specified)
                else repo.language

                //View.OnClickListener의 본체를 람다 표현식으로 작성한다.
                setOnClickListener { listener?.onItemClick(repo) }
            }
        }
    }

    //항상 리스트 크기만을 반환하므로 이 함수 또한 단일 표현식으로 표현할 수 있다.
    override fun getItemCount() = items.size


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
        fun onItemClick(repository: GithubRepo)
    }
}