package rocketly.mvvmdemo.net.remote

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import rocketly.mvvmdemo.model.CityList

/**
 * Created by zhuliyuan on 2018/8/16.
 */
interface CityApi {
    @GET("top")
    fun getHotCityList(@Query("group") group: String, @Query("key") key: String, @Query("number") num: Int): Observable<CityList>

    @GET("find")
    fun getCityDetail(@Query("location") location: String, @Query("key") key: String): Observable<CityList>
}