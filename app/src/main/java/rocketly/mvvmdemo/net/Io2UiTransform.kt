package rocketly.mvvmdemo.net

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by zhuliyuan on 2018/8/16.
 */
class Io2UiTransform<T> : ObservableTransformer<T, T> {
    override fun apply(upstream: Observable<T>): ObservableSource<T> = upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())

}