package rocketly.mvvmdemo.net

import android.widget.Toast
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.Response.success
import rocketly.mvvmdemo.MvvmApplication

/**
 * Created by Rocket on 2018/8/18.
 */
class ApiObserver<T>(val start: (d: Disposable) -> Unit = {},
                     val error: (e: Throwable) -> Unit = {},
                     val finish: () -> Unit = {},
                     val success: (T) -> Unit = {}
) : Observer<T> {
    override fun onSubscribe(d: Disposable) {
        start(d)
    }

    override fun onError(e: Throwable) {
        Toast.makeText(MvvmApplication.getContext(), handleError(e), Toast.LENGTH_SHORT).show()
        error(e)
    }

    override fun onComplete() {
        finish()
    }


    override fun onNext(t: T) {
        success(t)
    }
}