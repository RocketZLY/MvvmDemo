package com.boohee.one.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import rocketly.mvvmdemo.utils.databinding.ErrorEvent

/**
 * Created by zhuliyuan on 2018/8/8.
 */
open class BaseVM : ViewModel() {

    val isFirstLoading = MutableLiveData<Boolean>()

    val isLoadingMore = MutableLiveData<Boolean>()

    val isRefreshing = MutableLiveData<Boolean>()

    val isEmpty = MutableLiveData<Boolean>()

    val isError = MutableLiveData<ErrorEvent>()

    open fun onRefresh() {
        isRefreshing.value = true
    }

    open fun onLoadMore() {
        isLoadingMore.value = true
    }

    open fun onFirstLoad() {
        isFirstLoading.value = true
    }

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    /**
     * 重置加载状态
     */
    fun resetLoadStatus() {
        if (isFirstLoading.value == true) {
            isFirstLoading.value = false
        }
        if (isLoadingMore.value == true) {
            isLoadingMore.value = false
        }
        if (isRefreshing.value == true) {
            isRefreshing.value = false
        }
    }
}