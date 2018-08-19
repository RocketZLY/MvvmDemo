package rocketly.mvvmdemo.utils.databinding

import android.databinding.BindingAdapter
import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import android.text.method.TextKeyListener.clear
import android.view.View
import android.widget.TextView
import me.drakeet.multitype.MultiTypeAdapter
import rocketly.mvvmdemo.utils.funcextend.OnAvoidMultipleClickListener
import rocketly.mvvmdemo.utils.funcextend.emptyProcess
import rocketly.mvvmdemo.utils.funcextend.setOnAvoidMultipleClickListener
import java.util.Collections.addAll

/**
 * Created by zhuliyuan on 2018/8/10.
 */
@BindingAdapter("multiTypeItem")
fun setItem(rv: RecyclerView, list: List<Any>?) {
    if (rv.adapter == null || rv.adapter !is MultiTypeAdapter || list == null) return
    var oldSize = 0
    ((rv.adapter as MultiTypeAdapter).items as MutableList<Any>).apply {
        oldSize = size
        clear()
        addAll(list)
    }
    when {
        oldSize < list.size -> rv.adapter.notifyItemRangeInserted(oldSize, list.size - oldSize)
        oldSize > list.size -> rv.adapter.notifyItemRangeRemoved(list.size, oldSize - list.size)
        else -> rv.adapter.notifyItemRangeChanged(0, oldSize)
    }
}

@BindingAdapter("android:text")
fun setText(tv: TextView, text: String?) {
    tv.text = text.emptyProcess()
}

@BindingAdapter("android:onClick")
fun setOnAvoidMultipleClickListener(v: View, listener: OnAvoidMultipleClickListener) {
    v.setOnAvoidMultipleClickListener(listener)
}