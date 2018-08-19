package rocketly.mvvmdemo.utils.databinding

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

/**
 * Created by zhuliyuan on 2018/8/10.
 */
class BindingHolder<out T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root)