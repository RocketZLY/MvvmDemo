package rocketly.mvvmdemo

import android.app.Application
import android.content.Context

/**
 * Created by zhuliyuan on 2018/8/15.
 */
class MvvmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        @JvmField
        var context: Context? = null

        @JvmStatic
        fun getContext(): Context? = context
    }

}