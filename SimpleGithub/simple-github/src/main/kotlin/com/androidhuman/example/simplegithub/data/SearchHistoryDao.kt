package com.androidhuman.example.simplegithub.data

import android.arch.persistence.room.*
import com.androidhuman.example.simplegithub.api.model.GithubOwner
import com.androidhuman.example.simplegithub.api.model.GithubRepo
import io.reactivex.Flowable

@Dao
interface SearchHistoryDao {
    //데이터베이스에 저장소를 추가한다.
    //이미 저장된 항목이 있을 경우 데이터를 덮어쓴다.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(repo: GithubRepo)

    //저장되어 있는 저장소 목록을 반환한다.
    //Flowable 형태의 자료를 반환하므로, 데이터베이스가 변경되면 알림을 받아 새로운 자료를 가져온다.
    //따라서 항상 쵯신 자료를 유지한다.
    @Query("SELECT * FROM repositories")
    fun getHistory(): Flowable<List<GithubRepo>>

    //repositories 테이블의 모든 데이터를 삭제한다.
    @Query("DELETE FROM repositories")
    fun clearAll()
}