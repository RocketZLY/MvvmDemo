package rocketly.mvvmdemo.utils.funcextend

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.annotation.NonNull

/**
 * Created by zhuliyuan on 2018/8/14.
 */
fun <T> MutableLiveData<T>.observe(@NonNull owner: LifecycleOwner, @NonNull method: (T?) -> Unit) {
    observe(owner, Observer {
        method(it)
    })
}