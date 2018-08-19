package rocketly.mvvmdemo.utils.funcextend

import android.view.View
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by zhuliyuan on 2018/7/3.
 */

/**
 * 防止多次点击click listener
 */
fun View.setOnAvoidMultipleClickListener(listener: (View) -> Unit) {
    getAvoidMultipleClickObservable().subscribe {
        listener(it)
    }
}

fun View.setOnAvoidMultipleClickListener(listener: OnAvoidMultipleClickListener?) {
    getAvoidMultipleClickObservable().subscribe {
        listener?.avoidMultipleClickListener(it)
    }
}

fun View.getAvoidMultipleClickObservable(): Observable<View> =
        Observable.create<View> { emit ->
            this.setOnClickListener {
                emit.onNext(it)
            }
        }.throttleFirst(500, TimeUnit.MILLISECONDS)

interface OnAvoidMultipleClickListener {
    fun avoidMultipleClickListener(v: View)
}