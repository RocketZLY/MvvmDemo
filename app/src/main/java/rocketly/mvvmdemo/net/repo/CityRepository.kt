package rocketly.mvvmdemo.net.repo

import io.reactivex.Observable
import rocketly.mvvmdemo.model.CityList
import rocketly.mvvmdemo.net.AUTHKEY
import rocketly.mvvmdemo.net.ApiProvider
import rocketly.mvvmdemo.net.Io2UiTransform
import rocketly.mvvmdemo.net.remote.CityApi

/**
 * Created by zhuliyuan on 2018/8/16.
 */
object CityRepository {
    private val remoteSource by lazy {
        ApiProvider.create(CityApi::class.java)
    }

    fun getHotCityList(group: String = "world", key: String = AUTHKEY, num: Int = 10): Observable<CityList> = remoteSource.getHotCityList(group, key, num).compose(Io2UiTransform())

    fun getCicyDetail(location: String, key: String = AUTHKEY): Observable<CityList> = remoteSource.getCityDetail(location, key).compose(Io2UiTransform())
}