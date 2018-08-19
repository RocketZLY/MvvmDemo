package rocketly.mvvmdemo.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import me.drakeet.multitype.ItemViewBinder
import rocketly.mvvmdemo.databinding.ItemHotCityListBinding
import rocketly.mvvmdemo.model.Basic
import rocketly.mvvmdemo.utils.databinding.BindingHolder
import rocketly.mvvmdemo.viewmodel.HotCityListVM

/**
 * Created by Rocket on 2018/8/19.
 */
class HotCityListViewBinder(private val vm: HotCityListVM?) : ItemViewBinder<Basic, BindingHolder<ItemHotCityListBinding>>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): BindingHolder<ItemHotCityListBinding> {
        return BindingHolder(ItemHotCityListBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: BindingHolder<ItemHotCityListBinding>, item: Basic) {
        holder.binding.data = item
        holder.binding.vm = vm
        holder.binding.executePendingBindings()
    }
}