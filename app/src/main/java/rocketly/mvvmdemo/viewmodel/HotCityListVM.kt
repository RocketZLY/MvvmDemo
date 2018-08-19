package rocketly.mvvmdemo.viewmodel

import android.databinding.ObservableArrayList
import com.boohee.one.ui.viewmodel.BaseVM
import rocketly.mvvmdemo.model.Basic
import rocketly.mvvmdemo.net.ApiObserver
import rocketly.mvvmdemo.net.repo.CityRepository
import rocketly.mvvmdemo.utils.databinding.SingleLiveEvent

/**
 * Created by zhuliyuan on 2018/8/17.
 */
class HotCityListVM : BaseVM() {
    val cityList = ObservableArrayList<Basic>()
    val hotCityItemEvent = SingleLiveEvent<String>()

    override fun onFirstLoad() {
        super.onFirstLoad()
        load()
    }

    override fun onRefresh() {
        super.onRefresh()
        load()
    }

    private fun load() {
        CityRepository.getHotCityList(num = 50)
                .subscribe(ApiObserver(success = {
                    resetLoadStatus()
                    cityList.clear()
                    cityList.addAll(it.HeWeather6[0].basic)
                }, error = {
                    resetLoadStatus()
                }))
    }

    fun hotCityItemClick(s: String) {
        hotCityItemEvent.value = s
    }
}