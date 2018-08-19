package rocketly.mvvmdemo.utils.funcextend

import android.text.TextUtils

/**
 * Created by zhuliyuan on 2018/4/19.
 */
fun String?.emptyProcess(): String {
    return if (TextUtils.isEmpty(this)) "" else this!!
}