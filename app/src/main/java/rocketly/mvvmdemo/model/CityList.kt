package rocketly.mvvmdemo.model

/**
 * Created by zhuliyuan on 2018/8/16.
 */

data class CityList(
        val HeWeather6: List<HeWeather6>
)

data class HeWeather6(
        val basic: List<Basic>,
        val status: String
)

data class Basic(
        val cid: String,
        val location: String,
        val parent_city: String,
        val admin_area: String,
        val cnty: String,
        val lat: String,
        val lon: String,
        val tz: String,
        val type: String
)
